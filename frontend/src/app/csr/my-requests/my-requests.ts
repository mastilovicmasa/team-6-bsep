import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CsrService, CsrRequestDto } from '../csr.service';

@Component({
  selector: 'app-my-requests',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './my-requests.html',
  styleUrls: ['./my-requests.css']
})
export class MyRequestsComponent implements OnInit {
  requests: CsrRequestDto[] = [];
  loading = true;
  error = '';

  constructor(private csrService: CsrService) {}

  ngOnInit(): void {
    this.csrService.getMyRequests().subscribe({
      next: (data) => {
        this.requests = data;
        this.loading = false;
      },
      error: () => {
        this.error = 'Failed to load CSR requests';
        this.loading = false;
      }
    });
  }
}
