import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { AuthService } from '../../auth/auth.service';
import { zxcvbn } from '@zxcvbn-ts/core';

@Component({
  selector: 'app-register',
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './register.html',
  styleUrl: './register.css'
})
export class Register {

  showPw = false;
  showConfirm = false;
  get strengthLabel(): string {
    return ['Very weak', 'Weak', 'OK', 'Strong', 'Very strong'][this.strength];
  }

  private fb = inject(FormBuilder);
  private auth = inject(AuthService);

  form = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    firstName: ['', Validators.required],
    lastName: ['', Validators.required],
    organization: ['', Validators.required],
    password: ['', [Validators.required, Validators.minLength(12)]],
    confirmPassword: ['', Validators.required],
  }, { validators: Register.passwordsMatch });

  strength = 0;
  tips: string[] = [];

  onPasswordInput() {
    const pw = this.form.get('password')?.value ?? '';
    const res = zxcvbn(pw);
    this.strength = res.score;
    this.tips = res.feedback?.suggestions ?? [];
  }

  private static passwordsMatch(group: AbstractControl): ValidationErrors | null {
    const pw = group.get('password')?.value ?? '';
    const cf = group.get('confirmPassword')?.value ?? '';
    return pw && cf && pw !== cf ? { passwordMismatch: true } : null;
  }

  submitting = false;
  successMsg = '';
  errorMsg = '';

  submit() {
    this.successMsg = '';
    this.errorMsg = '';
    if (this.form.invalid || this.form.hasError('passwordMismatch')) {
      this.form.markAllAsTouched();
      return;
    }
    this.submitting = true;
    this.auth.register(this.form.value as any).subscribe({
      next: () => {
        this.submitting = false;
        this.successMsg = '✅ Registration successful. Check email for activation link.';
        this.form.disable(); // spreči dupli submit
      },
      error: (err) => {
        this.submitting = false;
        this.errorMsg = err?.error?.message || 'Error occured during registration. Please try again.';
      }
    });
  }
}
