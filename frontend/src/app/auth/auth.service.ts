import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface RegisterRequest {
  email: string;
  firstName: string;
  lastName: string;
  organization: string;
  password: string;
  confirmPassword: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
 
  private readonly API = 'http://localhost:8080/api/auth';

  constructor(private http: HttpClient) {}

  register(payload: RegisterRequest): Observable<void> {
    return this.http.post<void>(`${this.API}/register`, payload);
  }
}
