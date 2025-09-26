import { Routes } from '@angular/router';

export const routes: Routes = [
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
  {
    path: '',
    loadComponent: () =>
      import('./home/home/home').then(m => m.HomeComponent),
    children: [
      {
        path: 'dashboard',
        loadComponent: () =>
          import('./dashboard/dashboard').then(m => m.DashboardComponent)
      },
      {
        path: 'admin',
        loadComponent: () =>
          import('./admin/admin.component').then(m => m.AdminComponent)
      },
      {
        path: 'csr-upload',
        loadComponent: () =>
          import('./csr/csr-upload/csr-upload').then(m => m.CsrUploadComponent)
      },
      {
        path: 'my-requests',
        loadComponent: () =>
          import('./csr/my-requests/my-requests').then(m => m.MyRequestsComponent)
      },
      {
        path: 'admin-csr',
        loadComponent: () =>
          import('./csr/admin-csr/admin-csr').then(m => m.AdminRequestsComponent)
      },
      {
        path: 'create-ca-user',
        loadComponent: () =>
          import('./create-ca-user/create-ca-user').then(m => m.CreateCaUserComponent)
      },
      {
        path: 'change-password',
        loadComponent: () =>
          import('./auth/change-password/change-password').then(m => m.ChangePasswordComponent)
      },
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' }
    ]
  },
  { path: '', pathMatch: 'full', redirectTo: 'login' },
  { path: '**', redirectTo: 'dashboard' }
];
