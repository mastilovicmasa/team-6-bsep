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
      html: `
        <label class="block text-left mb-2 font-medium">Subject DN:</label>
        <input id="subjectDn" class="swal2-input" value="CN=${user.organization} CA, O=${user.organization}, C=RS" />

        <div class="text-left mt-3">
          <label class="block mb-2 font-medium">Allow further CA issuance?</label>
          <div>
            <input type="radio" id="allowYes" name="pathLen" value="1" checked>
            <label for="allowYes">Yes (CA can issue other CAs)</label>
          </div>
          <div>
            <input type="radio" id="allowNo" name="pathLen" value="0">
            <label for="allowNo">No (CA cannot issue other CAs)</label>
          </div>
        </div>
      `,
      focusConfirm: false,
      showCancelButton: true,
      confirmButtonText: 'Issue',
      preConfirm: () => {
        const subjectDn = (document.getElementById('subjectDn') as HTMLInputElement).value.trim();
        const pathLen = parseInt((document.querySelector('input[name="pathLen"]:checked') as HTMLInputElement).value);

        if (!subjectDn) {
          Swal.showValidationMessage('Subject DN is required');
          return false;
        }
        return { subjectDn, pathLen };
      }
    }).then((result) => {
      if (result.isConfirmed && result.value) {
        const { subjectDn, pathLen } = result.value;
        this.caUsersService.issueCaCertificate(user.email, subjectDn, pathLen).subscribe({
          next: () => {
            Swal.fire('Success', 'CA certificate issued successfully!', 'success');
            this.loadCaUsers();
          },
          error: (err) => {
            console.error(err);
            Swal.fire('Error', err.error?.error || 'Failed to issue CA certificate.', 'error');
          }
        });
      }
    });
  }

}
