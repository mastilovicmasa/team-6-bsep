import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CsrService, CsrRequestDto } from '../csr.service';

@Component({
  selector: 'app-admin-csr',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './admin-csr.html',
  styleUrls: ['./admin-csr.css']
})
export class AdminRequestsComponent implements OnInit {
  requests: CsrRequestDto[] = [];
  loading = true;
  error = '';

  constructor(private csrService: CsrService) {}

  ngOnInit(): void {
    this.csrService.getAllRequests().subscribe({
      next: (data) => {
        this.requests = data;
        this.loading = false;
      },
      error: () => {
        this.error = 'Failed to load CSR';
        this.loading = false;
      }
    });
  }
}
