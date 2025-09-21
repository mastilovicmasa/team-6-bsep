// src/app/dashboard/dashboard.component.ts
import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule],
  template: `
    <h2>Welcome to PKI System</h2>
    <p>This is the dashboard page.</p>
  `,
  styles: [`
    h2 {
      margin-bottom: 0.5rem;
    }
    p {
      color: #555;
    }
  `]
})
export class DashboardComponent {}
