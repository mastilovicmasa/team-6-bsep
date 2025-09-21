import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-csr-upload',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './csr-upload.html',
  styleUrl: './csr-upload.css'
})
export class CsrUploadComponent {
  private fb = inject(FormBuilder);
  private http = inject(HttpClient);

  csrFile: File | null = null;
  submitting = false;
  successMsg = '';
  errorMsg = '';

  caOptions = ['TestCA1', 'TestCA2'];

  form = this.fb.group({
    caName: ['', Validators.required],
    durationInDays: [365, [Validators.required, Validators.min(1)]]
  });

  onFileSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      this.csrFile = input.files[0];
    }
  }

  submit() {
    this.successMsg = '';
    this.errorMsg = '';
    if (this.form.invalid || !this.csrFile) {
      this.form.markAllAsTouched();
      return;
    }

    this.submitting = true;

    const formData = new FormData();
    formData.append('caName', this.form.value.caName!);
    formData.append('durationInDays', this.form.value.durationInDays!.toString());
    formData.append('csrFile', this.csrFile!);

    this.http.post('http://localhost:8080/api/csr/upload', formData).subscribe({
      next: (res) => {
        this.submitting = false;
        this.successMsg = '✅ CSR uploaded successfully!';
      },
      error: (err) => {
        this.submitting = false;
        this.errorMsg = err?.error || '❌ Upload failed';
      }
    });
  }
}
