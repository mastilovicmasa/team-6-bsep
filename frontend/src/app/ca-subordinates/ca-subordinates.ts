import { Component, OnInit } from '@angular/core';
import Swal from 'sweetalert2';
import { CaUser } from '../shared/ca-user.model';
import { CaService } from '../shared/ca.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-ca-subordinates',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './ca-subordinates.html',
  styleUrl: './ca-subordinates.css'
})
export class CaSubordinatesComponent implements OnInit {

  subordinates: CaUser[] = [];
  loading = true;

  constructor(private caService: CaService) {}

  ngOnInit() {
    this.loadSubordinates();
  }

  loadSubordinates() {
    this.loading = true;
    this.caService.getSubordinateCaUsers().subscribe({
      next: (users) => {
        console.log(users);
        this.subordinates = users;
        this.loading = false;
      },
      error: (err) => {
        console.error(err);
        this.loading = false;
        Swal.fire('Error', 'Failed to load subordinate users.', 'error');
      }
    });
  }

  issueSubCa(user: CaUser) {
    Swal.fire({
      title: 'Issue Sub-CA Certificate?',
      text: `Do you want to issue a subordinate CA certificate for ${user.firstName} (${user.organization})?`,
      input: 'text',
      inputLabel: 'Subject DN',
      inputValue: `CN=${user.organization} SubCA, O=${user.organization}, C=RS`,
      showCancelButton: true,
      confirmButtonText: 'Issue',
      preConfirm: (subjectDn) => {
        if (!subjectDn) {
          Swal.showValidationMessage('Subject DN is required');
          return false;
        }
        return subjectDn;
      }
    }).then((result) => {
      if (result.isConfirmed && result.value) {
        this.caService.issueSubCaCertificate(user.email, result.value).subscribe({
          next: () => {
            Swal.fire('Success', 'Subordinate CA certificate issued successfully!', 'success');
            this.loadSubordinates();
          },
          error: (err) => {
            console.error(err);
            Swal.fire('Error', 'Failed to issue subordinate CA certificate.', 'error');
          }
        });
      }
    });
  }
}
