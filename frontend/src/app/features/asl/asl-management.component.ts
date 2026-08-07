import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { I18nPropertiesService } from '../../core/i18n-properties.service';
// FIXME Francesco: mantenere gli endpoint ASL agganciati alla configurazione frontend, senza URL assoluti cablati.
import { environment } from '../../../environments/environment';

interface ProblemDetailPayload {
  detail?: string;
  message?: string;
}

interface AslRecord {
  id: number;
  codiceAzienda: string;
  denominazioneAzienda: string;
  indirizzo?: string;
  telefono?: string;
  email?: string;
  imported: boolean;
  note?: string | null;
  codiceRegione?: string;
}

@Component({
  selector: 'app-asl-management',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="card dashboard-content-card">
      <div class="dashboard-header">
        <h2>{{ t('dashboard.menu.asl') }}</h2>
      </div>

      <p class="dashboard-selection-info">{{ t('asl.management.subtitle') }}</p>

      <div class="card" style="padding: 1rem; margin-bottom: 1rem;">
        <div class="form-row">
          <label>{{ t('asl.filter.id') }}</label>
          <input type="number" [(ngModel)]="filters.id" />
        </div>
        <div class="form-row">
          <label>{{ t('asl.filter.code') }}</label>
          <input type="text" [(ngModel)]="filters.code" />
        </div>
        <div class="form-row">
          <label>{{ t('asl.filter.name') }}</label>
          <input type="text" [(ngModel)]="filters.name" />
        </div>
        <!-- imported filter removed as per UI update -->
        <div class="form-row" style="gap: 0.5rem;">
          <button class="btn btn-primary btn-sm" type="button" (click)="search()">{{ t('crud.actions.search') }}</button>
          <button class="btn btn-outline btn-sm" type="button" (click)="resetFilters()">{{ t('crud.actions.reset') }}</button>
        </div>
      </div>

      <div *ngIf="message" class="alert" [class.alert-success]="messageType === 'success'" [class.alert-danger]="messageType === 'error'">
        {{ message }}
      </div>

      <div class="table-responsive">
        <table class="search-table">
          <thead>
            <tr>
              <th>{{ t('asl.column.id') }}</th>
              <th>{{ t('asl.column.code') }}</th>
              <th>{{ t('asl.column.name') }}</th>
              <th>{{ t('asl.column.regionCode') }}</th>
              <th>{{ t('asl.column.address') }}</th>
              <th>{{ t('asl.column.email') }}</th>
              <th>{{ t('asl.column.phone') }}</th>
              <!-- removed imported and note columns from list view -->
              <th>{{ t('search.actions') }}</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let asl of filteredRecords()">
              <td>{{ asl.id }}</td>
              <td>{{ asl.codiceAzienda }}</td>
              <td>{{ asl.denominazioneAzienda }}</td>
              <td>{{ asl.codiceRegione || '-' }}</td>
              <td>{{ asl.indirizzo || '-' }}</td>
              <td>{{ asl.email || '-' }}</td>
              <td>{{ asl.telefono || '-' }}</td>
              <td>
                <button class="btn btn-primary btn-sm" type="button" (click)="importRow(asl)" *ngIf="!asl.imported">
                  {{ t('asl.action.importRow') }}
                </button>
                <button class="btn btn-secondary btn-sm" type="button" (click)="disassociateRow(asl)" *ngIf="asl.imported">
                  {{ t('asl.action.disassociateRow') }}
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  `
})
export class AslManagementComponent implements OnInit {
  filters = {
    id: '' as string,
    code: '' as string,
    name: '' as string,
    imported: 'all' as 'all' | 'imported' | 'notImported'
  };
  allAslRecords: AslRecord[] = [];
  translations: Record<string, string> = {};
  message = '';
  messageType: 'success' | 'error' = 'success';

  constructor(
    private readonly http: HttpClient,
    private readonly i18nPropertiesService: I18nPropertiesService
  ) {}

  ngOnInit(): void {
    this.i18nPropertiesService.loadTranslations(navigator.language).subscribe((translations: Record<string, string>) => {
      this.translations = translations;
      this.loadOverview();
    });
  }

  t(key: string): string {
    return this.translations[key] ?? key;
  }

  loadOverview(): void {
    // FIXME Francesco: usare sempre environment.apiBaseUrl per le API frontend; non introdurre percorsi assoluti /api.
    this.http.get<AslRecord[]>(`${environment.apiBaseUrl}/asl/overview`).subscribe({
      next: (records: AslRecord[]) => {
        this.allAslRecords = records;
      },
      error: (error: { error?: ProblemDetailPayload }) => {
        this.showErrorMessage(error, 'asl.messages.loadError');
      }
    });
  }

  filteredRecords(): AslRecord[] {
    return this.allAslRecords.filter((asl) => {
      if (this.filters.id && asl.id !== Number(this.filters.id)) {
        return false;
      }
      if (this.filters.code && !asl.codiceAzienda.toLowerCase().includes(this.filters.code.toLowerCase())) {
        return false;
      }
      if (this.filters.name && !asl.denominazioneAzienda.toLowerCase().includes(this.filters.name.toLowerCase())) {
        return false;
      }
      if (this.filters.imported === 'imported' && !asl.imported) {
        return false;
      }
      if (this.filters.imported === 'notImported' && asl.imported) {
        return false;
      }
      return true;
    });
  }

  search(): void {
    // The table is filtered locally, so this is only needed to refresh the view.
  }

  resetFilters(): void {
    this.filters = { id: '', code: '', name: '', imported: 'all' };
  }

  importRow(asl: AslRecord): void {
    // FIXME Francesco: usare sempre environment.apiBaseUrl; /api assoluto non rispetta il base path di deploy.
    this.http.post<AslRecord[]>(`${environment.apiBaseUrl}/asl/import`, { sourceIds: [asl.id] }).subscribe({
      next: () => {
        this.showMessage('asl.messages.associateSuccess', 'success');
        this.loadOverview();
      },
      error: (error: { error?: ProblemDetailPayload }) => {
        this.showErrorMessage(error, 'asl.messages.associateError');
      }
    });
  }

  disassociateRow(asl: AslRecord): void {
    // FIXME Francesco: usare sempre environment.apiBaseUrl; /api assoluto non rispetta il base path di deploy.
    this.http.delete<void>(`${environment.apiBaseUrl}/asl/${asl.id}`).subscribe({
      next: () => {
        this.showMessage('asl.messages.disassociateSuccess', 'success');
        this.loadOverview();
      },
      error: (error: { error?: ProblemDetailPayload }) => {
        this.showErrorMessage(error, 'asl.messages.disassociateError');
      }
    });
  }

  private showMessage(messageKey: string, type: 'success' | 'error'): void {
    this.message = this.t(messageKey);
    this.messageType = type;
    window.setTimeout(() => {
      this.message = '';
    }, 4000);
  }

  private showErrorMessage(error: { error?: ProblemDetailPayload } | undefined, fallbackKey: string): void {
    const detail = error?.error?.detail?.trim() || error?.error?.message?.trim();
    this.message = detail || this.t(fallbackKey);
    this.messageType = 'error';
    window.setTimeout(() => {
      this.message = '';
    }, 6000);
  }
}
