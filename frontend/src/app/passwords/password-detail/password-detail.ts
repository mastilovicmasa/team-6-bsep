import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { PasswordService } from '../../shared/password.service';
import { CryptoService } from '../../shared/crypto.service';
import { firstValueFrom } from 'rxjs';
import { CommonModule, DatePipe } from '@angular/common';
import { PasswordShareDialogComponent } from '../password-share-dialog/password-share-dialog';
import { PasswordEntry } from '../../shared/password.model';

@Component({
  selector: 'app-password-detail',
  standalone: true,
  imports: [CommonModule, RouterModule, DatePipe, PasswordShareDialogComponent],
  templateUrl: './password-detail.html',
  styleUrl: './password-detail.css'
})
export class PasswordDetailComponent implements OnInit {

  entry?: PasswordEntry

  isLoading = false;
  errorMessage = '';
  decryptedPassword = '';
  privateKeyPem = '';
  showPassword = false;

  constructor(
    private route: ActivatedRoute,
    private passwordService: PasswordService,
    private cryptoService: CryptoService
  ) {}

  async ngOnInit() {
    this.isLoading = true;
    const id = Number(this.route.snapshot.paramMap.get('id'));

    try {
      this.entry = await firstValueFrom(this.passwordService.getPasswordById(id));
    } catch (error: any) {
      console.error('Failed to load password details:', error);
      this.errorMessage = 'Failed to load password details.';
    } finally {
      this.isLoading = false;
    }
  }

  async onPrivateKeySelected(event: any) {
    const file = event.target.files[0];
    if (!file) return;
    this.privateKeyPem = await file.text();
  }

  async decryptPassword() {
    if (!this.entry || !this.privateKeyPem) {
      this.errorMessage = 'Private key not loaded.';
      return;
    }

    try {
      if(!this.entry.encryptedPassword){
          return;
        }
      // Dekripcija koristi jedinstveni encryptedPassword iz backa
      this.decryptedPassword = await this.cryptoService.decryptWithPrivateKey(        
        this.entry.encryptedPassword,
        this.privateKeyPem
      );
      this.showPassword = true;
      this.errorMessage = '';
    } catch (error: any) {
      console.error('Decryption failed:', error);
      this.errorMessage = 'Failed to decrypt password.';
    }
  }

  hidePassword() {
    this.showPassword = false;
    this.decryptedPassword = '';
  }

  showShareModal = false;

toggleShareModal() {
  this.showShareModal = !this.showShareModal;
}

async revokeShare(shareId: number) {
  try {
    await firstValueFrom(this.passwordService.revokeShare(shareId));
    // refresh
    const id = this.entry?.id;
    if (id) this.entry = await firstValueFrom(this.passwordService.getPasswordById(id));
  } catch (error) {
    console.error('Failed to revoke share:', error);
  }
}

async onShared() {
  this.showShareModal = false;
  // refresh list after new share

   if (!this.entry) return;
    try {
      const updated = await firstValueFrom(this.passwordService.getPasswordById(this.entry.id));
      this.entry = updated;
    } catch (err) {
      console.error('Failed to refresh password after sharing', err);
    }
  // const id = this.entry?.id;
  // if (id) this.ngOnInit();
}

}
