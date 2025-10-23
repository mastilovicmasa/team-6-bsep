import { Component, OnInit } from '@angular/core';
import Swal from 'sweetalert2';
import { CaUser } from '../shared/ca-user.model';
import { CaService } from '../shared/ca.service';
import { CommonModule } from '@angular/common';
import { AddSubordinateComponent } from '../add-subordinates/add-subordinates';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'app-ca-subordinates',
  standalone: true,
  imports: [CommonModule, AddSubordinateComponent],
  templateUrl: './ca-subordinates.html',
  styleUrls: ['./ca-subordinates.css']
})
export class CaSubordinatesComponent implements OnInit {
  subordinates: CaUser[] = [];
  loading = true;
  showAddSubordinate = false;

  constructor(private caService: CaService) {}

  ngOnInit() {
    this.loadSubordinates();
  }

  toggleAddSubordinate() {
    this.showAddSubordinate = !this.showAddSubordinate;
  }

  loadSubordinates() {
    this.loading = true;
    this.caService.getSubordinateCaUsers().subscribe({
      next: (users) => {
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

  async issueSubCa(user: CaUser) {
  try {
    // Učitaj sve dostupne šablone
    const templates = await firstValueFrom(this.caService.getTemplates());
    const options = templates.map(t => `<option value="${t.id}">${t.name}</option>`).join('');

    Swal.fire({
      title: 'Issue Sub-CA Certificate',
      html: `
        <label class="block text-left mb-2 font-medium">Template:</label>
        <select id="templateSelect" class="swal2-select">
          <option value="">(no template)</option>
          ${options}
        </select>

        <label class="block text-left mb-2 font-medium mt-3">Subject DN:</label>
        <input id="subjectDn" class="swal2-input" value="CN=${user.organization} SubCA, O=${user.organization}, C=RS" />
      `,
      showCancelButton: true,
      confirmButtonText: 'Issue',
      preConfirm: () => {
        const subjectDn = (document.getElementById('subjectDn') as HTMLInputElement).value.trim();
        const templateId = (document.getElementById('templateSelect') as HTMLSelectElement).value;
        if (!subjectDn) {
          Swal.showValidationMessage('Subject DN is required');
          return false;
        }
        return { subjectDn, templateId };
      }
    }).then(result => {
      if (result.isConfirmed && result.value) {
        const { subjectDn, templateId } = result.value;

        this.caService.issueSubCaCertificate(user.email, subjectDn, templateId ? +templateId : undefined)
          .subscribe({
            next: () => {
              Swal.fire('Success', 'Subordinate CA certificate issued successfully!', 'success');
              this.loadSubordinates();
            },
            error: (err) => {
              console.error(err);
              Swal.fire('Error', err.error?.error || 'Failed to issue subordinate CA certificate.', 'error');
            }
          });
      }
    });

  } catch (error) {
    console.error(error);
    Swal.fire('Error', 'Unable to load templates.', 'error');
  }
}


  onSubordinateCreated() {
    this.showAddSubordinate = false;
    this.loadSubordinates();  
  }

  openTemplateDialog() {
  Swal.fire({
    title: 'Create Template',
    html: `
      <input id="tplName" class="swal2-input" placeholder="Template name">
      <input id="tplCN" class="swal2-input" placeholder="CN regex (e.g. .*\\.ftn\\.com)">
      <input id="tplSAN" class="swal2-input" placeholder="SAN regex (optional)">
      <input id="tplTTL" type="number" class="swal2-input" placeholder="TTL (days)">
    `,
    showCancelButton: true,
    confirmButtonText: 'Save',
    preConfirm: () => {
      const name = (document.getElementById('tplName') as HTMLInputElement).value.trim();
      const cnRegex = (document.getElementById('tplCN') as HTMLInputElement).value.trim();
      const sanRegex = (document.getElementById('tplSAN') as HTMLInputElement).value.trim();
      const ttlDays = parseInt((document.getElementById('tplTTL') as HTMLInputElement).value);
      if (!name || !cnRegex || !ttlDays) {
        Swal.showValidationMessage('All required fields must be filled');
        return false;
      }
      return { name, cnRegex, sanRegex, ttlDays };
    }
  }).then(result => {
    if (result.isConfirmed) {
      this.caService.createTemplate({
        name: result.value.name,
        cnRegex: result.value.cnRegex,
        sanRegex: result.value.sanRegex,
        ttlDays: result.value.ttlDays,
        keyUsageMask: 32,
        extendedKeyUsages: ["1.3.6.1.5.5.7.3.1"]
      }).subscribe(() => Swal.fire('Template created!', '', 'success'));
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

        this.caService.revokeSubordinateCertificate(user.certificateSerial, reason).subscribe({
          next: () => {
            Swal.fire('Success',`Certificate for ${user.email} has been revoked.`, 'success');

            user.CaCertificate = false;
            user.revoked = true;

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
