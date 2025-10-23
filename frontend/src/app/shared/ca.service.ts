import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CaUser } from './ca-user.model';

@Injectable({ providedIn: 'root' })
export class CaService {
  private api = 'https://localhost:8443';
  constructor(private http: HttpClient) {}
  createRoot() {
    const token = localStorage.getItem('jwt');
    const headers = new HttpHeaders({
      Authorization: `Bearer ${token}`
    });
    return this.http.post(`${this.api}/api/admin/ca/root`, { }, { headers, responseType: 'text' });
  }
  getRootStatus() {
    const token = localStorage.getItem('jwt');
    const headers = new HttpHeaders({
      Authorization: `Bearer ${token}`
    });
    return this.http.get<any>(`${this.api}/api/dev/ca/root/status`, { headers });
  }


  getAllCaUsers(): Observable<CaUser[]> {
    const token = localStorage.getItem('jwt');
    const headers = new HttpHeaders({
      Authorization: `Bearer ${token}`
    });
    return this.http.get<CaUser[]>(`${this.api}/api/admin/ca/users`, { headers });
  }

  issueCaCertificate(email: string, subjectDn: string, pathLenConstraint: number): Observable<any> {
    const token = localStorage.getItem('jwt');
    const headers = new HttpHeaders({
      Authorization: `Bearer ${token}`
    });

    const body = {
      subjectDn,
      pathLenConstraint
    };

    return this.http.post(`${this.api}/api/admin/ca/${email}/issue-ca`, body, { headers });
  }

  getSubordinateCaUsers(): Observable<CaUser[]> {
    const token = localStorage.getItem('jwt');
    const headers = new HttpHeaders({
      Authorization: `Bearer ${token}`
    });
    return this.http.get<CaUser[]>(`${this.api}/api/ca/subordinates`, { headers });
  }

  // Izdaje Sub-CA sertifikat podređenom korisniku
  // issueSubCaCertificate(email: string, subjectDn: string): Observable<any> {
  //   const token = localStorage.getItem('jwt');
  //   const headers = new HttpHeaders({
  //     Authorization: `Bearer ${token}`
  //   });
  //   return this.http.post(`${this.api}/api/ca/issue-subca/${email}`, { subjectDn }, { headers });
  // }
   issueSubCaCertificate(email: string, subjectDn: string, templateId?: number): Observable<any> {
    const token = localStorage.getItem('jwt');
    const headers = new HttpHeaders({ Authorization: `Bearer ${token}` });

    const body: any = { subjectDn };
    if (templateId) body.templateId = templateId;

    return this.http.post(`${this.api}/api/ca/issue-subca/${email}`, body, { headers });
  }

  revokeSubordinateCertificate(serial: string, reason: string) {
    const token = localStorage.getItem('jwt');
    const headers = new HttpHeaders({ Authorization: `Bearer ${token}` });
    return this.http.post(
      `${this.api}/api/ca/revoke/${serial}`,
      { reason },
      { headers, responseType: 'text' }
    );
  }


  createSubordinateUser(userData: any): Observable<any> {
    const token = localStorage.getItem('jwt');
    const headers = new HttpHeaders({
      Authorization: `Bearer ${token}`
    });
    return this.http.post(`${this.api}/api/ca/create-subordinate`, userData, { headers });
  }

  revokeCertificate(serialHex: string, reason: string): Observable<any> {
    const token = localStorage.getItem('jwt');
    const headers = new HttpHeaders({
      Authorization: `Bearer ${token}`
    });
    const url = `${this.api}/api/admin/revoke/${serialHex}?reason=${encodeURIComponent(reason)}`;
    return this.http.post(url, {}, { headers, responseType: 'text' });
  }

  checkRevoked(serialHex: string): Observable<boolean> {
    const token = localStorage.getItem('jwt');
    const headers = new HttpHeaders({
      Authorization: `Bearer ${token}`
    });
    return this.http.get<boolean>(`${this.api}/api/admin/revoke/status/${serialHex}`, { headers });
  }

  createTemplate(template: any) {
  const headers = this.getAuthHeaders();
  return this.http.post(`${this.api}/api/ca/templates`, template, { headers });
  }

  getTemplatesForIssuer(issuerId: number) {
    const headers = this.getAuthHeaders();
    return this.http.get(`${this.api}/api/ca/templates/issuer/${issuerId}`, { headers });
  }

  private getAuthHeaders() {
    const token = localStorage.getItem('jwt');
    return new HttpHeaders({ Authorization: `Bearer ${token}` });
  }

  getAllTemplates() {
    const headers = this.getAuthHeaders();
    return this.http.get<any[]>(`${this.api}/api/ca/templates`, { headers });
  }
  getTemplates(): Observable<any[]> {
    const token = localStorage.getItem('jwt');
    const headers = new HttpHeaders({ Authorization: `Bearer ${token}` });
    return this.http.get<any[]>(`${this.api}/api/ca/templates`, { headers });
  }

 
}
