import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CryptoService } from '../../shared/crypto.service';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-password-shared-decrypt-dialog',
  imports: [CommonModule, FormsModule],
  templateUrl: './password-shared-decrypt-dialog.html',
  styleUrl: './password-shared-decrypt-dialog.css'
})
export class PasswordSharedDecryptDialog {

  @Input() siteName!: string;
  @Input() username!: string;
  @Input() encryptedPassword!: string;

  privateKeyPem = '';
  decryptedPassword = '';
  isProcessing = false;
  errorMessage = '';
  showPassword = false;

  constructor(private cryptoService: CryptoService) {}

  async onPrivateKeySelected(event: any) {
    const file = event.target.files?.[0];
    if (!file) return;
    this.privateKeyPem = await file.text();
  }

  async decryptPassword() {
    if (!this.privateKeyPem || !this.encryptedPassword) {
      console.log(this.privateKeyPem + this.encryptedPassword)
      this.errorMessage = 'Please upload your private key.';
      return;
    }

    this.isProcessing = true;
    this.errorMessage = '';

    try {
      const decrypted = await this.cryptoService.decryptWithPrivateKey(
        this.encryptedPassword,
        this.privateKeyPem
      );
      this.decryptedPassword = decrypted;
      this.showPassword = true;
    } catch (err) {
      console.error('Decryption failed', err);
      this.errorMessage = 'Failed to decrypt password.';
    } finally {
      this.isProcessing = false;
    }
  }

  hidePassword() {
    this.showPassword = false;
    this.decryptedPassword = '';
  }
}


