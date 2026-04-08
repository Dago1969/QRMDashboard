import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { AuthService, ChangePasswordRequest } from '../core/auth.service';

@Injectable({
  providedIn: 'root'
})
export class ChangePasswordService {

  constructor(private readonly authService: AuthService) { }

  /**
   * Inoltra la richiesta di cambio password al backend applicativo.
   */
  changePassword(request: ChangePasswordRequest): Observable<void> {
    return this.authService.changePassword(request);
  }
}
