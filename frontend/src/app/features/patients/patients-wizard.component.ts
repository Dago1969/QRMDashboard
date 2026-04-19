import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, EventEmitter, Input, OnDestroy, OnInit, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Subscription } from 'rxjs';
import { PatientApiService, PatientDto } from '../../core/patient-api.service';
import { I18nPropertiesService } from '../../core/i18n-properties.service';
import { QtmStepModalComponent } from '../../shared/qtm-step-modal.component';

type FieldType = 'text' | 'number' | 'checkbox' | 'datetime-local' | 'date' | 'select';

interface PatientFormModel {
  assistedId?: string;
  firstName: string;
  lastName: string;
  birthDate: string;
  gender: string;
  fiscalCode: string;
  email: string;
  primaryPhone: string;
  secondaryPhone: string;
  region: string;
  province: string;
  city: string;
  deliveryAddress: string;
  secondaryAddresses: string;
  communicationChannels: string;
  identificationDocumentReference: string;
  dataProcessingConsent: boolean;
  dataProcessingConsentDateTime: string;
  dataProcessingConsentRevocationLog: string;
  additionalConsents: string;
  therapyStatus: string;
  prescribingSpecialist: string;
  referenceHospitalStructure: string;
  referencePharmacy: string;
  preferredPickupPharmacy: string;
  deliveryMode: string;
  reminderEnabled: boolean;
  caregiverFullName: string;
  caregiverPhone: string;
  preferredContact: string;
  structureId: string;
}

interface FormField {
  key: keyof PatientFormModel;
  labelKey: string;
  type: FieldType;
  readonly?: boolean;
  options?: Array<{ value: string; labelKey: string }>;
}

interface FormFolder {
  key: string;
  titleKey: string;
  fields: FormField[];
}

@Component({
  selector: 'app-patients-wizard',
  standalone: true,
  imports: [CommonModule, FormsModule, QtmStepModalComponent],
  template: `
    <qtm-step-modal
      [title]="t(pageTitleKey)"
      [step]="currentStepNumber"
      [totalSteps]="folders.length"
      [stepTitle]="t(activeFolderConfig.titleKey)"
      (close)="cancelled.emit()"
    >
      <div *ngIf="message" class="message-box" [class.message-box-error]="messageType === 'error'">
        {{ message }}
      </div>

      <div class="form-grid">
        <label *ngFor="let field of activeFields">
          <span>{{ t(field.labelKey) }}</span>

          <input
            *ngIf="field.type !== 'checkbox' && field.type !== 'select'"
            [type]="field.type"
            [(ngModel)]="model[field.key]"
            [name]="getFieldName(field)"
            [readonly]="field.readonly || isViewMode"
            [disabled]="field.readonly || isViewMode"
          />

          <select
            *ngIf="field.type === 'select'"
            [(ngModel)]="model[field.key]"
            [name]="getFieldName(field)"
            [disabled]="field.readonly || isViewMode"
          >
            <option value=""></option>
            <option *ngFor="let option of field.options ?? []" [value]="option.value">{{ t(option.labelKey) }}</option>
          </select>

          <input
            *ngIf="field.type === 'checkbox'"
            type="checkbox"
            [(ngModel)]="model[field.key]"
            [name]="getFieldName(field)"
            [disabled]="isViewMode"
            class="checkbox-input"
          />
        </label>
      </div>

      <div modal-actions>
        <button *ngIf="!isFirstStep" type="button" class="secondary-btn" (click)="goToPreviousStep()">
          {{ t('wizard.actions.previous') }}
        </button>
        <button type="button" class="secondary-btn" (click)="cancelled.emit()">
          {{ t(isViewMode ? 'crud.actions.back' : 'crud.actions.cancel') }}
        </button>
        <button *ngIf="!isLastStep" type="button" class="primary-btn" (click)="goToNextStep()">
          {{ t('wizard.actions.next') }}
        </button>
        <button *ngIf="isLastStep && !isViewMode" type="button" class="primary-btn" (click)="save()">
          {{ t(patientIdInput === null ? 'crud.actions.create' : 'crud.actions.update') }}
        </button>
      </div>
    </qtm-step-modal>
  `,
  styles: [
    `
      .form-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(240px, 1fr)); gap: 16px; }
      .form-grid label { display: grid; gap: 6px; font-weight: 600; }
      .form-grid input, .form-grid select { border: 1px solid #d6deea; border-radius: 10px; padding: 10px 12px; }
      .checkbox-input { width: 20px; height: 20px; }
      .primary-btn, .secondary-btn { border: 0; border-radius: 10px; padding: 10px 14px; cursor: pointer; }
      .primary-btn { background: #2f67c7; color: #fff; }
      .secondary-btn { background: #e8edf6; color: #142033; }
      .message-box { margin-bottom: 16px; padding: 12px 14px; border-radius: 10px; background: #e9f7ef; color: #1d6b3b; }
      .message-box-error { background: #fdecec; color: #b42318; }
    `
  ]
})
export class PatientsWizardComponent implements OnInit, OnDestroy {
  @Input() patientIdInput: number | null = null;
  @Input() modeInput: 'create' | 'edit' | 'view' = 'create';
  @Output() readonly saved = new EventEmitter<void>();
  @Output() readonly cancelled = new EventEmitter<void>();

