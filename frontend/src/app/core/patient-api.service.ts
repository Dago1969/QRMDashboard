import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';

// DTO allineato a QTMPatients
export interface PatientDto {
  id?: number;
  assistedId?: string;
  firstName: string;
  lastName: string;
  fiscalCode: string;
  email?: string;
  primaryPhone?: string;
  secondaryPhone?: string;
  regionId?: number;
  region?: string;
  provinceId?: number;
  province?: string;
  // aggiungi altri campi se presenti in QTMPatients
}

/**
 * Service per chiamare i servizi REST di QTMPatients da QTMDashboard.
 * Gestisce tutte le operazioni CRUD e la propagazione del JWT tramite HttpInterceptor.
 */
@Injectable({ providedIn: 'root' })
export class PatientApiService {
  private readonly baseUrl = 'http://localhost:8088/api/patients';

  constructor(private readonly http: HttpClient) {}

  /**
   * Ricerca pazienti con filtri opzionali.
   */
  searchPatients(params?: Partial<PatientDto>): Observable<PatientDto[]> {
    return this.http.get<PatientDto[]>(this.baseUrl, { params: params as any }).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Recupera un paziente per ID.
   */
  getPatient(id: number): Observable<PatientDto> {
    return this.http.get<PatientDto>(`${this.baseUrl}/${id}`).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Crea un nuovo paziente.
   */
  createPatient(patient: PatientDto): Observable<PatientDto> {
    return this.http.post<PatientDto>(this.baseUrl, patient).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Aggiorna un paziente esistente.
   */
  updatePatient(id: number, patient: PatientDto): Observable<PatientDto> {
    return this.http.put<PatientDto>(`${this.baseUrl}/${id}`, patient).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Elimina un paziente per ID.
   */
  deletePatient(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Gestione centralizzata degli errori HTTP.
   */
  private handleError(error: any) {
    // Qui puoi loggare, mostrare toast, ecc.
    return throwError(() => error);
  }
}
