import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map, catchError, of } from 'rxjs';

/**
 * Carica traduzioni da file .properties statici (es. i18n/messages_it.properties).
 */
@Injectable({ providedIn: 'root' })
export class I18nPropertiesService {
  private readonly fallbackLanguage = 'it';

  constructor(private readonly http: HttpClient) {}

  loadTranslations(language: string): Observable<Record<string, string>> {
    const normalized = this.normalizeLanguage(language);
    return this.loadFile(normalized).pipe(
      catchError(() => this.loadFile(this.fallbackLanguage)),
      catchError(() => of({}))
    );
  }

  private loadFile(language: string): Observable<Record<string, string>> {
    // FIXME Francesco: usare sempre URL relativi per le risorse frontend; lo slash iniziale ignora il path prefix di deploy.
    return this.http
      .get(`i18n/messages_${language}.properties`, { responseType: 'text' })
      .pipe(map((content) => this.parseProperties(content)));
  }

  private normalizeLanguage(language: string): string {
    if (!language || language.trim().length === 0) {
      return this.fallbackLanguage;
    }
    return language.toLowerCase().split('-')[0];
  }

  private parseProperties(content: string): Record<string, string> {
    return content
      .split(/\r?\n/)
      .map((line) => line.trim())
      .filter((line) => line.length > 0 && !line.startsWith('#') && !line.startsWith('!'))
      .reduce<Record<string, string>>((acc, line) => {
        const separatorIndex = line.indexOf('=');
        if (separatorIndex <= 0) {
          return acc;
        }
        const key = line.slice(0, separatorIndex).trim();
        const value = line.slice(separatorIndex + 1).trim();
        acc[key] = value;
        return acc;
      }, {});
  }
}
