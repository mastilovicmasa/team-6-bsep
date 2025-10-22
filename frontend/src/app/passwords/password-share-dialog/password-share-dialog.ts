import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
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
  templateUrl: './password-share-dialog.html',
  styleUrls: ['./password-share-dialog.css']
})
export class PasswordShareDialogComponent implements OnInit {

  @Input() entry!: PasswordEntry;
  @Input() entryId!: number;
  @Output() shared = new EventEmitter<void>();

  targetEmail: string = '';
  privateKeyPem = '';
  decryptedPassword = '';
  isProcessing = false;
  successMessage = '';
  errorMessage = '';

  constructor(
    private passwordService: PasswordService,
    private cryptoService: CryptoService
  ) {}

  ngOnInit(): void {
    this.passwordService.getPasswordById(this.entryId).subscribe({
      next: (res) => { this.entry = res; },
      error: (err) => {  console.error('Failed to load entery', err); }
    });

  }

  async onPrivateKeySelected(event: any) {
    const file = event.target.files[0];
    if (!file) return;
    this.privateKeyPem = await file.text();
  }

  async sharePassword() {
    console.log(this.entry);
    if (!this.entry || !this.privateKeyPem || !this.targetEmail.trim()) {
      this.errorMessage = 'Missing input data.';
      return;
    }

    this.isProcessing = true;
    this.errorMessage = '';
    this.successMessage = '';

    try {
      // 1️⃣ Dešifruj lozinku privatnim ključem vlasnika
      const decrypted = await this.cryptoService.decryptWithPrivateKey(
        this.entry.encryptedPassword!,
        this.privateKeyPem
      );

      // 2️⃣ Preuzmi javni ključ korisnika kome se deli
      const pem = await firstValueFrom(
        this.passwordService.getUserPublicKeyByEmail(this.targetEmail)
      );

      // 3️⃣ Ponovo enkriptuje lozinku njegovim javnim ključem
      const encryptedForTarget = await this.cryptoService.encryptWithPublicKey(
        decrypted,
        pem
      );

      // 4️⃣ Šalje backendu
      await firstValueFrom(
        this.passwordService.sharePassword(this.entry.id, {
          targetEmail: this.targetEmail,
          encryptedPassword: encryptedForTarget
        })
      );

      this.successMessage = `Password successfully shared with ${this.targetEmail}.`;
      this.shared.emit();
    } catch (error: any) {
      console.error('Error while sharing password:', error);
      this.errorMessage = 'Failed to share password.';
    } finally {
      this.isProcessing = false;
    }
  }
}
