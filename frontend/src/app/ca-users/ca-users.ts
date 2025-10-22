import { Component, OnInit } from '@angular/core';
import Swal from 'sweetalert2';
import { CaUser } from '../shared/ca-user.model';
import { CaService } from '../shared/ca.service';
import { CommonModule } from '@angular/common';
import { CreateCaUserComponent } from '../create-ca-user/create-ca-user';

@Component({
  selector: 'app-ca-users',
  imports: [CommonModule, CreateCaUserComponent ],
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
        this.caUsers.forEach(user => {
        if (user.hasCaCertificate && user.certificateSerial) {
          this.caUsersService.checkRevoked(user.certificateSerial).subscribe(isRevoked => {
            user.revoked = isRevoked;
          });
        }
    });
      },
      error: (err) => {
        console.error(err);
        this.loading = false;
        Swal.fire('Error', 'Failed to load CA users.', 'error');
      }
    });
  }

  showCreateCaUser = false;

  toggleCreateCaUser() {
    this.showCreateCaUser = !this.showCreateCaUser;
  }

  onCaUserCreated(newUser: any) {
    this.caUsers.push(newUser);
    this.showCreateCaUser = false;
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

  revokeCertificate(user: any) {
    Swal.fire({
      title: `Revoke certificate for ${user.email}?`,
      html: `
        <div class="text-left mt-2">
          <label class="block mb-2 font-medium">Revocation reason:</label>
          <select id="revokeReason" class="swal2-select">
            <option value="keyCompromise">Key Compromise</option>
            <option value="caCompromise">CA Compromise</option>
            <option value="affiliationChanged">Affiliation Changed</option>
            <option value="superseded">Superseded</option>
            <option value="cessationOfOperation">Cessation of Operation</option>
            <option value="certificateHold">Certificate Hold</option>
            <option value="unspecified" selected>Unspecified</option>
          </select>
        </div>
      `,
      showCancelButton: true,
      confirmButtonText: 'Revoke',
      cancelButtonText: 'Cancel',
      focusConfirm: false,
      preConfirm: () => {
        const reason = (document.getElementById('revokeReason') as HTMLSelectElement).value;
        if (!reason) {
          Swal.showValidationMessage('You must select a reason for revocation');
          return false;
        }
        return { reason };
      }
    }).then(result => {
      if (result.isConfirmed && result.value?.reason) {
        const reason = result.value.reason;

        this.caUsersService.revokeCertificate(user.certificateSerial, reason).subscribe({
          next: () => {
            Swal.fire('Success',`Certificate for ${user.email} has been revoked.`, 'success');

            user.CaCertificate = false;
            user.revoked = true;
            this.loadCaUsers();

          },
          error: (err) => {
            console.error(err);
            alert(`Error revoking certificate: ${err.error?.message || err.message}`);
            Swal.fire('Error',`Error revoking certificate: ${err.error?.message || err.message}`, 'error');

          }
        });
      }
    });
  }

}
