import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { AuthService } from '../../auth/auth.service';

@Component({
  selector: 'app-register',
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './register.html',
  styleUrl: './register.css'
})
export class Register {

  private fb = inject(FormBuilder);
  private auth = inject(AuthService);

  form = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    firstName: ['', Validators.required],
    lastName: ['', Validators.required],
    organization: ['', Validators.required],
    password: ['', [Validators.required, Validators.minLength(12)]],
    confirmPassword: ['', Validators.required],
  });

  submitting = false;
  successMsg = '';
  errorMsg = '';

  submit() {
    this.successMsg = '';
    this.errorMsg = '';
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.submitting = true;
    this.auth.register(this.form.value as any).subscribe({
      next: () => {
        this.submitting = false;
        this.successMsg = '✅ Registracija uspešna! Proveri mejl i klikni na aktivacioni link.';
        this.form.disable(); // spreči dupli submit
      },
      error: (err) => {
        this.submitting = false;
        this.errorMsg = err?.error?.message || 'Došlo je do greške pri registraciji.';
      }
    });
  }
}
