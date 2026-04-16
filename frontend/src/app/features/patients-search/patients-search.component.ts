import { CommonModule } from '@angular/common';
import { HttpClient, HttpErrorResponse, HttpParams } from '@angular/common/http';
import { ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { I18nPropertiesService } from '../../core/i18n-properties.service';
import { environment } from '../../../environments/environment';
import { Subscription } from 'rxjs';

const AUTO_DISMISS_DELAY_MS = 4000;
const PATIENTS_API_URL = `${environment.apiBaseUrl}/patients`;

interface PatientSummary {
  id: number;
  assistedId?: string;
  firstName?: string;
  lastName?: string;
  fiscalCode?: string;
  email?: string;
  structureId?: number;
}

interface PatientSearchFilters {
  assistedId: string;
  firstName: string;
  lastName: string;
  email: string;
  fiscalCode: string;
  structureId: string;
}

/**
 * Vista di ricerca pazienti integrata nel frontend di QTMDB.
 */
@Component({
  selector: 'app-patients-search',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <section class="page-card">
      <header class="page-header">
        <div>
          <h2>{{ t('patients.search.title') }}</h2>
          <p>{{ t('patients.search.subtitle') }}</p>
        </div>
        <button type="button" class="primary-btn" (click)="openNew()">{{ t('crud.actions.new') }}</button>
      </header>

      <div *ngIf="message" class="message-box" [class.message-box-error]="messageType === 'error'">
        {{ message }}
      </div>

      <form class="filters-grid" (ngSubmit)="search()">
        <label>
          <span>{{ t('patients.field.assistedId') }}</span>
          <input [(ngModel)]="filters.assistedId" name="assistedId" type="text" />
        </label>

        <label>
          <span>{{ t('patients.field.firstName') }}</span>
          <input [(ngModel)]="filters.firstName" name="firstName" type="text" />
        </label>

        <label>
          <span>{{ t('patients.field.lastName') }}</span>
          <input [(ngModel)]="filters.lastName" name="lastName" type="text" />
        </label>

        <label>
          <span>{{ t('patients.field.email') }}</span>
          <input [(ngModel)]="filters.email" name="email" type="text" />
        </label>

        <label>
          <span>{{ t('patients.field.fiscalCode') }}</span>
          <input [(ngModel)]="filters.fiscalCode" name="fiscalCode" type="text" />
        </label>

        <label>
          <span>{{ t('patients.field.structureId') }}</span>
          <input [(ngModel)]="filters.structureId" name="structureId" type="number" />
        </label>

        <div class="actions-row">
          <button type="submit" class="primary-btn">{{ t('crud.actions.search') }}</button>
          <button type="button" class="secondary-btn" (click)="reset()">{{ t('crud.actions.reset') }}</button>
        </div>
      </form>

      <section class="results-section">
        <h3>{{ t('search.results') }}</h3>

        <table class="results-table" *ngIf="results.length > 0; else emptyState">
          <thead>
            <tr>
              <th>{{ t('common.id') }}</th>
              <th>{{ t('patients.field.assistedId') }}</th>
              <th>{{ t('patients.field.firstName') }}</th>
              <th>{{ t('patients.field.lastName') }}</th>
              <th>{{ t('patients.field.fiscalCode') }}</th>
              <th>{{ t('patients.field.email') }}</th>
              <th>{{ t('search.actions') }}</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let patient of results">
              <td>{{ patient.id }}</td>
              <td>{{ patient.assistedId || '-' }}</td>
              <td>{{ patient.firstName || '-' }}</td>
              <td>{{ patient.lastName || '-' }}</td>
              <td>{{ patient.fiscalCode || '-' }}</td>
              <td>{{ patient.email || '-' }}</td>
              <td class="table-actions">
                <button type="button" class="secondary-btn" (click)="openView(patient.id)">{{ t('search.action.view') }}</button>
                <button type="button" class="primary-btn" (click)="openEdit(patient.id)">{{ t('search.action.edit') }}</button>
              </td>
            </tr>
          </tbody>
        </table>

        <ng-template #emptyState>
          <p class="empty-state">{{ t('search.noResults') }}</p>
        </ng-template>
      </section>
    </section>
  `,
  styles: [
    `
      .page-card { background: #fff; border-radius: 18px; padding: 24px; box-shadow: 0 16px 40px rgba(15, 23, 42, 0.08); }
      .page-header { display: flex; justify-content: space-between; align-items: flex-start; gap: 16px; margin-bottom: 20px; }
      .page-header h2 { margin: 0 0 6px; }
      .page-header p { margin: 0; color: #5b687a; }
      .filters-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 16px; margin-bottom: 24px; }
      .filters-grid label { display: grid; gap: 6px; font-weight: 600; }
      .filters-grid input { border: 1px solid #d6deea; border-radius: 10px; padding: 10px 12px; }
      .actions-row { display: flex; align-items: end; gap: 10px; }
      .results-section h3 { margin: 0 0 12px; }
      .results-table { width: 100%; border-collapse: collapse; }
      .results-table th, .results-table td { padding: 12px; border-bottom: 1px solid #e5eaf3; text-align: left; }
      .table-actions { display: flex; gap: 8px; }
      .primary-btn, .secondary-btn { border: 0; border-radius: 10px; padding: 10px 14px; cursor: pointer; }
      .primary-btn { background: #2f67c7; color: #fff; }
      .secondary-btn { background: #e8edf6; color: #142033; }
      .message-box { margin-bottom: 16px; padding: 12px 14px; border-radius: 10px; background: #e9f7ef; color: #1d6b3b; }
      .message-box-error { background: #fdecec; color: #b42318; }
      .empty-state { padding: 20px 0; color: #5b687a; }
      @media (max-width: 700px) {
        .page-header { flex-direction: column; }
        .actions-row, .table-actions { flex-wrap: wrap; }
        .results-table { display: block; overflow-x: auto; }
      }
    `
  ]
})
export class PatientsSearchComponent implements OnInit, OnDestroy {
  filters: PatientSearchFilters = {
    assistedId: '',
    firstName: '',
    lastName: '',
    email: '',
    fiscalCode: '',
    structureId: ''
  };

  results: PatientSummary[] = [];
  message = '';
  messageType: 'success' | 'error' = 'success';
  translations: Record<string, string> = {};
  private messageTimeoutId: number | null = null;
  private readonly subscriptions = new Subscription();

  constructor(
    private readonly http: HttpClient,
    private readonly router: Router,
    private readonly changeDetectorRef: ChangeDetectorRef,
    private readonly i18nPropertiesService: I18nPropertiesService
  ) {}

  ngOnInit(): void {
    this.subscriptions.add(
      this.i18nPropertiesService.loadTranslations(navigator.language).subscribe({
        next: (translationMap) => {
          this.translations = translationMap;
          this.consumeFlashMessage();
          this.search(false);
        }
      })
    );
  }

  ngOnDestroy(): void {
    this.clearMessageTimer();
    this.subscriptions.unsubscribe();
  }

  t(key: string): string {
    return this.translations[key] ?? key;
  }

  search(showFeedback = true): void {
    let params = new HttpParams();

    for (const [key, value] of Object.entries(this.filters) as Array<[keyof PatientSearchFilters, string]>) {
      if (!value) {
        continue;
      }
      params = params.set(key, value);
    }

    this.subscriptions.add(
      this.http.get<PatientSummary[]>(PATIENTS_API_URL, { params }).subscribe({
        next: (results) => {
          this.results = results;
          if (showFeedback) {
            this.showMessage(this.t('search.success.completed'));
          }
        },
        error: (error: HttpErrorResponse) => {
          this.showMessage(this.extractErrorMessage(error, 'crud.error.search'), 'error');
        }
      })
    );
  }

  reset(): void {
    this.filters = {
      assistedId: '',
      firstName: '',
      lastName: '',
      email: '',
      fiscalCode: '',
      structureId: ''
    };
    this.search(false);
  }

  openNew(): void {
    void this.router.navigateByUrl('/patients/new');
  }

  openEdit(id: number): void {
    void this.router.navigate(['/patients', id]);
  }

  openView(id: number): void {
    void this.router.navigate(['/patients', id, 'view']);
  }

  private extractErrorMessage(error: HttpErrorResponse, fallbackKey: string): string {
    const detail = error.error?.detail;
    return typeof detail === 'string' && detail.length > 0 ? detail : this.t(fallbackKey);
  }

  private consumeFlashMessage(): void {
    const state = window.history.state as { flashMessage?: string; flashMessageType?: 'success' | 'error' } | null;

    if (!state?.flashMessage) {
      return;
    }

    this.showMessage(state.flashMessage, state.flashMessageType ?? 'success');

    const nextState = { ...state };
    delete nextState.flashMessage;
    delete nextState.flashMessageType;
    window.history.replaceState(nextState, document.title, `${window.location.pathname}${window.location.search}${window.location.hash}`);
  }

  private showMessage(message: string, type: 'success' | 'error' = 'success', persistent = false): void {
    this.clearMessageTimer();
    this.messageType = type;
    this.message = message;

    if (persistent || !message) {
      return;
    }

    const activeMessage = message;
    this.messageTimeoutId = window.setTimeout(() => {
      if (this.message === activeMessage) {
        this.clearMessage();
      }
    }, AUTO_DISMISS_DELAY_MS);
  }

  private clearMessage(): void {
    this.clearMessageTimer();
    this.message = '';
    this.changeDetectorRef.detectChanges();
  }

  private clearMessageTimer(): void {
    if (this.messageTimeoutId !== null) {
      window.clearTimeout(this.messageTimeoutId);
      this.messageTimeoutId = null;
    }
  }
}