import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'login' },

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
        path: 'ca-users',
        loadComponent: () =>
          import('./ca-users/ca-users').then(m => m.CaUsers)
      },
      {
        path: 'ca-subordinates',
        loadComponent: () =>
          import('./ca-subordinates/ca-subordinates').then(m => m.CaSubordinatesComponent)
      },
      {
        path: 'add-subordinates',
        loadComponent: () =>
          import('./add-subordinates/add-subordinates').then(m => m.AddSubordinateComponent)
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
      {
        path: 'ca-csr',
        loadComponent: () =>
          import('./csr/ca-csr/ca-csr').then(m => m.CaRequestsComponent)
      },
      {
        path: 'passwords',
        loadComponent: () =>
          import('./passwords/password-list/password-list').then(m => m.PasswordListComponent)
      },
      {
        path: 'passwords/create',
        loadComponent: () =>
          import('./passwords/password-create/password-create').then(m => m.PasswordCreateComponent)
      },
      {
        path: 'passwords/:id',
        loadComponent: () =>
          import('./passwords/password-detail/password-detail').then(m => m.PasswordDetailComponent)
      },
      {
        path: 'passwords/:id/share',
        loadComponent: () =>
          import('./passwords/password-share-dialog/password-share-dialog').then(m => m.PasswordShareDialogComponent)
      },

      { path: '', redirectTo: 'dashboard', pathMatch: 'full' }
    ]
  },
  { path: '**', redirectTo: 'dashboard' }
];
