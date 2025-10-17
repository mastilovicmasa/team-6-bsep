import { Component, OnInit } from '@angular/core';
import Swal from 'sweetalert2';
import { CaUser } from '../shared/ca-user.model';
import { CaService } from '../shared/ca.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-ca-users',
  imports: [CommonModule ],
  templateUrl: './ca-users.html',
  styleUrl: './ca-users.css'
})
export class CaUsers implements OnInit{

  caUsers: CaUser[] = [];
  loading = true;

  constructor(private caUsersService: CaService) {}

  ngOnInit() {
    this.loadCaUsers();
  }

  loadCaUsers() {
    this.loading = true;
    this.caUsersService.getAllCaUsers().subscribe({
      next: (users) => {
        this.caUsers = users;
        this.loading = false;
      },
      error: (err) => {
        console.error(err);
        this.loading = false;
        Swal.fire('Error', 'Failed to load CA users.', 'error');
      }
    });
  }

  issueCertificate(user: CaUser) {
    Swal.fire({
      title: 'Issue CA Certificate?',
      text: `Do you want to issue a CA certificate for ${user.firstName} (${user.organization})?`,
      input: 'text',
      inputLabel: 'Subject DN',
      inputValue: `CN=${user.organization} CA, O=${user.organization}, C=RS`,
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
        this.caUsersService.issueCaCertificate(user.email, result.value).subscribe({
          next: () => {
            Swal.fire('Success', 'CA certificate issued successfully!', 'success');
            this.loadCaUsers(); // refresh
          },
          error: (err) => {
            console.error(err);
            Swal.fire('Error', 'Failed to issue CA certificate.', 'error');
          }
        });
      }
    });
  }

}
