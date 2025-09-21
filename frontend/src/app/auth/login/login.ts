import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { AuthService, LoginRequest, JwtResponse } from '../../auth/auth.service';
import { RouterLink, Router } from '@angular/router';
import { RecaptchaModule } from 'ng-recaptcha';

@Component({
  selector: 'app-login',
  imports: [CommonModule, ReactiveFormsModule, RouterLink, RecaptchaModule],
  templateUrl: './login.html',
  styleUrl: './login.css'
})
export class Login {
  showPw = false;
  submitting = false;
  successMsg = '';
  errorMsg = '';

  private fb = inject(FormBuilder);
  private auth = inject(AuthService);
  private router = inject(Router);

  form = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', Validators.required],
    recaptchaToken: ['', Validators.required]
  });

  submit() {
    this.successMsg = '';
    this.errorMsg = '';
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.submitting = true;
    this.auth.login(this.form.value as LoginRequest).subscribe({
      next: (res: JwtResponse) => {
        this.submitting = false;
        this.successMsg = '✅ Logged in successfully!';
        // čuvanje tokena u localStorage/sessionStorage
        localStorage.setItem('jwt', res.token);
        localStorage.setItem('jti', res.jti);
        this.router.navigate(['home']);
      },
      error: (err) => {
        this.submitting = false;
        this.errorMsg = err?.error || 'Invalid email or password';
      }
    });
  }

  onCaptchaResolved(token: string | null) {
    this.form.patchValue({ recaptchaToken: token });
  }
}
