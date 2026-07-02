import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { I18nPropertiesService } from '../../core/i18n-properties.service';

interface ProblemDetailPayload {
  detail?: string;
  message?: string;
}

interface HospitalRecord {
  id: number;
  codiceRegione?: string;
  codiceAsl?: string;
  codiceStruttura?: string;
  struttura?: string;
  indirizzo?: string;
  hospitalTypeId?: number;
  aslId?: number;
  imported: boolean;
  note?: string | null;
}

@Component({
  selector: 'app-hospital-management',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="card dashboard-content-card">
      <div class="dashboard-header">
        <h2>{{ t('dashboard.menu.hospital') }}</h2>
      </div>

      <p class="dashboard-selection-info">{{ t('hospital.management.subtitle') }}</p>

      <div class="card" style="padding: 1rem; margin-bottom: 1rem;">
        <div class="form-row">
          <label>{{ t('hospital.filter.id') }}</label>
          <input type="number" [(ngModel)]="filters.id" />
        </div>
        <div class="form-row">
          <label>{{ t('hospital.filter.code') }}</label>
          <input type="text" [(ngModel)]="filters.code" />
        </div>
        <div class="form-row">
          <label>{{ t('hospital.filter.name') }}</label>
          <input type="text" [(ngModel)]="filters.name" />
        </div>
        <div class="form-row">
          <label>{{ t('hospital.filter.imported') }}</label>
          <select [(ngModel)]="filters.imported">
            <option value="all">{{ t('hospital.filter.status.all') }}</option>
            <option value="imported">{{ t('hospital.filter.status.imported') }}</option>
            <option value="notImported">{{ t('hospital.filter.status.notImported') }}</option>
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
              <th>{{ t('hospital.column.id') }}</th>
              <th>{{ t('hospital.column.regionCode') }}</th>
              <th>{{ t('hospital.column.asl') }}</th>
              <th>{{ t('hospital.column.code') }}</th>
              <th>{{ t('hospital.column.name') }}</th>
              <th>{{ t('hospital.column.address') }}</th>
              <th>{{ t('search.actions') }}</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let hospital of filteredRecords()">
              <td>{{ hospital.id }}</td>
              <td>{{ hospital.codiceRegione || '-' }}</td>
              <td>{{ hospital.codiceAsl || '-' }}</td>
              <td>{{ hospital.codiceStruttura || '-' }}</td>
              <td>{{ hospital.struttura || '-' }}</td>
              <td>{{ hospital.indirizzo || '-' }}</td>
              <td>
                <button class="btn btn-primary btn-sm" type="button" (click)="importRow(hospital)" *ngIf="!hospital.imported">
                  {{ t('hospital.action.importRow') }}
                </button>
                <button class="btn btn-secondary btn-sm" type="button" (click)="disassociateRow(hospital)" *ngIf="hospital.imported">
                  {{ t('hospital.action.disassociateRow') }}
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  `
})
export class HospitalManagementComponent implements OnInit {
  filters = {
    id: '' as string,
    code: '' as string,
    name: '' as string,
    imported: 'all' as 'all' | 'imported' | 'notImported'
  };
  allHospitalRecords: HospitalRecord[] = [];
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
    this.http.get<HospitalRecord[]>('/api/hospital/overview').subscribe({
      next: (records: HospitalRecord[]) => {
        this.allHospitalRecords = records;
      },
      error: (error: { error?: ProblemDetailPayload }) => {
        this.showErrorMessage(error, 'hospital.messages.loadError');
      }
    });
  }

  filteredRecords(): HospitalRecord[] {
    return this.allHospitalRecords.filter((hospital) => {
      if (this.filters.id && hospital.id !== Number(this.filters.id)) {
        return false;
      }
      if (this.filters.code && !(hospital.codiceStruttura || '').toLowerCase().includes(this.filters.code.toLowerCase())) {
        return false;
      }
      if (this.filters.name && !(hospital.struttura || '').toLowerCase().includes(this.filters.name.toLowerCase())) {
        return false;
      }
      if (this.filters.imported === 'imported' && !hospital.imported) {
        return false;
      }
      if (this.filters.imported === 'notImported' && hospital.imported) {
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

  importRow(hospital: HospitalRecord): void {
    this.http.post<HospitalRecord[]>('/api/hospital/import', { sourceIds: [hospital.id] }).subscribe({
      next: () => {
        this.showMessage('hospital.messages.associateSuccess', 'success');
        this.loadOverview();
      },
      error: (error: { error?: ProblemDetailPayload }) => {
        this.showErrorMessage(error, 'hospital.messages.associateError');
      }
    });
  }

  disassociateRow(hospital: HospitalRecord): void {
    this.http.delete<void>(`/api/hospital/${hospital.id}`).subscribe({
      next: () => {
        this.showMessage('hospital.messages.disassociateSuccess', 'success');
        this.loadOverview();
      },
      error: (error: { error?: ProblemDetailPayload }) => {
        this.showErrorMessage(error, 'hospital.messages.disassociateError');
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
