import { Routes } from '@angular/router';
import { DefaultLayoutComponent } from './layout/default-layout/default-layout.component';
import { AuthLayoutComponent } from './layout/auth-layout/auth-layout.component';
import { AuthGuard } from './core/guards/auth.guard';
import { NoAuthGuard } from './core/guards/no-auth.guard';
import { RoleGuard } from './core/guards/role.guard';

export const routes: Routes = [
  // ========== REDIRECT INICIAL ==========
  {
    path: '',
    redirectTo: 'dashboard',
    pathMatch: 'full'
  },

  // ========== RUTAS PÚBLICAS (Auth Layout - Sin Sidebar) ==========
  {
    path: '',
    component: AuthLayoutComponent,
    canActivate: [NoAuthGuard], // Solo accesible si NO está autenticado
    children: [
      {
        path: 'login',
        loadChildren: () => import('./views/auth/routes').then(m => m.loginRoutes),
        data: { title: 'Iniciar Sesión' }
      },
      {
        path: 'register',
        loadChildren: () => import('./views/auth/routes').then(m => m.registerRoutes),
        data: { title: 'Crear Cuenta' }
      },
      {
        path: 'forgot-password',
        loadChildren: () => import('./views/auth/routes').then(m => m.forgotPasswordRoutes),
        data: { title: 'Recuperar Contraseña' }
      },
      {
        path: 'reset-password',
        loadChildren: () => import('./views/auth/routes').then(m => m.resetPasswordRoutes),
        data: { title: 'Restablecer Contraseña' }
      },
      {
        path: 'verify-email',
        loadChildren: () => import('./views/auth/routes').then(m => m.verifyEmailRoutes),
        data: { title: 'Verificar Email' }
      }
    ]
  },

  // ========== RUTAS PRIVADAS (Default Layout - Con Sidebar) ==========
  {
    path: '',
    component: DefaultLayoutComponent,
    canActivate: [AuthGuard], // Solo accesible si está autenticado
    children: [
      // Dashboard (Acceso para todos los usuarios autenticados)
      {
        path: 'dashboard',
        loadChildren: () => import('./views/dashboard/routes').then(m => m.routes),
        data: { title: 'Dashboard' }
      },

      // Usuarios (Requiere permiso users.read)
      {
        path: 'users',
        loadChildren: () => import('./views/users/routes').then(m => m.routes),
        canActivate: [RoleGuard],
        data: { 
          title: 'Usuarios',
          requiredPermissions: ['users.read']
        }
      },

      // Roles (Requiere permiso roles.read)
      {
        path: 'roles',
        loadChildren: () => import('./views/roles/routes').then(m => m.routes),
        canActivate: [RoleGuard],
        data: { 
          title: 'Roles',
          requiredPermissions: ['roles.read']
        }
      },

      // Permisos (Requiere permiso roles.read o ser ADMIN)
      {
        path: 'permissions',
        loadChildren: () => import('./views/permissions/routes').then(m => m.routes),
        canActivate: [RoleGuard],
        data: { 
          title: 'Permisos',
          requiredPermissions: ['roles.read']
        }
      },

      // Perfil (Acceso para todos los usuarios autenticados)
      {
        path: 'profile',
        loadChildren: () => import('./views/profile/routes').then(m => m.routes),
        data: { title: 'Mi Perfil' }
      },
      {
        path: '',
        loadChildren: () => import('./views/parking-service/routes').then((m) => m.parkingRoutes)
      },

      {
  path: 'support',
  loadChildren: () => import('./views/support/routes').then(m => m.routes),
  canActivate: [AuthGuard]
},
{
  path: 'docs/user-manual',
  loadChildren: () => import('./views/docs/user-manual/routes').then(m => m.routes),
  canActivate: [AuthGuard]
},
{
  path: 'about',
  loadChildren: () => import('./views/about/routes').then(m => m.routes),
  canActivate: [AuthGuard]
}
    ]
  },

  // ========== PÁGINAS DE ERROR (Futuro) ==========
  // {
  //   path: '403',
  //   loadComponent: () => import('./views/errors/page403/page403.component').then(m => m.Page403Component),
  //   data: { title: 'Acceso Denegado' }
  // },
  // {
  //   path: '404',
  //   loadComponent: () => import('./views/errors/page404/page404.component').then(m => m.Page404Component),
  //   data: { title: 'Página No Encontrada' }
  // },
  // {
  //   path: '500',
  //   loadComponent: () => import('./views/errors/page500/page500.component').then(m => m.Page500Component),
  //   data: { title: 'Error del Servidor' }
  // },

  // ========== FALLBACK (404) ==========
  {
    path: '**',
    redirectTo: 'dashboard'
  }
];
