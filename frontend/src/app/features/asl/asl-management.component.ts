import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { I18nPropertiesService } from '../../core/i18n-properties.service';

interface AslRecord {
  id: number;
  codiceAzienda: string;
  denominazioneAzienda: string;
  indirizzo?: string;
  telefono?: string;
  email?: string;
  imported: boolean;
  note?: string | null;
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
        <div class="form-row">
          <label>{{ t('asl.filter.imported') }}</label>
          <select [(ngModel)]="filters.imported">
            <option value="all">{{ t('asl.filter.status.all') }}</option>
            <option value="imported">{{ t('asl.filter.status.imported') }}</option>
            <option value="notImported">{{ t('asl.filter.status.notImported') }}</option>
          </select>
        </div>
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
              <th>{{ t('asl.column.address') }}</th>
              <th>{{ t('asl.column.email') }}</th>
              <th>{{ t('asl.column.phone') }}</th>
              <th>{{ t('asl.column.imported') }}</th>
              <th>{{ t('asl.column.note') }}</th>
              <th>{{ t('search.actions') }}</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let asl of filteredRecords()">
              <td>{{ asl.id }}</td>
              <td>{{ asl.codiceAzienda }}</td>
              <td>{{ asl.denominazioneAzienda }}</td>
              <td>{{ asl.indirizzo || '-' }}</td>
              <td>{{ asl.email || '-' }}</td>
              <td>{{ asl.telefono || '-' }}</td>
              <td><input type="checkbox" [checked]="asl.imported" disabled /></td>
              <td>{{ asl.note || '-' }}</td>
              <td>
                <button class="btn btn-primary btn-sm" type="button" (click)="importRow(asl)" *ngIf="!asl.imported">
                  {{ t('asl.action.importRow') }}
                </button>
                <button class="btn btn-secondary btn-sm" type="button" (click)="selectRow(asl)" *ngIf="asl.imported">
                  {{ t('asl.action.edit') }}
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div *ngIf="selectedAsl" class="card" style="padding: 1rem; margin-top: 1rem;">
        <h3>{{ t('asl.details.title') }}</h3>
        <div class="form-row">
          <label>{{ t('asl.field.id') }}</label>
          <input type="number" [value]="selectedAsl.id" disabled />
        </div>
        <div class="form-row">
          <label>{{ t('asl.field.name') }}</label>
          <input [value]="selectedAsl.denominazioneAzienda" disabled />
        </div>
        <div class="form-row">
          <label>{{ t('asl.field.note') }}</label>
          <textarea [(ngModel)]="selectedAsl.note"></textarea>
        </div>
        <button class="btn btn-primary" type="button" (click)="saveSelected()">{{ t('asl.action.saveNote') }}</button>
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
  selectedAsl: AslRecord | null = null;
  selectedId: number | null = null;
  translations: Record<string, string> = {};
  message = '';
  messageType: 'success' | 'error' = 'success';

  constructor(
    private readonly http: HttpClient,
    private readonly i18nPropertiesService: I18nPropertiesService
  ) {}

  ngOnInit(): void {
    this.i18nPropertiesService.loadTranslations(navigator.language).subscribe((translations) => {
      this.translations = translations;
      this.loadOverview();
    });
  }

  t(key: string): string {
    return this.translations[key] ?? key;
  }

  loadOverview(): void {
    this.http.get<AslRecord[]>('/api/asl/overview').subscribe({
      next: (records) => {
        this.allAslRecords = records;
      },
      error: () => {
        this.showMessage('asl.messages.loadError', 'error');
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
    this.http.post<AslRecord[]>('/api/asl/import', { sourceIds: [asl.id] }).subscribe({
      next: () => {
        this.showMessage('asl.messages.importSuccess', 'success');
        this.loadOverview();
      },
      error: () => {
        this.showMessage('asl.messages.importError', 'error');
      }
    });
  }

  selectRow(asl: AslRecord): void {
    this.selectedAsl = { ...asl };
  }

  saveSelected(): void {
    if (!this.selectedAsl) {
      return;
    }

    const currentAsl = this.selectedAsl;

    this.http.put<AslRecord>(`/api/asl/${currentAsl.id}`, { id: currentAsl.id, note: currentAsl.note }).subscribe({
      next: (updated) => {
        this.showMessage('asl.messages.noteSaved', 'success');
        this.selectedAsl = { ...currentAsl, note: updated.note ?? null };
        this.allAslRecords = this.allAslRecords.map((asl) =>
          asl.id === updated.id ? { ...asl, note: updated.note ?? null } : asl
        );
      },
      error: () => {
        this.showMessage('asl.messages.noteSaveError', 'error');
      }
    });
  }

  loadSelected(): void {
    if (this.selectedId == null) {
      return;
    }

    this.http.get<AslRecord>(`/api/asl/${this.selectedId}`).subscribe({
      next: (value) => {
        this.selectedAsl = value;
      },
      error: () => {
        this.showMessage('asl.messages.loadError', 'error');
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
}
