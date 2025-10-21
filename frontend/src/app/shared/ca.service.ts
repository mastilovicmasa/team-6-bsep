import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CaUser } from './ca-user.model';

@Injectable({ providedIn: 'root' })
export class CaService {
  private api = 'http://localhost:8443';
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
  issueSubCaCertificate(email: string, subjectDn: string): Observable<any> {
    const token = localStorage.getItem('jwt');
    const headers = new HttpHeaders({
      Authorization: `Bearer ${token}`
    });
    return this.http.post(`${this.api}/api/ca/issue-subca/${email}`, { subjectDn }, { headers });
  }

  createSubordinateUser(userData: any): Observable<any> {
    const token = localStorage.getItem('jwt');
    const headers = new HttpHeaders({
      Authorization: `Bearer ${token}`
    });
    return this.http.post(`${this.api}/api/ca/create-subordinate`, userData, { headers });
  }


}
