//src/app/shared/components/page-header/page-header.component.ts
import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

/**
 * Componente de header de páginas
 * 
 * Incluye: Título, breadcrumbs, botones de acción
 * 
 * Uso:
 * <app-page-header
 *   title="Usuarios"
 *   [breadcrumbs]="[
 *     { label: 'Inicio', url: '/dashboard' },
 *     { label: 'Usuarios', url: '/users' }
 *   ]">
 *   <button class="btn btn-primary">
 *     <i class="fas fa-plus"></i> Nuevo Usuario
 *   </button>
 * </app-page-header>
 */
@Component({
  selector: 'app-page-header',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './page-header.component.html',
  styleUrls: ['./page-header.component.css']
})
export class PageHeaderComponent {
  
  @Input() title: string = '';
  @Input() subtitle?: string;
  @Input() breadcrumbs: Breadcrumb[] = [];
  @Input() icon?: string;

  constructor() {}
}

/**
 * Interface para breadcrumbs
 */
export interface Breadcrumb {
  label: string;
  url?: string;
  active?: boolean;
}