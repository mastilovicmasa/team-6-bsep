import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { CaService } from '../shared/ca.service';

@Component({
  selector: 'app-admin',
  templateUrl: './admin.component.html',
  imports: [CommonModule],
  styleUrls: ['./admin.component.css']
})
export class AdminComponent {

   busy = false;
  msg = '';
  status: any = null;

  constructor(private ca: CaService) {}

  createRoot() {
    this.busy = true; this.msg = '';
    this.ca.createRoot().subscribe({
      next: (txt: any) => { this.msg = String(txt); this.busy = false; this.refreshStatus(); },
      error: (err: { error: any; message: any; }) => {
        this.msg = err?.error || err?.message || 'Error';
        this.busy = false;
        this.refreshStatus();
      }
    });
  }

  refreshStatus() {
    this.ca.getRootStatus().subscribe({
      next: (s: any) => this.status = s,
      error: (_: any) => {} // ok ako još ne postoji
    });
  }

  ngOnInit() { this.refreshStatus(); }
}
