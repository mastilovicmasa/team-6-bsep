import { Component, OnInit } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { PasswordService } from '../../shared/password.service';
import { PasswordEntry } from '../../shared/password.model';
import { firstValueFrom } from 'rxjs';
import { CommonModule, DatePipe } from '@angular/common';

@Component({
  selector: 'app-password-list',
  standalone: true,
  imports: [CommonModule, RouterModule, DatePipe],
  templateUrl: './password-list.html'
})
export class PasswordListComponent implements OnInit {

  passwords: PasswordEntry[] = [];
  shared: PasswordEntry[] = [];
  isLoading = false;
  errorMessage = '';

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
  

}
