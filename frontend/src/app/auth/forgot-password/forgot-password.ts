import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { AuthService } from '../../auth/auth.service';

@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './forgot-password.html',
  styleUrl: './forgot-password.css'
})
export class ForgotPassword {
  private fb = inject(FormBuilder);
  private auth = inject(AuthService);

  form = this.fb.group({
    email: ['', [Validators.required, Validators.email]]
  });

  submitting = false;
  msg = '';

  submit() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.submitting = true;
    this.auth.forgotPassword(this.form.value.email!).subscribe({
      next: () => {
        this.submitting = false;
        this.msg = '📩 Check your email for reset link.';
      },
      error: () => {
        this.submitting = false;
        this.msg = '❌ Error sending reset email.';
      }
    });
  }
}