  readonly folders: FormFolder[] = [
    {
      key: 'identity',
      titleKey: 'patients.folder.identity',
      fields: [
        { key: 'assistedId', labelKey: 'patients.field.assistedId', type: 'text', readonly: true },
        { key: 'firstName', labelKey: 'patients.field.firstName', type: 'text' },
        { key: 'lastName', labelKey: 'patients.field.lastName', type: 'text' },
        { key: 'birthDate', labelKey: 'patients.field.birthDate', type: 'date' },
        {
          key: 'gender',
          labelKey: 'patients.field.gender',
          type: 'select',
          options: [
            { value: 'M', labelKey: 'patients.gender.male' },
            { value: 'F', labelKey: 'patients.gender.female' }
          ]
        },
        { key: 'fiscalCode', labelKey: 'patients.field.fiscalCode', type: 'text' },
        { key: 'email', labelKey: 'patients.field.email', type: 'text' },
        { key: 'primaryPhone', labelKey: 'patients.field.primaryPhone', type: 'text' },
        { key: 'secondaryPhone', labelKey: 'patients.field.secondaryPhone', type: 'text' },
        { key: 'region', labelKey: 'patients.field.region', type: 'text' },
        { key: 'province', labelKey: 'patients.field.province', type: 'text' },
        { key: 'city', labelKey: 'patients.field.city', type: 'text' },
        { key: 'deliveryAddress', labelKey: 'patients.field.deliveryAddress', type: 'text' },
        { key: 'secondaryAddresses', labelKey: 'patients.field.secondaryAddresses', type: 'text' },
        { key: 'communicationChannels', labelKey: 'patients.field.communicationChannels', type: 'text' },
        { key: 'identificationDocumentReference', labelKey: 'patients.field.identificationDocumentReference', type: 'text' }
      ]
    },
    {
      key: 'privacy',
      titleKey: 'patients.folder.privacy',
      fields: [
        { key: 'dataProcessingConsent', labelKey: 'patients.field.dataProcessingConsent', type: 'checkbox' },
        { key: 'dataProcessingConsentDateTime', labelKey: 'patients.field.dataProcessingConsentDateTime', type: 'datetime-local' },
        { key: 'dataProcessingConsentRevocationLog', labelKey: 'patients.field.dataProcessingConsentRevocationLog', type: 'text' },
        { key: 'additionalConsents', labelKey: 'patients.field.additionalConsents', type: 'text' }
      ]
    },
    {
      key: 'medical',
      titleKey: 'patients.folder.medical',
      fields: [
        { key: 'therapyStatus', labelKey: 'patients.field.therapyStatus', type: 'text' },
        { key: 'prescribingSpecialist', labelKey: 'patients.field.prescribingSpecialist', type: 'text' },
        { key: 'referenceHospitalStructure', labelKey: 'patients.field.referenceHospitalStructure', type: 'text' },
        { key: 'referencePharmacy', labelKey: 'patients.field.referencePharmacy', type: 'text' },
        { key: 'preferredPickupPharmacy', labelKey: 'patients.field.preferredPickupPharmacy', type: 'text' },
        { key: 'deliveryMode', labelKey: 'patients.field.deliveryMode', type: 'text' },
        { key: 'reminderEnabled', labelKey: 'patients.field.reminderEnabled', type: 'checkbox' },
        { key: 'caregiverFullName', labelKey: 'patients.field.caregiverFullName', type: 'text' },
        { key: 'caregiverPhone', labelKey: 'patients.field.caregiverPhone', type: 'text' },
        { key: 'preferredContact', labelKey: 'patients.field.preferredContact', type: 'text' },
        { key: 'structureId', labelKey: 'patients.field.structureId', type: 'number' }
      ]
    }
  ];

