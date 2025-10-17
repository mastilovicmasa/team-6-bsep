import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import Swal from 'sweetalert2';
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

  constructor(private fb: FormBuilder, private caService: CaService) {
    this.form = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      firstName: ['', Validators.required],
      lastName: ['', Validators.required],
      organization: ['', Validators.required],
    });
  }

  submit() {
    if (this.form.invalid) {
      Swal.fire('Error', 'Please fill in all required fields correctly.', 'error');
      return;
    }

    const formData = this.form.value;
    this.caService.createSubordinateUser(formData).subscribe({
      next: () => {
        Swal.fire('Success', 'Subordinate CA user created successfully!', 'success');
        this.form.reset();
      },
      error: (err) => {
        console.error(err);
        Swal.fire('Error', err.error?.error || 'Failed to create subordinate CA user.', 'error');
      }
    });
  }
}
