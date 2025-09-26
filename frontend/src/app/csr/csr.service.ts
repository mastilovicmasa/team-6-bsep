import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface CaOption {
  id: number;
  subjectDn: string;
  notBefore: string;
  notAfter: string;
}

export interface CsrRequestDto {
  id: number;
  subjectCn: string;
  subjectO: string;
  subjectC: string;
  durationInDays: number;
  status: 'PENDING' | 'ISSUED' | 'REJECTED';
  createdAt: string;
}

@Injectable({
  providedIn: 'root'
})
export class CsrService {
  private http = inject(HttpClient);
  private baseUrl = 'http://localhost:8080/api';

  getCaList(): Observable<CaOption[]> {
    const token = localStorage.getItem('jwt');
    const headers = new HttpHeaders({
      Authorization: `Bearer ${token}`
    });
    return this.http.get<CaOption[]>(`${this.baseUrl}/ca/list`, { headers });
  }

  uploadCsr(formData: FormData): Observable<any> {
    const token = localStorage.getItem('jwt');
    const headers = new HttpHeaders({
      Authorization: `Bearer ${token}`
    });
    return this.http.post(`${this.baseUrl}/csr/upload`, formData, { headers });
  }

  getMyRequests(): Observable<CsrRequestDto[]> {
    const token = localStorage.getItem('jwt');
    const headers = new HttpHeaders({
      Authorization: `Bearer ${token}`
    });
    return this.http.get<CsrRequestDto[]>(`${this.baseUrl}/csr/my-requests`, { headers });
  }

  getAllRequests(): Observable<CsrRequestDto[]> {
    const token = localStorage.getItem('jwt');
    const headers = new HttpHeaders({
      Authorization: `Bearer ${token}`
    });
    return this.http.get<CsrRequestDto[]>(`${this.baseUrl}/csr/all`, { headers });
  }


}
