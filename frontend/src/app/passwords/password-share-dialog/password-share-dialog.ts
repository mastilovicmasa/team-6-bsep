import { Component, Input } from '@angular/core';
import { PasswordService } from '../../shared/password.service';
import { CryptoService } from '../../shared/crypto.service';
import { firstValueFrom } from 'rxjs';
import { PasswordEntry } from '../../shared/password.model';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-password-share-dialog',  
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './password-share-dialog.html'
})
export class PasswordShareDialogComponent {

  @Input() entry!: PasswordEntry;

  targetEmail?: number;
  privateKeyPem = '';
  decryptedPassword = '';
  isProcessing = false;
  successMessage = '';
  errorMessage = '';

  constructor(
    private passwordService: PasswordService,
    private cryptoService: CryptoService
  ) {}

  async onPrivateKeySelected(event: any) {
    const file = event.target.files[0];
    if (!file) return;

    const content = await file.text();
    this.privateKeyPem = content;
  }

  async sharePassword() {
    if (!this.entry || !this.privateKeyPem || !this.targetEmail) {
      this.errorMessage = 'Missing input data.';
      return;
    }

    this.isProcessing = true;
    this.errorMessage = '';
    this.successMessage = '';

    try {
      const currentUserId = parseInt(localStorage.getItem('userId') || '0', 10);
      const share = this.entry.shares.find(s => s.userId === currentUserId);

      if (!share) {
        this.errorMessage = 'You do not have access to decrypt this password.';
        return;
      }

      // Korisnik dešifruje lozinku svojim privatnim ključem
      const decrypted = await this.cryptoService.decryptWithPrivateKey(
        share.encryptedPassword,
        this.privateKeyPem
      );

      // Preuzima PEM sertifikat primaoca
      const pem = await firstValueFrom(this.passwordService.getUserCertificate());

      // Ponovno enkriptuje lozinku javnim ključem primaoca
      const encryptedForTarget = await this.cryptoService.encryptWithPublicKey(decrypted, pem);

      // Šalje backendu novi PasswordShare
      await firstValueFrom(this.passwordService.sharePassword(this.entry.id, {
        targetUserId: this.targetEmail,
        encryptedPassword: encryptedForTarget
      }));

      this.successMessage = 'Password successfully shared.';
    } catch (error: any) {
      console.error('Error while sharing password:', error);
      this.errorMessage = 'Failed to share password.';
    } finally {
      this.isProcessing = false;
    }
  }
}
