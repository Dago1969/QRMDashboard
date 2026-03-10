// ...existing code...
// ...existing code...
import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { Router } from '@angular/router';
import { NgFor, NgIf } from '@angular/common';
import { AuthService } from '../../core/auth.service';
import { I18nPropertiesService } from '../../core/i18n-properties.service';

/**
 * Dashboard protetta che mostra dati utente ottenuti da endpoint backend autenticato.
 */
@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [NgIf, NgFor],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent implements OnInit {
  message = '';
  username = '';
  subject = '';
  // Raggruppamento per la view: [{ client, resourceRoles: string[], realmRoles: string[] }]
  groupedClientRoles: Array<{ client: string; resourceRoles: string[]; realmRoles: string[] }> = [];
  decodedClaimsPretty = '';
  errorMessage = '';
  translations: Record<string, string> = {};

  constructor(
    private readonly authService: AuthService,
    private readonly router: Router,
    private readonly i18nPropertiesService: I18nPropertiesService,
    private readonly cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.i18nPropertiesService.loadTranslations(navigator.language).subscribe({
      next: (translationMap) => {
        this.translations = translationMap;
      }
    });

    this.authService.getDashboardData().subscribe({
      next: (response) => {
        this.message = response.message;
        this.username = response.username;
        this.subject = response.subject;

        // Raggruppa i box per client
        const resourceClients = Object.keys(response.clientRoles ?? {}).filter(client => client.toLowerCase() !== 'account').sort();
        const claims = response.decodedClaims ?? {};
        const realmAccess = (claims['realm_access'] as any)?.roles as string[] | undefined;
        const standardRoles = [
          'default-roles-qtm',
          'offline_access',
          'uma_authorization'
        ];
        const customRealmRoles = (realmAccess ?? []).filter(r => !standardRoles.includes(r));

        // Prepara la struttura raggruppata per la view
        this.groupedClientRoles = resourceClients.map(client => {
          // Resource roles ordinati
          const resourceRoles = (response.clientRoles?.[client] ?? []).slice().sort();
          // Realm roles ordinati
          const realmRoles = (customRealmRoles ?? []).slice().sort();
          return {
            client,
            resourceRoles,
            realmRoles
          };
        });

        this.decodedClaimsPretty = JSON.stringify(response.decodedClaims ?? {}, null, 2);
      },
      error: () => {
        this.errorMessage = this.t('dashboard.error.invalidToken');
        this.cdr.detectChanges();
      }
    });
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  t(key: string): string {
    return this.translations[key] ?? key;
  }

  openTenantsDashboard(client: string, role: string): void {
    const token = this.authService.getToken();

    if (!token) {
      this.errorMessage = this.t('dashboard.error.jwtMissing');
      return;
    }

    this.authService.resolveTenantAppUrl(client).subscribe({
      next: (resolution) => {
        this.authService.validateTenantRole(role).subscribe({
          next: () => {
            const targetUrl = this.buildTenantTargetUrl(resolution.tenantAppUrl);
            targetUrl.searchParams.set('token', token);
            targetUrl.searchParams.set('client', client);
            targetUrl.searchParams.set('role', role);
            window.location.href = targetUrl.toString();
          },
          error: (error: HttpErrorResponse) => {
            this.errorMessage = this.getTenantResolutionErrorMessage(error, role, client);
            this.cdr.detectChanges();
          }
        });
      },
      error: (error: HttpErrorResponse) => {
        this.errorMessage = this.getTenantResolutionErrorMessage(error, role, client);
        this.cdr.detectChanges();
      }
    });
  }

  private getTenantResolutionErrorMessage(error: HttpErrorResponse, role: string, client?: string): string {
    const clientName = client || 'TENANTS-APP';
    if (error.status === 404) {
      return this.t('dashboard.error.tenantRoleNotProvisioned')
        .replace('{role}', role)
        .replaceAll('{client}', clientName);
    }

    const detail = this.extractErrorDetail(error).toLowerCase();
    if (detail.includes('ruolo non trovato') || detail.includes('role not found')) {
      return this.t('dashboard.error.tenantRoleNotProvisioned')
        .replace('{role}', role)
        .replaceAll('{client}', clientName);
    }

    return this.t('dashboard.error.tenantResolutionFailed');
  }

  private extractErrorDetail(error: HttpErrorResponse): string {
    const payload = error.error;
    if (typeof payload === 'string') {
      return payload;
    }
    if (payload && typeof payload === 'object') {
      const detail = (payload as { detail?: unknown }).detail;
      if (typeof detail === 'string') {
        return detail;
      }
      const message = (payload as { message?: unknown }).message;
      if (typeof message === 'string') {
        return message;
      }
    }
    return '';
  }

  private buildTenantTargetUrl(rawTenantUrl: string): URL {
    const targetUrl = new URL(rawTenantUrl);

    if (targetUrl.pathname === '' || targetUrl.pathname === '/') {
      targetUrl.pathname = '/dashboard';
    }

    return targetUrl;
  }

  /**
   * Chiude la modale di errore e resetta il messaggio.
   */
  closeErrorModal(): void {
    this.errorMessage = '';
    this.cdr.detectChanges();
  }
}
