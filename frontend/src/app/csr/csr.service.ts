import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface CaOption {
  id: number;
  subjectDn: string;
  notBefore: string;
  notAfter: string;
}

@Injectable({
  providedIn: 'root'
})
export class CsrService {
  private http = inject(HttpClient);
  private baseUrl = 'http://localhost:8080/api';

  getCaList(): Observable<CaOption[]> {
    return this.http.get<CaOption[]>(`${this.baseUrl}/ca/list`);
  }

  uploadCsr(formData: FormData): Observable<any> {
    return this.http.post(`${this.baseUrl}/csr/upload`, formData);
  }
}
