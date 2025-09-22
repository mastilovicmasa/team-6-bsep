import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';

@Injectable({ providedIn: 'root' })
export class CaService {
  private api = 'http://localhost:8080';
  constructor(private http: HttpClient) {}
  createRoot() {
    return this.http.post(`${this.api}/api/admin/ca/root`, {}, { responseType: 'text' });
  }
  getRootStatus() {
    const token = localStorage.getItem('jwt');
    const headers = new HttpHeaders({
      Authorization: `Bearer ${token}`
    });
    return this.http.get<any>(`${this.api}/api/dev/ca/root/status`, { headers });
  }
}
