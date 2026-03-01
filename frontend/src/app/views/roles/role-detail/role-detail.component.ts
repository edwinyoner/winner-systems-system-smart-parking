import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, ActivatedRoute } from '@angular/router';

// Services
import { RoleService } from '../../../core/services/role.service';
import { AuthContextService } from '../../../core/services/auth-context.service';

// Models
import { Role, Permission } from '../../../core/models/role.model'; 

// Components
import { AlertMessageComponent } from '../../../shared/components/alert-message/alert-message.component';

/**
 * Interfaz para agrupar permisos por módulo
 */
interface PermissionsByModule {
  [module: string]: Permission[];
}

@Component({
  selector: 'app-role-detail',
  standalone: true,
  imports: [
    CommonModule,
    AlertMessageComponent
  ],
  templateUrl: './role-detail.component.html',
  styleUrls: ['./role-detail.component.scss']
})
export class RoleDetailComponent implements OnInit {
  
  roleId!: number;
  role: Role | null = null;
  permissionsByModule: PermissionsByModule = {};
  
  isLoading = false;
  errorMessage: string | null = null;
  
  constructor(
    private roleService: RoleService,
    private authContext: AuthContextService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.getRoleId();
    this.loadRole();
  }

  /**
   * Control de permisos (patrón aprendido)
   */
  hasPermission(permission: string): boolean {
    return this.authContext.hasPermission(permission);
  }

  private getRoleId(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.roleId = Number(id);
    } else {
      this.router.navigate(['/roles']);
    }
  }

  private loadRole(): void {
    this.isLoading = true;
    
    this.roleService.getRoleById(this.roleId).subscribe({
      next: (role) => {
        this.role = role;
        this.groupPermissionsByModule(role.permissions);
        this.isLoading = false;
      },
      error: (error) => {
        this.isLoading = false;
        this.errorMessage = 'Error al cargar la información del rol';
        console.error('Error loading role:', error);
      }
    });
  }

  /**
   * Agrupa los permisos por módulo
   */
  private groupPermissionsByModule(permissions: Permission[]): void {
    this.permissionsByModule = {};
    
    permissions.forEach(permission => {
      // Extraer el módulo del nombre del permiso (ej: "users.create" → "users")
      const parts = permission.name.split('.');
      const module = parts.length > 1 ? parts[0] : 'general';
      
      if (!this.permissionsByModule[module]) {
        this.permissionsByModule[module] = [];
      }
      
      this.permissionsByModule[module].push(permission);
    });
  }

  /**
   * Obtiene las keys de los módulos ordenadas
   */
  getModules(): string[] {
    return Object.keys(this.permissionsByModule).sort();
  }

  /**
   * Obtiene los permisos de un módulo específico
   */
  getModulePermissions(module: string): Permission[] {
    return this.permissionsByModule[module] || [];
  }

  /**
   * Formatea el nombre del módulo para mostrar
   */
  formatModuleName(module: string): string {
    return module.charAt(0).toUpperCase() + module.slice(1);
  }

  /**
   * Verifica si el rol es de sistema (no editable)
   */
  isSystemRole(): boolean {
    return this.role?.name === 'ADMIN';
  }

  /**
   * Navega a la edición del rol
   */
  editRole(): void {
    this.router.navigate(['/roles', this.roleId, 'edit']);
  }

  /**
   * Vuelve a la lista de roles
   */
  goBack(): void {
    this.router.navigate(['/roles']);
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