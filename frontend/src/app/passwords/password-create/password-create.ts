import { Component, EventEmitter, Output } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { PasswordService } from '../../shared/password.service';
import { CryptoService } from '../../shared/crypto.service';
import { firstValueFrom } from 'rxjs';
import { PasswordEntry } from '../../shared/password.model';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-password-create',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './password-create.html',
  styleUrls: ['./password-create.css'] 
})
export class PasswordCreateComponent {

  form: FormGroup;
  isSaving = false;
  successMessage = '';
  errorMessage = '';
  @Output() passwordCreated = new EventEmitter<void>();

  constructor(
    private fb: FormBuilder,
    private passwordService: PasswordService,
    private cryptoService: CryptoService
  ) {
    this.form = this.fb.group({
      siteName: ['', Validators.required],
      username: ['', Validators.required],
      password: ['', Validators.required]
    });
  }

  async onSubmit() {
    if (this.form.invalid) return;

    this.isSaving = true;
    this.successMessage = '';
    this.errorMessage = '';

    try {
      // Preuzimanje javnog sertifikata prijavljenog korisnika
      const pem = await firstValueFrom(this.passwordService.getUserCertificate());

      // Enkripcija lozinke javnim ključem korisnika
      const plainPassword = this.form.value.password;
      const encrypted = await this.cryptoService.encryptWithPublicKey(plainPassword, pem);

      // Formiranje request objekta
      const request = {
        siteName: this.form.value.siteName,
        username: this.form.value.username,
        encryptedPassword: encrypted
      };

      // Slanje backendu
      const result: PasswordEntry = await firstValueFrom(this.passwordService.createPassword(request));
      //console.log(result);

      this.successMessage = `Password for ${result.siteName} successfully saved.`;
      this.form.reset();
      this.passwordCreated.emit();

    } catch (error: any) {
      console.error('Error while saving password:', error);
      this.errorMessage = 'Failed to save password.';
    } finally {
      this.isSaving = false;
    }
  }


}
