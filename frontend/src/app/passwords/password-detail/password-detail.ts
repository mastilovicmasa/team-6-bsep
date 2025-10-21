import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { PasswordService } from '../../shared/password.service';
import { CryptoService } from '../../shared/crypto.service';
import { firstValueFrom } from 'rxjs';
import { CommonModule, DatePipe } from '@angular/common';

@Component({
  selector: 'app-password-detail',
  standalone: true,
  imports: [CommonModule, RouterModule, DatePipe],
  templateUrl: './password-detail.html'
})
export class PasswordDetailComponent implements OnInit {

  entry?: {
    id: number;
    siteName: string;
    username: string;
    encryptedPassword?: string;
    createdAt: string;
  };

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
}
