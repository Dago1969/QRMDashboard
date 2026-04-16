import { NgIf } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { I18nPropertiesService } from '../../core/i18n-properties.service';
import { ChangePasswordService } from '../change-password.service';

@Component({
  selector: 'app-change-password',
  standalone: true,
  imports: [ReactiveFormsModule, NgIf, RouterModule],
  templateUrl: './change-password.component.html',
  styleUrl: './change-password.component.css'
})
export class ChangePasswordComponent implements OnInit {
  private static readonly robustPasswordPattern = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z\d\s])\S{8,}$/;

  errorMessage = '';
  successMessage = '';
  translations: Record<string, string> = {};
  private username = '';

  readonly form;

  constructor(
    private readonly formBuilder: FormBuilder,
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly i18nPropertiesService: I18nPropertiesService,
    private readonly changePasswordService: ChangePasswordService
  ) {
    this.form = this.formBuilder.nonNullable.group({
      currentPassword: ['', [Validators.required]],
      newPassword: ['', [Validators.required, Validators.pattern(ChangePasswordComponent.robustPasswordPattern)]],
      confirmPassword: ['', [Validators.required]]
    });
  }

  ngOnInit(): void {
    this.username = this.route.snapshot.queryParamMap.get('username')?.trim() ?? '';
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
    if (!this.username) {
      this.errorMessage = this.t('changePassword.error.missingUsername');
      return;
    }

    if (this.form.invalid) {
      this.errorMessage = this.t('changePassword.error.requiredFields');
      return;
    }

    const { currentPassword, newPassword, confirmPassword } = this.form.getRawValue();
    if (newPassword !== confirmPassword) {
      this.errorMessage = this.t('changePassword.error.passwordMismatch');
      return;
    }

    if (!ChangePasswordComponent.robustPasswordPattern.test(newPassword) || newPassword.toLowerCase().includes('password')) {
      this.errorMessage = this.t('changePassword.error.passwordWeak');
      return;
    }

    this.errorMessage = '';
    this.successMessage = '';

    this.changePasswordService.changePassword({
      username: this.username,
      currentPassword,
      newPassword,
      confirmPassword
    }).subscribe({
      next: () => {
        this.router.navigate(['/login'], { queryParams: { passwordChanged: 'true' } });
      },
      error: (error) => {
        this.errorMessage = error?.error?.detail || this.t('changePassword.error.generic');
      }
    });
  }

  backToLogin(): void {
    this.router.navigate(['/login']);
  }

}
