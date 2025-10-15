import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators, FormGroup } from '@angular/forms';
import { AuthService } from '../../auth/auth.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-change-password',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './change-password.html',
  styleUrls: ['./change-password.css']
})
export class ChangePasswordComponent implements OnInit {
  form!: FormGroup;

  success = '';
  error = '';
  submitting = false;

  constructor(private fb: FormBuilder, private auth: AuthService, private router: Router) {}

  ngOnInit(): void {
    this.form = this.fb.group({
      oldPassword: ['', Validators.required],
      newPassword: ['', [Validators.required, Validators.minLength(8)]],
      confirmPassword: ['', Validators.required]
    });
  }

  submit(): void {
    if (this.form.invalid) return;

    const { oldPassword, newPassword, confirmPassword } = this.form.value;

    this.submitting = true;
    this.success = '';
    this.error = '';

    this.auth.changePassword(oldPassword!, newPassword!, confirmPassword!).subscribe({
      next: () => {
        this.success = '✅ Password changed successfully!';
        this.error = '';
        this.submitting = false;

        // odmah odjavi korisnika
        localStorage.removeItem('jwt');
        localStorage.removeItem('jti');

        // redirect na login
        this.router.navigate(['/login']);
      },
      error: (err) => {
        this.error = err.error?.message || '❌ Failed to change password';
        this.success = '';
        this.submitting = false;
      }
    });

  }
}
