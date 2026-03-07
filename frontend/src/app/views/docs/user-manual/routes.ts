import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./user-manual.component').then(m => m.UserManualComponent),
    data: {
      title: 'Manual de Usuario'
    }
  }
];