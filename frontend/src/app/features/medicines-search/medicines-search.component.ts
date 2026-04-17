import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { I18nPropertiesService } from '../../core/i18n-properties.service';
import { MedicineApiService, MedicineDto, MedicineSearchFilters } from '../../core/medicine-api.service';

const AUTO_DISMISS_DELAY_MS = 4000;

/**
 * Vista di ricerca farmaci integrata nel frontend di QTMDB con azioni CRUD complete.
 */
@Component({
  selector: 'app-medicines-search',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <section class="page-card">
      <header class="page-header">
        <div>
          <h2>{{ t('medicines.search.title') }}</h2>
          <p>{{ t('medicines.search.subtitle') }}</p>
        </div>
        <button type="button" class="primary-btn" (click)="openNew()">{{ t('medicines.actions.new') }}</button>
      </header>

      <div *ngIf="message" class="message-box" [class.message-box-error]="messageType === 'error'">
        {{ message }}
      </div>

      <form class="filters-grid" (ngSubmit)="search()">
        <label>
          <span>{{ t('medicines.field.codiceAic') }}</span>
          <input [(ngModel)]="filters.codiceAic" name="codiceAic" type="text" />
        </label>

        <label>
          <span>{{ t('medicines.field.codFarmaco') }}</span>
          <input [(ngModel)]="filters.codFarmaco" name="codFarmaco" type="text" />
        </label>

        <label>
          <span>{{ t('medicines.field.codConfezione') }}</span>
          <input [(ngModel)]="filters.codConfezione" name="codConfezione" type="text" />
        </label>

        <label>
          <span>{{ t('medicines.field.denominazione') }}</span>
          <input [(ngModel)]="filters.denominazione" name="denominazione" type="text" />
        </label>

        <label>
          <span>{{ t('medicines.field.descrizione') }}</span>
          <input [(ngModel)]="filters.descrizione" name="descrizione" type="text" />
        </label>

        <label>
          <span>{{ t('medicines.field.codiceAtc') }}</span>
          <input [(ngModel)]="filters.codiceAtc" name="codiceAtc" type="text" />
        </label>

        <label>
          <span>{{ t('medicines.field.ragioneSociale') }}</span>
          <input [(ngModel)]="filters.ragioneSociale" name="ragioneSociale" type="text" />
        </label>

        <label>
          <span>{{ t('medicines.field.statoAmministrativo') }}</span>
          <input [(ngModel)]="filters.statoAmministrativo" name="statoAmministrativo" type="text" />
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
              <th>{{ t('medicines.field.codiceAic') }}</th>
              <th>{{ t('medicines.field.denominazione') }}</th>
              <th>{{ t('medicines.field.descrizione') }}</th>
              <th>{{ t('medicines.field.codiceAtc') }}</th>
              <th>{{ t('medicines.field.ragioneSociale') }}</th>
              <th>{{ t('search.actions') }}</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let medicine of results">
              <td>{{ medicine.id }}</td>
              <td>{{ medicine.codiceAic || '-' }}</td>
              <td>{{ medicine.denominazione || '-' }}</td>
              <td>{{ medicine.descrizione || '-' }}</td>
              <td>{{ medicine.codiceAtc || '-' }}</td>
              <td>{{ medicine.ragioneSociale || '-' }}</td>
              <td class="table-actions">
                <button type="button" class="secondary-btn" (click)="openView(medicine.id!)">{{ t('search.action.view') }}</button>
                <button type="button" class="primary-btn" (click)="openEdit(medicine.id!)">{{ t('search.action.edit') }}</button>
                <button type="button" class="danger-btn" (click)="delete(medicine.id!)">{{ t('search.action.delete') }}</button>
              </td>
            </tr>
          </tbody>
        </table>

        <ng-template #emptyState>
          <p class="empty-state">{{ t('medicines.search.noResults') }}</p>
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
      .table-actions { display: flex; gap: 8px; flex-wrap: wrap; }
      .primary-btn, .secondary-btn, .danger-btn { border: 0; border-radius: 10px; padding: 10px 14px; cursor: pointer; }
      .primary-btn { background: #2f67c7; color: #fff; }
      .secondary-btn { background: #e8edf6; color: #142033; }
      .danger-btn { background: #d92d20; color: #fff; }
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
export class MedicinesSearchComponent implements OnInit, OnDestroy {
  filters: MedicineSearchFilters = {
    codiceAic: '',
    codFarmaco: '',
    codConfezione: '',
    denominazione: '',
    descrizione: '',
    codiceAtc: '',
    ragioneSociale: '',
    statoAmministrativo: ''
  };

  results: MedicineDto[] = [];
  message = '';
  messageType: 'success' | 'error' = 'success';
  translations: Record<string, string> = {};
  private messageTimeoutId: number | null = null;
  private readonly subscriptions = new Subscription();

  constructor(
    private readonly medicineApiService: MedicineApiService,
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
    this.subscriptions.add(
      this.medicineApiService.searchMedicines(this.filters).subscribe({
        next: (results) => {
          this.results = results;
          if (showFeedback) {
            this.showMessage(this.t('search.success.completed'));
          }
        },
        error: (error: HttpErrorResponse) => {
          this.showMessage(this.extractErrorMessage(error, 'medicines.error.search'), 'error');
        }
      })
    );
  }

  reset(): void {
    this.filters = {
      codiceAic: '',
      codFarmaco: '',
      codConfezione: '',
      denominazione: '',
      descrizione: '',
      codiceAtc: '',
      ragioneSociale: '',
      statoAmministrativo: ''
    };
    this.search(false);
  }

  openNew(): void {
    void this.router.navigateByUrl('/medicines/new');
  }

  openEdit(id: number): void {
    void this.router.navigate(['/medicines', id]);
  }

  openView(id: number): void {
    void this.router.navigate(['/medicines', id, 'view']);
  }

  delete(id: number): void {
    if (!window.confirm(this.t('medicines.search.confirmDelete'))) {
      return;
    }

    this.subscriptions.add(
      this.medicineApiService.deleteMedicine(id).subscribe({
        next: () => {
          this.results = this.results.filter((medicine) => medicine.id !== id);
          this.showMessage(this.t('medicines.success.delete'));
        },
        error: (error: HttpErrorResponse) => {
          this.showMessage(this.extractErrorMessage(error, 'medicines.error.delete'), 'error');
        }
      })
    );
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