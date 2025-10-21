import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { 
  PasswordEntry, 
  PasswordCreateRequest, 
  PasswordShare, 
  PasswordShareRequest,
  SharedPassword
} from './password.model';

@Injectable({
  providedIn: 'root'
})
export class PasswordService {

  private baseUrl = 'http://localhost:8080/api/passwords';
  private usersUrl = 'http://localhost:8080/api/users';

  constructor(private http: HttpClient) {}

  // Pomoćna funkcija za Authorization header
  private getAuthHeaders(): HttpHeaders {
    const token = localStorage.getItem('jwt');
    return new HttpHeaders({
      Authorization: `Bearer ${token}`
    });
  }

  getMyPasswords(): Observable<PasswordEntry[]> {
    const token = localStorage.getItem('jwt');
    const headers = new HttpHeaders({ Authorization: `Bearer ${token}` });
    return this.http.get<PasswordEntry[]>(`${this.baseUrl}/owned`, { headers });
  }

  getSharedPasswords(): Observable<SharedPassword[]> {
    const token = localStorage.getItem('jwt');
    const headers = new HttpHeaders({ Authorization: `Bearer ${token}` });
    return this.http.get<SharedPassword[]>(`${this.baseUrl}/shared`, { headers });
  }
  // Dohvatanje jedne lozinke po ID-ju
  getPasswordById(id: number): Observable<any> {
    const headers = this.getAuthHeaders();
    return this.http.get<any>(`${this.baseUrl}/${id}`, { headers });
  }

  // Kreiranje nove lozinke
  createPassword(request: PasswordCreateRequest): Observable<PasswordEntry> {
    const headers = this.getAuthHeaders();
    return this.http.post<PasswordEntry>(`${this.baseUrl}`, request, { headers });
  }

  // Deljenje lozinke sa drugim korisnikom
  sharePassword(entryId: number, request: PasswordShareRequest): Observable<PasswordShare> {
    const headers = this.getAuthHeaders();
    return this.http.post<PasswordShare>(`${this.baseUrl}/${entryId}/share`, request, { headers });
  }

  // Povlačenje (revoke) deljenja
  revokeShare(shareId: number): Observable<void> {
    const headers = this.getAuthHeaders();
    return this.http.delete<void>(`${this.baseUrl}/share/${shareId}`, { headers });
  }

  // Preuzimanje javnog sertifikata (PEM)
  getUserCertificate(): Observable<string> {
    const headers = this.getAuthHeaders();
    return this.http.get(`${this.usersUrl}/public-key`, { headers, responseType: 'text' });
  }

  getUserPublicKeyByEmail(email: string): Observable<string> {
    const headers = this.getAuthHeaders();
    return this.http.get(`${this.usersUrl}/public-key/${email}`, { headers, responseType: 'text' });
  }

}
