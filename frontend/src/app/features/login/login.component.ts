import { Component, OnInit } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { NgIf } from '@angular/common';
import { AuthService } from '../../core/auth.service';
import { I18nPropertiesService } from '../../core/i18n-properties.service';

/**
 * Pagina login che invia username/password al backend per autenticazione Keycloak.
 */
@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule, NgIf, RouterModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent implements OnInit {
  errorMessage = '';
  translations: Record<string, string> = {};
  readonly loginForm;

  constructor(
    private readonly formBuilder: FormBuilder,
    private readonly authService: AuthService,
    private readonly router: Router,
    private readonly i18nPropertiesService: I18nPropertiesService
  ) {
    this.loginForm = this.formBuilder.nonNullable.group({
      username: ['francesco.tripodi', [Validators.required]],
      password: ['QTM!2026', [Validators.required]]
    });
  }

  ngOnInit(): void {
    this.i18nPropertiesService.loadTranslations(navigator.language).subscribe({
      next: (translationMap) => {
        this.translations = translationMap;
      }
    });
  }

  t(key: string): string {
    return this.translations[key] ?? key;
  }

  onSubmit(): void {
    if (this.loginForm.invalid) {
      this.errorMessage = this.t('login.error.requiredCredentials');
      return;
    }

    const { username, password } = this.loginForm.getRawValue();
    const normalizedUsername = username.trim();

    if (!normalizedUsername) {
      this.errorMessage = this.t('login.error.requiredCredentials');
      return;
    }

    this.errorMessage = '';

    this.authService.login(normalizedUsername, password).subscribe({
      next: () => {
        this.router.navigate(['/dashboard']);
      },
      error: () => {
        this.errorMessage = this.t('login.error.failedAccess');
      }
    });
  }
}
