import { Routes } from '@angular/router';
import { LoginComponent } from './features/login/login.component';
import { DashboardComponent } from './features/dashboard/dashboard.component';
import { authGuard } from './core/auth.guard';
import { PatientsTesterComponent } from './patients-tester.component';
import { ChangePasswordComponent } from './shared/change-password/change-password.component';

/**
 * Definizione rotte applicative minime: login pubblico e dashboard protetta.
 */
import { PasswordRecoverComponent } from './features/login/passwordrecover/passwordrecover.component';

export const appRoutes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'login' },
  { path: 'login', component: LoginComponent },
  { path: 'change-password', component: ChangePasswordComponent },
  { path: 'passwordrecover', component: PasswordRecoverComponent },
  { path: 'dashboard', component: DashboardComponent, canActivate: [authGuard] },
  { path: 'patients-tester', component: PatientsTesterComponent, canActivate: [authGuard] },
  { path: '**', redirectTo: 'login' }
];
