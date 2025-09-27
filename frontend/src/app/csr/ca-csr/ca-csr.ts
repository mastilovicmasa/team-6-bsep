import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CsrService, CsrRequestDto } from '../csr.service';

@Component({
  selector: 'app-ca-csr',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './ca-csr.html',
  styleUrls: ['./ca-csr.css']
})
export class CaRequestsComponent implements OnInit {
  requests: CsrRequestDto[] = [];
  loading = true;
  error = '';

  constructor(private csrService: CsrService) {}

  ngOnInit(): void {
    this.csrService.getMyCaRequests().subscribe({
      next: (data) => {
        this.requests = data;
        this.loading = false;
      },
      error: () => {
        this.error = 'Failed to load CA CSR requests';
        this.loading = false;
      }
    });
  }

  approve(id: number): void {
    this.csrService.approveAsCa(id).subscribe({
      next: () => {
        this.requests = this.requests.map(r =>
          r.id === id ? { ...r, status: 'ISSUED' } : r
        );
      },
      error: () => {
        this.error = 'Failed to approve request';
      }
    });
  }
}
