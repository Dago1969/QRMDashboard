import { CommonModule } from '@angular/common';
import { Component, ViewChild } from '@angular/core';
import { Observable } from 'rxjs';
import { PatientApiService, PatientDto } from '../../core/patient-api.service';
import {
  SearchFilterField,
  SearchPageActionEvent,
  SearchPageComponent,
  SearchResultColumn
} from '../../shared/search-page.component';
import { PatientsWizardComponent } from '../patients/patients-wizard.component';

/**
 * Gestione pazienti tramite search-page centralizzata e wizard popup in stile TENAPP.
 */
@Component({
  selector: 'app-patients-search',
  standalone: true,
  imports: [CommonModule, SearchPageComponent, PatientsWizardComponent],
  template: `
    <app-search-page
      #searchPage
      [titleKey]="'patients.search.title'"
      [subtitleKey]="'patients.search.subtitle'"
      [emptyStateKey]="'patients.search.noResults'"
      [createActionLabelKey]="'patients.actions.new'"
      [filters]="searchFilters"
      [columns]="searchColumns"
      [fetchResults]="fetchPatients"
      [showDeleteAction]="false"
      (createAction)="openCreate()"
      (editAction)="openEdit($event)"
      (viewAction)="openView($event)"
    />

    <app-patients-wizard
      *ngIf="wizardOpen"
      [patientIdInput]="selectedPatientId"
      [modeInput]="wizardMode"
      (saved)="onSaved()"
      (cancelled)="closeWizard()"
    />
  `
})
export class PatientsSearchComponent {
  @ViewChild('searchPage') private searchPage?: SearchPageComponent<PatientDto>;

  readonly searchFilters: SearchFilterField[] = [
    { key: 'assistedId', labelKey: 'patients.field.assistedId', type: 'text' },
    { key: 'firstName', labelKey: 'patients.field.firstName', type: 'text' },
    { key: 'lastName', labelKey: 'patients.field.lastName', type: 'text' },
    {
      key: 'gender',
      labelKey: 'patients.field.gender',
      type: 'select',
      options: [
        { value: 'M', labelKey: 'patients.gender.male' },
        { value: 'F', labelKey: 'patients.gender.female' }
      ]
    },
    { key: 'birthDate', labelKey: 'patients.field.birthDate', type: 'date' },
    { key: 'email', labelKey: 'patients.field.email', type: 'text' },
    { key: 'fiscalCode', labelKey: 'patients.field.fiscalCode', type: 'text' },
    { key: 'structureId', labelKey: 'patients.field.structureId', type: 'number' }
  ];

  readonly searchColumns: SearchResultColumn<PatientDto>[] = [
    { key: 'assistedId', labelKey: 'patients.field.assistedId' },
    { key: 'firstName', labelKey: 'patients.field.firstName' },
    { key: 'lastName', labelKey: 'patients.field.lastName' },
    { key: 'gender', labelKey: 'patients.field.gender', formatter: (row) => this.formatGender(row.gender) },
    { key: 'birthDate', labelKey: 'patients.field.birthDate', formatter: (row) => row.birthDate || '-' },
    { key: 'fiscalCode', labelKey: 'patients.field.fiscalCode' },
    { key: 'email', labelKey: 'patients.field.email' }
  ];

  readonly fetchPatients = (filters: Record<string, string>): Observable<PatientDto[]> => this.patientApiService.searchPatients(filters);

  wizardOpen = false;
  wizardMode: 'create' | 'edit' | 'view' = 'create';
  selectedPatientId: number | null = null;

  constructor(private readonly patientApiService: PatientApiService) {}

  openCreate(): void {
    this.selectedPatientId = null;
    this.wizardMode = 'create';
    this.wizardOpen = true;
  }

  openEdit(event: SearchPageActionEvent<PatientDto>): void {
    this.selectedPatientId = Number(event.id);
    this.wizardMode = 'edit';
    this.wizardOpen = true;
  }

  openView(event: SearchPageActionEvent<PatientDto>): void {
    this.selectedPatientId = Number(event.id);
    this.wizardMode = 'view';
    this.wizardOpen = true;
  }

  closeWizard(): void {
    this.wizardOpen = false;
    this.selectedPatientId = null;
  }

  onSaved(): void {
    const successKey = this.wizardMode === 'create' ? 'crud.success.create' : 'crud.success.update';
    this.closeWizard();
    this.searchPage?.reload(false);
    this.searchPage?.showExternalMessage(this.searchPage.translate(successKey));
  }

  private formatGender(gender?: string): string {
    if (gender === 'M') {
      return this.searchPage?.translate('patients.gender.male') ?? 'M';
    }
    if (gender === 'F') {
      return this.searchPage?.translate('patients.gender.female') ?? 'F';
    }
    return '-';
  }
}