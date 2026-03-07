import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./support.component').then(m => m.SupportComponent),
    data: {
      title: 'Soporte Técnico'
    }
  }
];