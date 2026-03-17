// ...existing code...
// ...existing code...
import { Component, OnDestroy, OnInit, ChangeDetectorRef } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { Router, RouterLink } from '@angular/router';
import { NgFor, NgIf } from '@angular/common';
import { AuthService, DashboardResponse, DashboardUserProject } from '../../core/auth.service';
import { I18nPropertiesService } from '../../core/i18n-properties.service';

interface DashboardRoleGroup {
  client: string;
  resourceRoles: string[];
  realmRoles: string[];
  projectCode?: string;
  projectDescription?: string;
}

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
  groupedClientRoles: DashboardRoleGroup[] = [];
  userProjects: DashboardUserProject[] = [];
  decodedClaimsPretty = '';
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
        this.subject = response.subject;
        this.userProjects = Array.isArray(response.userProjects) ? response.userProjects : [];

        const resourceClients = Object.keys(response.clientRoles ?? {}).filter(client => client.toLowerCase() !== 'account').sort();
        const claims = response.decodedClaims ?? {};
        const realmAccess = (claims['realm_access'] as any)?.roles as string[] | undefined;
        const standardRoles = [
          'default-roles-qtm',
          'offline_access',
          'uma_authorization'
        ];
        const customRealmRoles = (realmAccess ?? []).filter(r => !standardRoles.includes(r));

        const projectsByClient = this.groupProjectsByClient(this.userProjects);
        this.groupedClientRoles = resourceClients.flatMap((client) => {
          const resourceRoles = (response.clientRoles?.[client] ?? []).slice().sort();
          const realmRoles = (customRealmRoles ?? []).slice().sort();
          const clientProjects = projectsByClient.get(client) ?? [];

          // Se non ci sono progetti specifici, NON mostrare box progetto e passa null
          if (clientProjects.length === 0) {
            return [{
              client,
              resourceRoles,
              realmRoles,
              projectCode: null,
              projectDescription: null
            }];
          }

          return clientProjects.map((project) => ({
            client,
            resourceRoles,
            realmRoles,
            projectCode: project.projectCode,
            projectDescription: project.projectDescription
          }));
        });

        this.decodedClaimsPretty = JSON.stringify(response.decodedClaims ?? {}, null, 2);
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
