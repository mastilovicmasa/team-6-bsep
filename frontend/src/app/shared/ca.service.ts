import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { CertificateRequest } from '../model/certificate-request.model';

@Injectable({ providedIn: 'root' })
export class CaService {
  private api = 'http://localhost:8080';
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

   getAllCas() {
    const token = localStorage.getItem('jwt');
    const headers = new HttpHeaders({
      Authorization: `Bearer ${token}`
    });
    return this.http.get<any[]>(`${this.api}/api/ca/list`, { headers });
  }

  issueIntermediate(request: CertificateRequest) {
    const token = localStorage.getItem('jwt');
    const headers = new HttpHeaders({
      Authorization: `Bearer ${token}`,
      'Content-Type': 'application/json'
    });

    return this.http.post<any>(
      `${this.api}/api/ca/issue-intermediate`,
      request,
      { headers }
    );
  }


}
