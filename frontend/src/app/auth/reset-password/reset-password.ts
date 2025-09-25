import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { AuthService } from '../../auth/auth.service';

@Component({
  selector: 'app-reset-password',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './reset-password.html',
  styleUrl: './reset-password.css'
})
export class ResetPassword {
  private fb = inject(FormBuilder);
  private auth = inject(AuthService);
  private route = inject(ActivatedRoute);

  token = this.route.snapshot.queryParamMap.get('token') ?? '';

  form = this.fb.group({
    newPassword: ['', [Validators.required, Validators.minLength(12)]],
    confirmPassword: ['', Validators.required]
  });

  submitting = false;
  msg = '';

  showPw = false;
  showConfirm = false;

  submit() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.submitting = true;
    this.auth.resetPassword(
      this.token,
      this.form.value.newPassword!,
      this.form.value.confirmPassword!
    ).subscribe({
      next: () => {
        this.submitting = false;
        this.msg = '✅ Password successfully reset.';
      },
      error: () => {
        this.submitting = false;
        this.msg = '❌ Error resetting password.';
      }
    });
  }
}
