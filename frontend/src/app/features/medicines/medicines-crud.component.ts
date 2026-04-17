import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { I18nPropertiesService } from '../../core/i18n-properties.service';
import { MedicineApiService, MedicineDto } from '../../core/medicine-api.service';

const AUTO_DISMISS_DELAY_MS = 4000;

type FieldType = 'text' | 'url';

interface MedicineFormModel {
  id?: number;
  codiceAic: string;
  codFarmaco: string;
  codConfezione: string;
  denominazione: string;
  descrizione: string;
  codiceDitta: string;
  ragioneSociale: string;
  statoAmministrativo: string;
  tipoProcedura: string;
  forma: string;
  codiceAtc: string;
  paAssociati: string;
  fornitura: string;
  linkFi: string;
  linkRcp: string;
}

interface FormField {
  key: keyof MedicineFormModel;
  labelKey: string;
  type: FieldType;
}

interface FormFolder {
  key: string;
  titleKey: string;
  fields: FormField[];
}

/**
 * Form farmaco per creazione, modifica e consultazione dentro QTMDB.
 */
@Component({
  selector: 'app-medicines-crud',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <section class="page-card">
      <header class="page-header">
        <div>
          <h2>{{ t(pageTitleKey) }}</h2>
          <p *ngIf="loading">{{ t('crud.loading') }}</p>
        </div>
      </header>

      <div *ngIf="message" class="message-box" [class.message-box-error]="messageType === 'error'">
        {{ message }}
      </div>

      <div class="tabs">
        <button
          *ngFor="let folder of folders"
          type="button"
          class="tab-btn"
          [class.active]="activeFolder === folder.key"
          (click)="activeFolder = folder.key"
        >
          {{ t(folder.titleKey) }}
        </button>
      </div>

      <form class="form-grid" (ngSubmit)="save()">
        <label *ngFor="let field of activeFields">
          <span>{{ t(field.labelKey) }}</span>

          <input
            [type]="field.type"
            [(ngModel)]="model[field.key]"
            [name]="getFieldName(field)"
            [readonly]="isViewMode"
            [disabled]="isViewMode"
          />
        </label>

        <div class="actions-row">
          <button *ngIf="!isViewMode" type="submit" class="primary-btn">
            {{ t(isEditMode ? 'crud.actions.update' : 'crud.actions.create') }}
          </button>
          <button type="button" class="secondary-btn" (click)="goBack()">
            {{ t(isViewMode ? 'crud.actions.back' : 'crud.actions.cancel') }}
          </button>
        </div>
      </form>
    </section>
  `,
  styles: [
    `
      .page-card { background: #fff; border-radius: 18px; padding: 24px; box-shadow: 0 16px 40px rgba(15, 23, 42, 0.08); }
      .page-header h2 { margin: 0 0 4px; }
      .page-header p { margin: 0; color: #5b687a; }
      .tabs { display: flex; flex-wrap: wrap; gap: 10px; margin: 20px 0; }
      .tab-btn { border: 0; border-radius: 999px; padding: 10px 14px; background: #e9eef7; color: #17315b; cursor: pointer; }
      .tab-btn.active { background: #2f67c7; color: #fff; }
      .form-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(240px, 1fr)); gap: 16px; }
      .form-grid label { display: grid; gap: 6px; font-weight: 600; }
      .form-grid input { border: 1px solid #d6deea; border-radius: 10px; padding: 10px 12px; }
      .actions-row { grid-column: 1 / -1; display: flex; gap: 10px; margin-top: 8px; }
      .primary-btn, .secondary-btn { border: 0; border-radius: 10px; padding: 10px 14px; cursor: pointer; }
      .primary-btn { background: #2f67c7; color: #fff; }
      .secondary-btn { background: #e8edf6; color: #142033; }
      .message-box { margin-bottom: 16px; padding: 12px 14px; border-radius: 10px; background: #e9f7ef; color: #1d6b3b; }
      .message-box-error { background: #fdecec; color: #b42318; }
    `
  ]
})
export class MedicinesCrudComponent implements OnInit, OnDestroy {
  readonly folders: FormFolder[] = [
    {
      key: 'identity',
      titleKey: 'medicines.folder.identity',
      fields: [
        { key: 'codiceAic', labelKey: 'medicines.field.codiceAic', type: 'text' },
        { key: 'codFarmaco', labelKey: 'medicines.field.codFarmaco', type: 'text' },
        { key: 'codConfezione', labelKey: 'medicines.field.codConfezione', type: 'text' },
        { key: 'denominazione', labelKey: 'medicines.field.denominazione', type: 'text' },
        { key: 'descrizione', labelKey: 'medicines.field.descrizione', type: 'text' },
        { key: 'forma', labelKey: 'medicines.field.forma', type: 'text' },
        { key: 'codiceAtc', labelKey: 'medicines.field.codiceAtc', type: 'text' }
      ]
    },
    {
      key: 'company',
      titleKey: 'medicines.folder.company',
      fields: [
        { key: 'codiceDitta', labelKey: 'medicines.field.codiceDitta', type: 'text' },
        { key: 'ragioneSociale', labelKey: 'medicines.field.ragioneSociale', type: 'text' },
        { key: 'statoAmministrativo', labelKey: 'medicines.field.statoAmministrativo', type: 'text' },
        { key: 'tipoProcedura', labelKey: 'medicines.field.tipoProcedura', type: 'text' },
        { key: 'paAssociati', labelKey: 'medicines.field.paAssociati', type: 'text' },
        { key: 'fornitura', labelKey: 'medicines.field.fornitura', type: 'text' }
      ]
    },
    {
      key: 'links',
      titleKey: 'medicines.folder.links',
      fields: [
        { key: 'linkFi', labelKey: 'medicines.field.linkFi', type: 'url' },
        { key: 'linkRcp', labelKey: 'medicines.field.linkRcp', type: 'url' }
      ]
    }
  ];

  activeFolder = 'identity';
  model: MedicineFormModel = this.createEmptyModel();
  loading = false;
  isViewMode = false;
  isEditMode = false;
  medicineId: number | null = null;
  pageTitleKey = 'medicines.title.new';
  message = '';
  messageType: 'success' | 'error' = 'success';
  translations: Record<string, string> = {};
  private messageTimeoutId: number | null = null;
  private readonly subscriptions = new Subscription();

  constructor(
    private readonly medicineApiService: MedicineApiService,
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly i18nPropertiesService: I18nPropertiesService
  ) {}

  ngOnInit(): void {
    this.subscriptions.add(
      this.i18nPropertiesService.loadTranslations(navigator.language).subscribe({
        next: (translationMap) => {
          this.translations = translationMap;
          const mode = this.route.snapshot.data['mode'];
          this.isViewMode = mode === 'view';

          const idParam = this.route.snapshot.paramMap.get('id');
          if (!idParam) {
            this.pageTitleKey = 'medicines.title.new';
            return;
          }

          this.medicineId = Number(idParam);
          this.isEditMode = true;
          this.pageTitleKey = this.isViewMode ? 'medicines.title.view' : 'medicines.title.edit';
          this.loadMedicine(this.medicineId);
        }
      })
    );
  }

  ngOnDestroy(): void {
    this.clearMessageTimer();
    this.subscriptions.unsubscribe();
  }

  get activeFields(): FormField[] {
    return this.folders.find((folder) => folder.key === this.activeFolder)?.fields ?? [];
  }

  t(key: string): string {
    return this.translations[key] ?? key;
  }

  getFieldName(field: FormField): string {
    return String(field.key);
  }

  save(): void {
    const payload = this.toPayload();
    const request = this.medicineId === null
      ? this.medicineApiService.createMedicine(payload)
      : this.medicineApiService.updateMedicine(this.medicineId, payload);

    this.subscriptions.add(
      request.subscribe({
        next: () => {
          void this.router.navigate(['/medicines/search'], {
            state: {
              flashMessage: this.t(this.medicineId === null ? 'medicines.success.create' : 'medicines.success.update'),
              flashMessageType: 'success'
            }
          });
        },
        error: (error: HttpErrorResponse) => {
          this.showMessage(this.extractErrorMessage(error, 'medicines.error.save'), 'error');
        }
      })
    );
  }

  goBack(): void {
    void this.router.navigateByUrl('/medicines/search');
  }

  private loadMedicine(id: number): void {
    this.loading = true;
    this.subscriptions.add(
      this.medicineApiService.getMedicine(id).subscribe({
        next: (medicine) => {
          this.model = {
            ...this.createEmptyModel(),
            ...medicine
          };
          this.loading = false;
          this.clearMessage();
        },
        error: (error: HttpErrorResponse) => {
          this.loading = false;
          this.showMessage(this.extractErrorMessage(error, 'medicines.error.load'), 'error');
        }
      })
    );
  }

  private toPayload(): MedicineDto {
    return {
      codiceAic: this.model.codiceAic,
      codFarmaco: this.model.codFarmaco,
      codConfezione: this.model.codConfezione,
      denominazione: this.model.denominazione,
      descrizione: this.model.descrizione || undefined,
      codiceDitta: this.model.codiceDitta || undefined,
      ragioneSociale: this.model.ragioneSociale || undefined,
      statoAmministrativo: this.model.statoAmministrativo || undefined,
      tipoProcedura: this.model.tipoProcedura || undefined,
      forma: this.model.forma || undefined,
      codiceAtc: this.model.codiceAtc || undefined,
      paAssociati: this.model.paAssociati || undefined,
      fornitura: this.model.fornitura || undefined,
      linkFi: this.model.linkFi || undefined,
      linkRcp: this.model.linkRcp || undefined
    };
  }

  private createEmptyModel(): MedicineFormModel {
    return {
      codiceAic: '',
      codFarmaco: '',
      codConfezione: '',
      denominazione: '',
      descrizione: '',
      codiceDitta: '',
      ragioneSociale: '',
      statoAmministrativo: '',
      tipoProcedura: '',
      forma: '',
      codiceAtc: '',
      paAssociati: '',
      fornitura: '',
      linkFi: '',
      linkRcp: ''
    };
  }

  private extractErrorMessage(error: HttpErrorResponse, fallbackKey: string): string {
    const detail = error.error?.detail;
    return typeof detail === 'string' && detail.length > 0 ? detail : this.t(fallbackKey);
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
  }

  private clearMessageTimer(): void {
    if (this.messageTimeoutId !== null) {
      window.clearTimeout(this.messageTimeoutId);
      this.messageTimeoutId = null;
    }
  }
}