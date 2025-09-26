import { Component } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { AuthService } from '../auth/auth.service';

@Component({
  selector: 'app-create-ca-user',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './create-ca-user.html',
  styleUrls: ['./create-ca-user.css']
})
export class CreateCaUserComponent {
  form: FormGroup;
  success = '';
  error = '';

  constructor(private fb: FormBuilder, private auth: AuthService) {
    this.form = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      firstName: ['', Validators.required],
      lastName: ['', Validators.required],
      organization: ['', Validators.required]
    });
  }

  submit(): void {
    if (this.form.invalid) return;

    this.auth.createCaUser(this.form.value).subscribe({
      next: (msg: string) => {   
        this.success = msg;      
        this.error = '';
        this.form.reset();
      },
      error: (err) => {
        if (err.status === 409) {
          this.error = 'Email already registered.';
        } else if (err.error?.message) {
          this.error = err.error.message;
        } else {
          this.error = 'Failed to create CA user';
        }
        this.success = '';
      }
    });
  }

}
