import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { CsrService, CaOption } from '../csr.service';
import Swal from 'sweetalert2';
import { Router } from '@angular/router';


@Component({
  selector: 'app-csr-upload',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './csr-upload.html',
  styleUrl: './csr-upload.css'
})
export class CsrUploadComponent implements OnInit {
  private fb = inject(FormBuilder);
  private csrService = inject(CsrService);
  private router = inject(Router);

  csrFile: File | null = null;
  submitting = false;
  successMsg = '';
  errorMsg = '';

  caOptions: CaOption[] = [];

  form = this.fb.group({
    caName: [null as string | null, Validators.required], 
    durationInDays: [null as number | null, [Validators.required, Validators.min(1)]]
  });

  ngOnInit() {
    this.csrService.getCaList().subscribe({
      next: (res) => { this.caOptions = res; },
      error: (err) => { console.error('Failed to load CA list', err); }
    });

    this.form.get('caName')?.valueChanges.subscribe(caName => {
      const max = this.getMaxDuration(caName);
      const durationCtrl = this.form.get('durationInDays');
      if (durationCtrl) {
        durationCtrl.setValidators([
          Validators.required,
          Validators.min(1),
          Validators.max(max)
        ]);
        durationCtrl.updateValueAndValidity();
      }
    });
  }

  getMaxDuration(caName: string | null): number {
    if (!caName) return 0;
    const ca = this.caOptions.find(c => c.subjectDn === caName);
    if (!ca) return 0;

    const notBefore = new Date(ca.notBefore).getTime();
    const notAfter = new Date(ca.notAfter).getTime();
    return Math.floor((notAfter - notBefore) / (1000 * 60 * 60 * 24));
  }

  formatSubject(subjectDn: string): string {
    const parts = subjectDn.split(',');
    const map: Record<string, string> = {};
    for (const p of parts) {
      const [k, v] = p.split('=').map(s => s.trim());
      map[k] = v;
    }
    return `${map['CN'] || ''} (${map['O'] || ''}, ${map['C'] || ''})`;
  }

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

    this.csrService.uploadCsr(formData).subscribe({
      next: () => {
        this.submitting = false;
        Swal.fire({
          icon: 'success',
          title: 'Success',
          text: 'CSR uploaded successfully!',
          confirmButtonText: 'OK'
        }).then(() => {
          this.router.navigate(['/my-requests']);
        });
      },
      error: (err) => {
        this.submitting = false;
        Swal.fire({
          icon: 'error',
          title: 'Upload failed',
          text: err?.error || '❌ Upload failed',
          confirmButtonText: 'OK'
        });
      }
    });
  }
}
