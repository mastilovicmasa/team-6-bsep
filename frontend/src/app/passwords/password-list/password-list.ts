import { Component, OnInit } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { PasswordService } from '../../shared/password.service';
import { PasswordEntry, SharedPassword } from '../../shared/password.model';
import { firstValueFrom } from 'rxjs';
import { CommonModule, DatePipe } from '@angular/common';
import { PasswordCreateComponent } from '../password-create/password-create';
import { PasswordSharedDecryptDialog } from '../password-shared-decrypt-dialog/password-shared-decrypt-dialog';

@Component({
  selector: 'app-password-list',
  standalone: true,
  imports: [CommonModule, RouterModule, DatePipe, PasswordCreateComponent, PasswordSharedDecryptDialog],
  templateUrl: './password-list.html',
  styleUrls: ['./password-list.css']
})
export class PasswordListComponent implements OnInit {

  passwords: PasswordEntry[] = [];
  shared: SharedPassword[] = [];
  isLoading = false;
  errorMessage = '';
  showSharedDialog = false;
  selectedShared?: SharedPassword;

  constructor(
    private passwordService: PasswordService,
    private router: Router
  ) {}

  async ngOnInit() {
    this.isLoading = true;
    this.errorMessage = '';

    try {
      this.passwords = await firstValueFrom(this.passwordService.getMyPasswords());
      this.shared = await firstValueFrom(this.passwordService.getSharedPasswords());
    } catch (error: any) {
      console.error('Failed to load passwords:', error);
      this.errorMessage = 'Failed to load passwords.';
    } finally {
      this.isLoading = false;
    }
  }

  openDetails(entry: PasswordEntry) {
    this.router.navigate(['/passwords', entry.id]);
  }

  openSharedDialog(entry: SharedPassword) {
    this.selectedShared = entry;
    this.showSharedDialog = true;
  }

 showAddPassword = false;

  toggleAddPassword() {
    this.showAddPassword = !this.showAddPassword;
  }

  onPasswordCreated() {
    this.showAddPassword = false;
    this.ngOnInit(); // refresh liste
  }


  

}
