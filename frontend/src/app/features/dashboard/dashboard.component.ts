import { Component, OnInit } from '@angular/core';
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
  clientRoleEntries: Array<{ client: string; role: string }> = [];
  decodedClaimsPretty = '';
  errorMessage = '';
  translations: Record<string, string> = {};

  constructor(
    private readonly authService: AuthService,
    private readonly router: Router,
    private readonly i18nPropertiesService: I18nPropertiesService
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
        this.clientRoleEntries = Object.entries(response.clientRoles ?? {})
          .filter(([client]) => client.toLowerCase() !== 'account')
          .flatMap(([client, roles]) =>
            (roles ?? []).map((role) => ({
              client,
              role
            }))
          );
        this.decodedClaimsPretty = JSON.stringify(response.decodedClaims ?? {}, null, 2);
      },
      error: () => {
        this.errorMessage = this.t('dashboard.error.invalidToken');
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
        const targetUrl = this.buildTenantTargetUrl(resolution.tenantAppUrl);
        targetUrl.searchParams.set('token', token);
        targetUrl.searchParams.set('client', client);
        targetUrl.searchParams.set('role', role);
        window.location.href = targetUrl.toString();
      },
      error: () => {
        this.errorMessage = this.t('dashboard.error.tenantResolutionFailed');
      }
    });
  }

  private buildTenantTargetUrl(rawTenantUrl: string): URL {
    const targetUrl = new URL(rawTenantUrl);

    if (targetUrl.pathname === '' || targetUrl.pathname === '/') {
      targetUrl.pathname = '/dashboard';
    }

    return targetUrl;
  }
}
