// ...existing code...
// ...existing code...
import { Component, OnDestroy, OnInit, ChangeDetectorRef } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { Router, RouterLink } from '@angular/router';
import { NgFor, NgIf } from '@angular/common';
import { I18nPropertiesService } from '../../core/i18n-properties.service';

import { AuthService, DashboardResponse, DashboardUserProject } from '../../core/auth.service';

/**
 * Dashboard protetta che mostra dati utente ottenuti da endpoint backend autenticato.
 */
@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [NgIf, NgFor, RouterLink],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent implements OnInit, OnDestroy {
  message = '';
  username = '';
  subject = '';
  decodedClaims: Record<string, unknown> | null = null;
  decodedClaimsJson = '';
  userProjects: DashboardUserProject[] = [];
  groupedProjects: Array<{ tenantCode: string, tenantName: string, projects: DashboardUserProject[] }> = [];
  errorMessage = '';
  isMessageFading = false;
  isErrorFading = false;
  translations: Record<string, string> = {};
  private messageFadeTimeoutId: ReturnType<typeof setTimeout> | null = null;
  private messageClearTimeoutId: ReturnType<typeof setTimeout> | null = null;
  private errorFadeTimeoutId: ReturnType<typeof setTimeout> | null = null;
  private errorClearTimeoutId: ReturnType<typeof setTimeout> | null = null;

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
      next: (response: DashboardResponse) => {
        this.showSuccessMessage(response.message);
        this.username = response.username;
        this.applyJwtDebugInfo(response);
        this.userProjects = Array.isArray(response.userProjects) ? response.userProjects : [];

        // Raggruppa per tenantCode e tenantName
        const grouped: { [tenantKey: string]: { tenantCode: string, tenantName: string, projects: DashboardUserProject[] } } = {};
        this.userProjects.forEach(project => {
          const key = project.tenantId || project.tenantCode || project.tenantName || 'unknown';
          if (!grouped[key]) {
            grouped[key] = {
              tenantCode: project.tenantCode || '',
              tenantName: project.tenantName || '',
              projects: []
            };
          }
          grouped[key].projects.push(project);
        });
        this.groupedProjects = Object.values(grouped);

        // Log per debug
        this.groupedProjects.forEach((group, idx) => {
          console.log(`[SUPERBOX ${idx}] tenantName:`, group.tenantName, '| tenantCode:', group.tenantCode, '| progetti:', group.projects.length);
        });
      },
      error: () => {
        this.showErrorMessage(this.t('dashboard.error.invalidToken'));
      }
    });
  }

  ngOnDestroy(): void {
    this.clearMessageTimers();
    this.clearErrorTimers();
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  t(key: string): string {
    return this.translations[key] ?? key;
  }

  openTenantsDashboard(client: string, role: string, project?: string): void {
    const token = this.authService.getToken();

    if (!token) {
      this.showErrorMessage(this.t('dashboard.error.jwtMissing'));
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
            // Passa project solo se valorizzato
            if (project !== undefined && project !== null && project !== '') {
              targetUrl.searchParams.set('project', project);
            }
            window.location.href = targetUrl.toString();
          },
          error: (error: HttpErrorResponse) => {
            this.showErrorMessage(this.getTenantResolutionErrorMessage(error, role, client));
          }
        });
      },
      error: (error: HttpErrorResponse) => {
        this.showErrorMessage(this.getTenantResolutionErrorMessage(error, role, client));
      }
    });
  }

  /**
   * Mostra un banner verde temporaneo e lo dissolve automaticamente dopo 10 secondi.
   */
  private showSuccessMessage(message: string): void {
    this.clearMessageTimers();
    this.message = message;
    this.isMessageFading = false;

    if (!message) {
      this.cdr.detectChanges();
      return;
    }

    this.messageFadeTimeoutId = setTimeout(() => {
      this.isMessageFading = true;
      this.cdr.detectChanges();
    }, 9000);

    this.messageClearTimeoutId = setTimeout(() => {
      this.message = '';
      this.isMessageFading = false;
      this.cdr.detectChanges();
    }, 10000);
  }

  /**
   * Mostra un banner rosso temporaneo e lo dissolve automaticamente dopo 15 secondi.
   */
  private showErrorMessage(message: string): void {
    this.clearErrorTimers();
    this.errorMessage = message;
    this.isErrorFading = false;

    if (!message) {
      this.cdr.detectChanges();
      return;
    }

    this.errorFadeTimeoutId = setTimeout(() => {
      this.isErrorFading = true;
      this.cdr.detectChanges();
    }, 14000);

    this.errorClearTimeoutId = setTimeout(() => {
      this.errorMessage = '';
      this.isErrorFading = false;
      this.cdr.detectChanges();
    }, 15000);

    this.cdr.detectChanges();
  }

  private clearMessageTimers(): void {
    if (this.messageFadeTimeoutId) {
      clearTimeout(this.messageFadeTimeoutId);
      this.messageFadeTimeoutId = null;
    }
    if (this.messageClearTimeoutId) {
      clearTimeout(this.messageClearTimeoutId);
      this.messageClearTimeoutId = null;
    }
  }

  private clearErrorTimers(): void {
    if (this.errorFadeTimeoutId) {
      clearTimeout(this.errorFadeTimeoutId);
      this.errorFadeTimeoutId = null;
    }
    if (this.errorClearTimeoutId) {
      clearTimeout(this.errorClearTimeoutId);
      this.errorClearTimeoutId = null;
    }
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
    const normalizedUrl = rawTenantUrl?.trim();

    if (!normalizedUrl) {
      return new URL('/dashboard', window.location.origin);
    }

    const targetUrl = /^https?:\/\//i.test(normalizedUrl)
      ? new URL(normalizedUrl)
      : new URL(normalizedUrl.startsWith('/') ? normalizedUrl : `/${normalizedUrl}`, window.location.origin);

    if (targetUrl.pathname === '' || targetUrl.pathname === '/') {
      targetUrl.pathname = '/dashboard';
    }

    return targetUrl;
  }

  /**
   * Ripristina la sezione debug JWT usando i dati backend se presenti, altrimenti decodifica il token locale.
   */
  private applyJwtDebugInfo(response: DashboardResponse): void {
    const backendClaims = this.normalizeClaims(response.decodedClaims);
    const tokenClaims = this.decodeClaimsFromCurrentToken();
    const effectiveClaims = backendClaims ?? tokenClaims;

    this.decodedClaims = effectiveClaims;
    this.decodedClaimsJson = effectiveClaims ? JSON.stringify(effectiveClaims, null, 2) : '';
    this.subject = typeof response.subject === 'string' && response.subject.trim() !== ''
      ? response.subject
      : this.extractSubject(effectiveClaims);
  }

  private decodeClaimsFromCurrentToken(): Record<string, unknown> | null {
    const token = this.authService.getToken();
    if (!token) {
      return null;
    }

    const parts = token.split('.');
    if (parts.length < 2) {
      return null;
    }

    try {
      const payload = this.decodeBase64Url(parts[1]);
      const parsed = JSON.parse(payload) as unknown;
      return this.normalizeClaims(parsed);
    } catch {
      return null;
    }
  }

  private normalizeClaims(value: unknown): Record<string, unknown> | null {
    if (!value || typeof value !== 'object' || Array.isArray(value)) {
      return null;
    }

    return value as Record<string, unknown>;
  }

  private extractSubject(claims: Record<string, unknown> | null): string {
    if (!claims) {
      return '';
    }

    const subjectClaim = claims['sub'];
    return typeof subjectClaim === 'string' ? subjectClaim : '';
  }

  private decodeBase64Url(value: string): string {
    const base64 = value.replace(/-/g, '+').replace(/_/g, '/');
    const paddingLength = (4 - (base64.length % 4)) % 4;
    const padded = base64 + '='.repeat(paddingLength);
    return atob(padded);
  }

  private groupProjectsByClient(userProjects: DashboardUserProject[]): Map<string, DashboardUserProject[]> {
    return userProjects
      .filter((project) => Boolean(project.tenantCode && project.projectCode))
      .reduce((projectsByClient, project) => {
        const clientCode = project.tenantCode as string;
        const currentProjects = projectsByClient.get(clientCode) ?? [];
        const alreadyAdded = currentProjects.some((currentProject) =>
          currentProject.projectId === project.projectId
          || currentProject.projectCode === project.projectCode
        );

        if (!alreadyAdded) {
          projectsByClient.set(clientCode, [...currentProjects, project]);
        }

        return projectsByClient;
      }, new Map<string, DashboardUserProject[]>());
  }

  /**
   * Chiude la modale di errore e resetta il messaggio.
   */
  closeErrorModal(): void {
    this.clearErrorTimers();
    this.errorMessage = '';
    this.isErrorFading = false;
    this.cdr.detectChanges();
  }
}
