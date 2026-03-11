import { Component } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { PatientApiService, PatientDto } from './core/patient-api.service';
import { NgIf, NgFor, NgClass } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-patients-tester',
  standalone: true,
  templateUrl: './patients-tester.component.html',
  styleUrls: ['./patients-tester.component.css'],
  providers: [PatientApiService],
  imports: [CommonModule, ReactiveFormsModule, FormsModule, NgIf, NgFor, NgClass],
})
export class PatientsTesterComponent {
  searchForm: FormGroup;
  patients: PatientDto[] = [];
  selectedPatient: PatientDto | null = null;
  error: string | null = null;
  loading = false;

  constructor(
    private fb: FormBuilder,
    private patientApi: PatientApiService
  ) {
    this.searchForm = this.fb.group({
      firstName: [''],
      lastName: [''],
      fiscalCode: ['']
    });
  }

  search() {
    this.loading = true;
    this.error = null;
    this.patientApi.searchPatients(this.searchForm.value).subscribe({
      next: (res: PatientDto[]) => { this.patients = res; this.loading = false; },
      error: (err: any) => { this.error = err?.message || 'Errore'; this.loading = false; }
    });
  }

  selectPatient(p: PatientDto) {
    this.selectedPatient = { ...p };
  }

  startNewPatient() {
    this.selectedPatient = {
      firstName: '',
      lastName: '',
      fiscalCode: '',
      email: '',
      assistedId: '',
      primaryPhone: '',
      secondaryPhone: '',
      region: '',
      province: ''
    };
  }

  clearSelection() {
    this.selectedPatient = null;
  }

  savePatient() {
    if (!this.selectedPatient) return;
    const op = this.selectedPatient.id
      ? this.patientApi.updatePatient(this.selectedPatient.id, this.selectedPatient)
      : this.patientApi.createPatient(this.selectedPatient);
    this.loading = true;
    op.subscribe({
      next: () => { this.search(); this.clearSelection(); },
      error: (err: any) => { this.error = err?.message || 'Errore'; this.loading = false; }
    });
  }

  deletePatient(id: number) {
    if (!confirm('Confermi la cancellazione?')) return;
    this.loading = true;
    this.patientApi.deletePatient(id).subscribe({
      next: () => { this.search(); },
      error: (err: any) => { this.error = err?.message || 'Errore'; this.loading = false; }
    });
  }
}
