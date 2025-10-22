import { Routes } from '@angular/router';
import { AuthGuard } from './guards/auth.guard'; // obavezno dodaj import

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'login' },

  // JAVNE RUTE (bez guard-a)
  {
    path: 'register',
    loadComponent: () =>
      import('./auth/register/register').then(m => m.Register)
  },
  {
    path: 'login',
    loadComponent: () =>
      import('./auth/login/login').then(m => m.Login)
  },
  {
    path: 'forgot-password',
    loadComponent: () =>
      import('./auth/forgot-password/forgot-password').then(m => m.ForgotPassword)
  },
  {
    path: 'reset-password',
    loadComponent: () =>
      import('./auth/reset-password/reset-password').then(m => m.ResetPassword)
  },

  // ZAŠTIĆENE RUTE (guardovane)
  {
    path: '',
    loadComponent: () =>
      import('./home/home/home').then(m => m.HomeComponent),
    canActivate: [AuthGuard],
    children: [
      {
        path: 'dashboard',
        loadComponent: () =>
          import('./dashboard/dashboard').then(m => m.DashboardComponent),
        canActivate: [AuthGuard]
      },
      {
        path: 'admin',
        loadComponent: () =>
          import('./admin/admin.component').then(m => m.AdminComponent),
        canActivate: [AuthGuard]
      },
      {
        path: 'csr-upload',
        loadComponent: () =>
          import('./csr/csr-upload/csr-upload').then(m => m.CsrUploadComponent),
        canActivate: [AuthGuard]
      },
      {
        path: 'my-requests',
        loadComponent: () =>
          import('./csr/my-requests/my-requests').then(m => m.MyRequestsComponent),
        canActivate: [AuthGuard]
      },
      {
        path: 'admin-csr',
        loadComponent: () =>
          import('./csr/admin-csr/admin-csr').then(m => m.AdminRequestsComponent),
        canActivate: [AuthGuard]
      },
      {
        path: 'ca-users',
        loadComponent: () =>
          import('./ca-users/ca-users').then(m => m.CaUsers),
        canActivate: [AuthGuard]
      },
      {
        path: 'ca-subordinates',
        loadComponent: () =>
          import('./ca-subordinates/ca-subordinates').then(m => m.CaSubordinatesComponent),
        canActivate: [AuthGuard]
      },
      {
        path: 'add-subordinates',
        loadComponent: () =>
          import('./add-subordinates/add-subordinates').then(m => m.AddSubordinateComponent),
        canActivate: [AuthGuard]
      },
      {
        path: 'create-ca-user',
        loadComponent: () =>
          import('./create-ca-user/create-ca-user').then(m => m.CreateCaUserComponent),
        canActivate: [AuthGuard]
      },
      {
        path: 'change-password',
        loadComponent: () =>
          import('./auth/change-password/change-password').then(m => m.ChangePasswordComponent),
        canActivate: [AuthGuard]
      },
      {
        path: 'ca-csr',
        loadComponent: () =>
          import('./csr/ca-csr/ca-csr').then(m => m.CaRequestsComponent),
        canActivate: [AuthGuard]
      },
      {
        path: 'passwords',
        loadComponent: () =>
          import('./passwords/password-list/password-list').then(m => m.PasswordListComponent),
        canActivate: [AuthGuard]
      },
      {
        path: 'passwords/create',
        loadComponent: () =>
          import('./passwords/password-create/password-create').then(m => m.PasswordCreateComponent),
        canActivate: [AuthGuard]
      },
      {
        path: 'passwords/:id',
        loadComponent: () =>
          import('./passwords/password-detail/password-detail').then(m => m.PasswordDetailComponent),
        canActivate: [AuthGuard]
      },
      {
        path: 'passwords/:id/share',
        loadComponent: () =>
          import('./passwords/password-share-dialog/password-share-dialog').then(m => m.PasswordShareDialogComponent),
        canActivate: [AuthGuard]
      },

      { path: '', redirectTo: 'dashboard', pathMatch: 'full' }
    ]
  },

  // fallback – ako ruta ne postoji
  { path: '**', redirectTo: 'login' }
];