  activeFolder = 'identity';
  model: PatientFormModel = this.createEmptyModel();
  isViewMode = false;
  pageTitleKey = 'patients.title.new';
  message = '';
  messageType: 'success' | 'error' = 'success';
  translations: Record<string, string> = {};

  private readonly subscriptions = new Subscription();

  constructor(
    private readonly patientApiService: PatientApiService,
    private readonly i18nPropertiesService: I18nPropertiesService
  ) {}

  ngOnInit(): void {
    this.subscriptions.add(
      this.i18nPropertiesService.loadTranslations(navigator.language).subscribe({
        next: (translationMap) => {
          this.translations = translationMap;
          this.isViewMode = this.modeInput === 'view';
          this.pageTitleKey = this.isViewMode ? 'patients.title.view' : this.patientIdInput === null ? 'patients.title.new' : 'patients.title.edit';
          if (this.patientIdInput !== null) {
            this.loadPatient(this.patientIdInput);
          }
        }
      })
    );
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

  get activeFields(): FormField[] {
    return this.activeFolderConfig.fields;
  }

  get activeFolderConfig(): FormFolder {
    return this.folders.find((folder) => folder.key === this.activeFolder) ?? this.folders[0];
  }

  get currentStepNumber(): number {
    return Math.max(this.folders.findIndex((folder) => folder.key === this.activeFolder), 0) + 1;
  }

  get isFirstStep(): boolean {
    return this.currentStepNumber === 1;
  }

  get isLastStep(): boolean {
    return this.currentStepNumber === this.folders.length;
  }

  t(key: string): string {
    return this.translations[key] ?? key;
  }

  getFieldName(field: FormField): string {
    return String(field.key);
  }

  goToPreviousStep(): void {
    const previousIndex = Math.max(this.currentStepNumber - 2, 0);
    this.activeFolder = this.folders[previousIndex].key;
  }

  goToNextStep(): void {
    const nextIndex = Math.min(this.currentStepNumber, this.folders.length - 1);
    this.activeFolder = this.folders[nextIndex].key;
  }

  save(): void {
    const payload = this.toPayload();
    const request = this.patientIdInput === null
      ? this.patientApiService.createPatient(payload)
      : this.patientApiService.updatePatient(this.patientIdInput, payload);

    this.subscriptions.add(
      request.subscribe({
        next: () => this.saved.emit(),
        error: (error: HttpErrorResponse) => {
          this.messageType = 'error';
          this.message = this.extractErrorMessage(error, 'crud.error.save');
        }
      })
    );
  }

  private loadPatient(id: number): void {
    this.subscriptions.add(
      this.patientApiService.getPatient(id).subscribe({
        next: (patient) => {
          this.model = {
            ...this.createEmptyModel(),
            ...patient,
            birthDate: typeof patient.birthDate === 'string' ? patient.birthDate.slice(0, 10) : '',
            gender: typeof patient.gender === 'string' ? patient.gender : '',
            dataProcessingConsent: Boolean(patient.dataProcessingConsent),
            reminderEnabled: Boolean(patient.reminderEnabled),
            dataProcessingConsentDateTime: typeof patient.dataProcessingConsentDateTime === 'string'
              ? patient.dataProcessingConsentDateTime.slice(0, 16)
              : '',
            structureId: patient.structureId === undefined || patient.structureId === null ? '' : String(patient.structureId)
          };
        },
        error: (error: HttpErrorResponse) => {
          this.messageType = 'error';
          this.message = this.extractErrorMessage(error, 'crud.error.load');
        }
      })
    );
  }

  private toPayload(): PatientDto {
    return {
      assistedId: this.model.assistedId || undefined,
      firstName: this.model.firstName,
      lastName: this.model.lastName,
      birthDate: this.model.birthDate || undefined,
      gender: this.model.gender || undefined,
      fiscalCode: this.model.fiscalCode,
      email: this.model.email || undefined,
      primaryPhone: this.model.primaryPhone || undefined,
      secondaryPhone: this.model.secondaryPhone || undefined,
      region: this.model.region || undefined,
      province: this.model.province || undefined,
      city: this.model.city || undefined,
      deliveryAddress: this.model.deliveryAddress || undefined,
      secondaryAddresses: this.model.secondaryAddresses || undefined,
      communicationChannels: this.model.communicationChannels || undefined,
      identificationDocumentReference: this.model.identificationDocumentReference || undefined,
      dataProcessingConsent: this.model.dataProcessingConsent,
      dataProcessingConsentDateTime: this.model.dataProcessingConsentDateTime || undefined,
      dataProcessingConsentRevocationLog: this.model.dataProcessingConsentRevocationLog || undefined,
      additionalConsents: this.model.additionalConsents || undefined,
      therapyStatus: this.model.therapyStatus || undefined,
      prescribingSpecialist: this.model.prescribingSpecialist || undefined,
      referenceHospitalStructure: this.model.referenceHospitalStructure || undefined,
      referencePharmacy: this.model.referencePharmacy || undefined,
      preferredPickupPharmacy: this.model.preferredPickupPharmacy || undefined,
      deliveryMode: this.model.deliveryMode || undefined,
      reminderEnabled: this.model.reminderEnabled,
      caregiverFullName: this.model.caregiverFullName || undefined,
      caregiverPhone: this.model.caregiverPhone || undefined,
      preferredContact: this.model.preferredContact || undefined,
      structureId: this.model.structureId ? Number(this.model.structureId) : undefined
    };
  }

  private createEmptyModel(): PatientFormModel {
    return {
      assistedId: '',
      firstName: '',
      lastName: '',
      birthDate: '',
      gender: '',
      fiscalCode: '',
      email: '',
      primaryPhone: '',
      secondaryPhone: '',
      region: '',
      province: '',
      city: '',
      deliveryAddress: '',
      secondaryAddresses: '',
      communicationChannels: '',
      identificationDocumentReference: '',
      dataProcessingConsent: false,
      dataProcessingConsentDateTime: '',
      dataProcessingConsentRevocationLog: '',
      additionalConsents: '',
      therapyStatus: '',
      prescribingSpecialist: '',
      referenceHospitalStructure: '',
      referencePharmacy: '',
      preferredPickupPharmacy: '',
      deliveryMode: '',
      reminderEnabled: false,
      caregiverFullName: '',
      caregiverPhone: '',
      preferredContact: '',
      structureId: ''
    };
  }

  private extractErrorMessage(error: HttpErrorResponse, fallbackKey: string): string {
    const detail = error.error?.detail;
    return typeof detail === 'string' && detail.length > 0 ? detail : this.t(fallbackKey);
  }
}