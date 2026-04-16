import { Component, OnInit } from '@angular/core';
import { CommonModule, NgIf } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { AuthService } from '../../../core/auth.service';
import { I18nPropertiesService } from '../../../core/i18n-properties.service';

@Component({
	selector: 'app-passwordrecover',
	standalone: true,
	imports: [CommonModule, NgIf, ReactiveFormsModule, RouterModule],
	templateUrl: './passwordrecover.component.html',
	styleUrls: ['./passwordrecover.component.css']
})
export class PasswordRecoverComponent implements OnInit {
	errorMessage = '';
	infoMessage = '';
	isSubmitting = false;
	translations: Record<string, string> = {};
	readonly recoverForm;

	constructor(
		private readonly formBuilder: FormBuilder,
		private readonly authService: AuthService,
		private readonly i18nPropertiesService: I18nPropertiesService
	) {
		this.recoverForm = this.formBuilder.nonNullable.group({
			email: ['', [Validators.required, Validators.email]]
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
		if (this.recoverForm.invalid) {
			this.infoMessage = '';
			this.errorMessage = this.t('passwordRecover.error.invalidEmail');
			this.recoverForm.markAllAsTouched();
			return;
		}

		const { email } = this.recoverForm.getRawValue();
		const normalizedEmail = email.trim();

		if (!normalizedEmail) {
			this.infoMessage = '';
			this.errorMessage = this.t('passwordRecover.error.invalidEmail');
			return;
		}

		this.isSubmitting = true;
		this.errorMessage = '';
		this.infoMessage = '';

		this.authService.passwordRecover({ email: normalizedEmail }).subscribe({
			next: (response) => {
				this.infoMessage = response.message || this.t('passwordRecover.info.sent');
				this.isSubmitting = false;
			},
			error: () => {
				this.errorMessage = this.t('passwordRecover.error.generic');
				this.isSubmitting = false;
			}
		});
	}
}