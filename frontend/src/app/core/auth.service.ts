import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { environment } from '../../environments/environment';

interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  refreshExpiresIn: number;
}

interface DashboardResponse {
  message: string;
  username: string;
  subject: string;
  clientRoles: Record<string, string[]>;
  decodedClaims: Record<string, unknown>;
}

interface TenantResolutionResponse {
  clientCode: string;
  tenantAppUrl: string;
}

/**
 * Service centralizzato per autenticazione e chiamate backend protette.
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly tokenStorageKey = 'qtm_access_token';

  constructor(private readonly http: HttpClient) {}

  login(username: string, password: string): Observable<LoginResponse> {
    return this.http
      .post<LoginResponse>(`${environment.apiBaseUrl}/auth/login`, { username, password })
      .pipe(tap((response) => this.setToken(response.accessToken)));
  }

  getDashboardData(): Observable<DashboardResponse> {
    return this.http.get<DashboardResponse>(`${environment.apiBaseUrl}/users/dashboard`);
  }

  resolveTenantAppUrl(clientCode: string): Observable<TenantResolutionResponse> {
    return this.http.get<TenantResolutionResponse>(
      `${environment.apiBaseUrl}/tenant-app-pointers/resolve/${encodeURIComponent(clientCode)}`
    );
  }

  getToken(): string | null {
    const token = localStorage.getItem(this.tokenStorageKey);

    if (!token) {
      return null;
    }

    if (this.isTokenExpired(token)) {
      localStorage.removeItem(this.tokenStorageKey);
      return null;
    }

    return token;
  }

  isAuthenticated(): boolean {
    return this.getToken() !== null;
  }

  logout(): void {
    localStorage.removeItem(this.tokenStorageKey);
  }

  private setToken(token: string): void {
    localStorage.setItem(this.tokenStorageKey, token);
  }

  private isTokenExpired(token: string): boolean {
    const parts = token.split('.');

    if (parts.length < 2) {
      return true;
    }

    try {
      const payload = this.decodeBase64Url(parts[1]);
      const claims = JSON.parse(payload) as { exp?: number };

      if (typeof claims.exp !== 'number') {
        return true;
      }

      const nowInSeconds = Math.floor(Date.now() / 1000);
      return claims.exp <= nowInSeconds;
    } catch {
      return true;
    }
  }

  private decodeBase64Url(value: string): string {
    const base64 = value.replace(/-/g, '+').replace(/_/g, '/');
    const paddingLength = (4 - (base64.length % 4)) % 4;
    const padded = base64 + '='.repeat(paddingLength);
    return atob(padded);
  }
}
