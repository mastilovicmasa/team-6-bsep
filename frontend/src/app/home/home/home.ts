import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../auth/auth.service';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './home.html',
  styleUrls: ['./home.css']
})
export class HomeComponent implements OnInit {
  private auth = inject(AuthService);

  role: string | null = null;

  ngOnInit(): void {
    const token = localStorage.getItem('jwt');
    if (token) {
      const payload = this.auth.logJwtPayload(token);
      this.role = payload?.role || null;
    }
  }

  logout() {
    localStorage.removeItem('jwt');
    localStorage.removeItem('jti');
    this.role = null;
    window.location.href = '/login'; // ili koristi Router.navigate
  }
}
