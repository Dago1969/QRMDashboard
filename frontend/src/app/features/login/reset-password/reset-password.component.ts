import { Component, OnInit } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { NgIf } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { I18nPropertiesService } from '../../../core/i18n-properties.service';
import { AuthService } from '../../../core/auth.service';

@Component({
  selector: 'app-reset-password',
  standalone: true,
  imports: [ReactiveFormsModule, NgIf, RouterModule],
  templateUrl: './reset-password.component.html',
  styleUrl: './reset-password.component.css'
})
export class ResetPasswordComponent implements OnInit {
  errorMessage = '';
  successMessage = '';
  translations: Record<string, string> = {};
  token = '';
  readonly form;

  constructor(
    private readonly formBuilder: FormBuilder,
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly i18nPropertiesService: I18nPropertiesService,
    private readonly authService: AuthService
  ) {
    this.form = this.formBuilder.nonNullable.group({
      newPassword: ['', [Validators.required, Validators.minLength(8)]],
      confirmPassword: ['', [Validators.required]]
    });
  }

  ngOnInit(): void {
    this.token = this.route.snapshot.queryParamMap.get('token') ?? '';
    this.i18nPropertiesService.loadTranslations(navigator.language).subscribe({
      next: (translationMap: Record<string, string>) => {
        this.translations = translationMap;
      }
    });
  }

  t(key: string): string {
    return this.translations[key] ?? key;
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.errorMessage = this.t('resetPassword.error.requiredFields');
      return;
    }
    const { newPassword, confirmPassword } = this.form.getRawValue();
    if (newPassword !== confirmPassword) {
      this.errorMessage = this.t('resetPassword.error.passwordMismatch');
      return;
    }
    if (!this.token) {
      this.errorMessage = this.t('resetPassword.error.missingToken');
      return;
    }
    this.errorMessage = '';
    this.successMessage = '';
    this.authService.resetPassword({ token: this.token, newPassword }).subscribe({
      next: () => {
        this.successMessage = this.t('resetPassword.success');
        setTimeout(() => this.router.navigate(['/login'], { queryParams: { passwordChanged: 'true' } }), 2000);
      },
      error: (error: HttpErrorResponse) => {
        this.errorMessage = error?.error?.detail || this.t('resetPassword.error.generic');
      }
    });
  }
}
