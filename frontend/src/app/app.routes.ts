import { Routes } from '@angular/router';
import { LoginComponent } from './features/login/login.component';
import { DashboardComponent } from './features/dashboard/dashboard.component';
import { authGuard } from './core/auth.guard';
import { PatientsTesterComponent } from './patients-tester.component';
import { ChangePasswordComponent } from './shared/change-password/change-password.component';
import { PatientsSearchComponent } from './features/patients-search/patients-search.component';
import { PatientsCrudComponent } from './features/patients/patients-crud.component';

/**
 * Definizione rotte applicative minime: login pubblico e dashboard protetta.
 */

import { PasswordRecoverComponent } from './features/login/passwordrecover/passwordrecover.component';
import { ResetPasswordComponent } from './features/login/reset-password/reset-password.component';

export const appRoutes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'login' },
  { path: 'login', component: LoginComponent },
  { path: 'change-password', component: ChangePasswordComponent },
  { path: 'passwordrecover', component: PasswordRecoverComponent },
  { path: 'reset-password', component: ResetPasswordComponent },
  { path: 'dashboard', component: DashboardComponent, canActivate: [authGuard] },
  { path: 'patients/search', component: PatientsSearchComponent, canActivate: [authGuard] },
  { path: 'patients/new', component: PatientsCrudComponent, canActivate: [authGuard] },
  { path: 'patients/:id', component: PatientsCrudComponent, canActivate: [authGuard] },
  { path: 'patients/:id/view', component: PatientsCrudComponent, canActivate: [authGuard], data: { mode: 'view' } },
  { path: 'patients-tester', component: PatientsTesterComponent, canActivate: [authGuard] },
  { path: '**', redirectTo: 'login' }
];
