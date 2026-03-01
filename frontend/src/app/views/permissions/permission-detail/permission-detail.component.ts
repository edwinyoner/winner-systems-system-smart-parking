import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, ActivatedRoute } from '@angular/router';

// Services
import { PermissionService } from '../../../core/services/permission.service';
import { AuthContextService } from '../../../core/services/auth-context.service';

// Models
import { Permission } from '../../../core/models/permission.model';

// Components
import { AlertMessageComponent } from '../../../shared/components/alert-message/alert-message.component';

@Component({
  selector: 'app-permission-detail',
  standalone: true,
  imports: [
    CommonModule,
    AlertMessageComponent
  ],
  templateUrl: './permission-detail.component.html',
  styleUrls: ['./permission-detail.component.scss']
})
export class PermissionDetailComponent implements OnInit {
  
  permissionId!: number;
  permission: Permission | null = null;
  
  isLoading = false;
  errorMessage: string | null = null;

  // Módulos disponibles para mostrar label
  availableModules: { [key: string]: string } = {
    'users': 'Usuarios',
    'roles': 'Roles',
    'permissions': 'Permisos',
    'parking': 'Estacionamiento',
    'rates': 'Tarifas',
    'reports': 'Reportes',
    'sensors': 'Sensores',
    'cameras': 'Cámaras',
    'barriers': 'Barreras',
    'dashboard': 'Dashboard'
  };
  
  constructor(
    private permissionService: PermissionService,
    private authContext: AuthContextService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.getPermissionId();
    this.loadPermission();
  }

  /**
   * Control de permisos (patrón aprendido)
   */
  hasPermission(permission: string): boolean {
    return this.authContext.hasPermission(permission);
  }

  private getPermissionId(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.permissionId = Number(id);
    } else {
      this.router.navigate(['/permissions']);
    }
  }

  private loadPermission(): void {
    this.isLoading = true;
    
    this.permissionService.getPermissionById(this.permissionId).subscribe({
      next: (permission) => {
        this.permission = permission;
        this.isLoading = false;
      },
      error: (error) => {
        this.isLoading = false;
        this.errorMessage = 'Error al cargar la información del permiso';
        console.error('Error loading permission:', error);
      }
    });
  }

  /**
   * Obtiene el nombre legible del módulo
   */
  getModuleLabel(): string {
    if (!this.permission?.module) return 'Sin módulo';
    return this.availableModules[this.permission.module] || this.permission.module;
  }

  /**
   * Obtiene la clase CSS del badge según el módulo
   */
  getModuleBadgeClass(): string {
    if (!this.permission?.module) return 'bg-secondary';
    
    const colors: { [key: string]: string } = {
      'users': 'bg-primary',
      'roles': 'bg-success',
      'permissions': 'bg-warning',
      'parking': 'bg-info',
      'rates': 'bg-secondary',
      'reports': 'bg-dark',
      'sensors': 'bg-danger',
      'cameras': 'bg-primary',
      'barriers': 'bg-success',
      'dashboard': 'bg-info'
    };
    
    return colors[this.permission.module] || 'bg-secondary';
  }

  /**
   * Extrae el módulo y acción del nombre del permiso
   * Ejemplo: "users.create" → { module: "users", action: "create" }
   */
  parsePermissionName(): { module: string; action: string } | null {
    if (!this.permission) return null;
    
    const parts = this.permission.name.split('.');
    if (parts.length === 2) {
      return {
        module: parts[0],
        action: parts[1]
      };
    }
    
    return null;
  }

  /**
   * Obtiene el label de la acción
   */
  getActionLabel(): string {
    const parsed = this.parsePermissionName();
    if (!parsed) return 'N/A';
    
    const actions: { [key: string]: string } = {
      'create': 'Crear',
      'read': 'Leer',
      'update': 'Actualizar',
      'delete': 'Eliminar',
      'export': 'Exportar',
      'import': 'Importar',
      'restore': 'Restaurar',
      'approve': 'Aprobar',
      'reject': 'Rechazar'
    };
    
    return actions[parsed.action] || parsed.action;
  }

  /**
   * Navega a la edición del permiso
   */
  editPermission(): void {
    this.router.navigate(['/permissions', this.permissionId, 'edit']);
  }

  /**
   * Vuelve a la lista de permisos
   */
  goBack(): void {
    this.router.navigate(['/permissions']);
  }

  /**
   * Formatea una fecha a formato local
   */
  formatDate(date: string): string {
    if (!date) return 'N/A';
    return new Date(date).toLocaleDateString('es-PE', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  dismissError(): void {
    this.errorMessage = null;
  }
}