import { Component, OnInit } from '@angular/core';
import { CaService } from '../shared/ca.service';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CertificateRequest } from '../model/certificate-request.model';


@Component({
  selector: 'app-issue-intermediate',
   imports: [CommonModule, FormsModule],
  templateUrl: './issue-intermediate.html',
  styleUrl: './issue-intermediate.css'
})
export class IssueIntermediate implements OnInit{

   cas: any[] = [];
  request: CertificateRequest = {
    issuerId: 0,
    commonName: '',
    organization: '',
    organizationalUnit: '',
    country: '',
    email: '',
    validityInDays: 365
  };
  issued: any;

  constructor(private certService: CaService) {}

  ngOnInit(): void {
    this.certService.getAllCas().subscribe((data) => (this.cas = data));
  }

  issueCertificate() {
    this.certService.issueIntermediate(this.request).subscribe({
      next: (res) => (this.issued = res),
      error: (err) => console.error('Error issuing certificate', err)
    });
  }

}
