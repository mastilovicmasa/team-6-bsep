import { Component, inject, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { AuthService, LoginRequest, JwtResponse } from '../../auth/auth.service';
import { RouterLink, Router } from '@angular/router';
import { RecaptchaModule, RecaptchaComponent } from 'ng-recaptcha';

@Component({
  selector: 'app-login',
  imports: [CommonModule, ReactiveFormsModule, RouterLink, RecaptchaModule],
  templateUrl: './login.html',
  styleUrl: './login.css'
})
export class Login {
  @ViewChild(RecaptchaComponent) captcha!: RecaptchaComponent;
  
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

        if (this.captcha) {
          this.captcha.reset();
        }
        this.form.patchValue({ recaptchaToken: '' });
      }
    });
  }

  onCaptchaResolved(token: string | null) {
    this.form.patchValue({ recaptchaToken: token });
  }
}
