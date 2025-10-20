import { Component, EventEmitter, Output } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { CaService } from '../shared/ca.service';

@Component({
  selector: 'app-add-subordinates',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './add-subordinates.html',
  styleUrls: ['./add-subordinates.css']
})
export class AddSubordinateComponent {
  form: FormGroup;
  success = '';
  error = '';

  @Output() userCreated = new EventEmitter<void>(); 
  @Output() close = new EventEmitter<void>();    

  constructor(private fb: FormBuilder, private caService: CaService) {
    this.form = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      firstName: ['', Validators.required],
      lastName: ['', Validators.required],
      organization: ['', Validators.required],
    });
  }

  submit(): void {
    if (this.form.invalid) return;

    this.caService.createSubordinateUser(this.form.value).subscribe({
      next: () => {
        this.success = 'Subordinate CA user created successfully!';
        this.error = '';
        this.userCreated.emit();
        this.form.reset();

        setTimeout(() => this.close.emit(), 500);
      },
      error: (err) => {
        console.error(err);
        if (err.status === 409) {
          this.error = 'Email already registered.';
        } else if (err.error?.message) {
          this.error = err.error.message;
        } else {
          this.error = 'Failed to create subordinate CA user.';
        }
        this.success = '';
      }
    });
  }
}
