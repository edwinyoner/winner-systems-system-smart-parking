import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';

// Services
import { RoleService } from '../../../core/services/role.service';
import { PermissionService } from '../../../core/services/permission.service';
import { AuthContextService } from '../../../core/services/auth-context.service';

// Models
import { CreateRoleRequest } from '../../../core/models/role.model';
import { PermissionGroup } from '../../../core/models/permission.model';

// Components
import { AlertMessageComponent } from '../../../shared/components/alert-message/alert-message.component';

@Component({
  selector: 'app-role-create',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    AlertMessageComponent
  ],
  templateUrl: './role-create.component.html',
  styleUrls: ['./role-create.component.scss']
})
export class RoleCreateComponent implements OnInit {
  
  roleForm!: FormGroup;
  permissionGroups: PermissionGroup[] = [];
  selectedPermissions: Set<number> = new Set();
  
  isLoading = false;
  isLoadingPermissions = false;
  errorMessage: string | null = null;
  
  constructor(
    private fb: FormBuilder,
    private roleService: RoleService,
    private permissionService: PermissionService,
    private authContext: AuthContextService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.initForm();
    this.loadPermissions();
  }

  /**
   * Control de permisos (patrón aprendido)
   */
  hasPermission(permission: string): boolean {
    return this.authContext.hasPermission(permission);
  }

  private initForm(): void {
    this.roleForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(50)]],
      description: ['', [Validators.maxLength(255)]],
      status: [true]
    });
  }

  private loadPermissions(): void {
    this.isLoadingPermissions = true;
    
    this.permissionService.getPermissionsGrouped().subscribe({
      next: (groups) => {
        this.permissionGroups = groups;
        this.isLoadingPermissions = false;
      },
      error: (error) => {
        this.isLoadingPermissions = false;
        this.errorMessage = 'Error al cargar los permisos del sistema';
        console.error('Error loading permissions:', error);
      }
    });
  }

  // ========== GESTIÓN DE PERMISOS ==========

  /**
   * Verifica si un permiso está seleccionado
   */
  isPermissionSelected(permissionId: number): boolean {
    return this.selectedPermissions.has(permissionId);
  }

  /**
   * Toggle de un permiso individual
   */
  togglePermission(permissionId: number): void {
    if (this.selectedPermissions.has(permissionId)) {
      this.selectedPermissions.delete(permissionId);
    } else {
      this.selectedPermissions.add(permissionId);
    }
  }

  /**
   * Verifica si todos los permisos de un módulo están seleccionados
   */
  areAllModulePermissionsSelected(group: PermissionGroup): boolean {
    return group.permissions.every(p => this.selectedPermissions.has(p.id));
  }

  /**
   * Toggle todos los permisos de un módulo
   */
  toggleModulePermissions(group: PermissionGroup): void {
    const allSelected = this.areAllModulePermissionsSelected(group);
    
    if (allSelected) {
      // Deseleccionar todos
      group.permissions.forEach(p => this.selectedPermissions.delete(p.id));
    } else {
      // Seleccionar todos
      group.permissions.forEach(p => this.selectedPermissions.add(p.id));
    }
  }

  /**
   * Selecciona todos los permisos del sistema
   */
  selectAllPermissions(): void {
    this.permissionGroups.forEach(group => {
      group.permissions.forEach(p => this.selectedPermissions.add(p.id));
    });
  }

  /**
   * Deselecciona todos los permisos
   */
  clearAllPermissions(): void {
    this.selectedPermissions.clear();
  }

  /**
   * Obtiene el conteo de permisos seleccionados
   */
  getSelectedPermissionsCount(): number {
    return this.selectedPermissions.size;
  }

  // ========== FORMULARIO ==========

  onSubmit(): void {
    // Validar formulario
    if (this.roleForm.invalid) {
      this.markFormGroupTouched(this.roleForm);
      return;
    }

    // Validar que se haya seleccionado al menos un permiso
    if (this.selectedPermissions.size === 0) {
      this.errorMessage = 'Debes seleccionar al menos un permiso para el rol';
      return;
    }

    this.isLoading = true;
    this.errorMessage = null;

    const request: CreateRoleRequest = {
      name: this.roleForm.value.name.trim(),
      description: this.roleForm.value.description?.trim() || null,
      status: this.roleForm.value.status,
      permissionIds: Array.from(this.selectedPermissions)
    };

    this.roleService.createRole(request).subscribe({
      next: (role) => {
        this.isLoading = false;
        
        // Navegar a la lista con mensaje de éxito
        this.router.navigate(['/roles'], {
          state: {
            successMessage: `Rol "${role.name}" creado exitosamente con ${this.selectedPermissions.size} permisos`
          }
        });
      },
      error: (error) => {
        this.isLoading = false;
        this.errorMessage = error.error?.message || 'Error al crear el rol. Verifica que el nombre no esté duplicado.';
        console.error('Error creating role:', error);
      }
    });
  }

  cancel(): void {
    this.router.navigate(['/roles']);
  }

  private markFormGroupTouched(formGroup: FormGroup): void {
    Object.keys(formGroup.controls).forEach(key => {
      const control = formGroup.get(key);
      control?.markAsTouched();

      if (control instanceof FormGroup) {
        this.markFormGroupTouched(control);
      }
    });
  }

  // ========== VALIDACIONES ==========

  hasError(field: string, error: string): boolean {
    const control = this.roleForm.get(field);
    return !!(control?.hasError(error) && control?.touched);
  }

  getErrorMessage(field: string): string {
    const control = this.roleForm.get(field);

    if (control?.hasError('required')) {
      return 'Este campo es requerido';
    }

    if (control?.hasError('minlength')) {
      const minLength = control.errors?.['minlength'].requiredLength;
      return `Mínimo ${minLength} caracteres`;
    }

    if (control?.hasError('maxlength')) {
      const maxLength = control.errors?.['maxlength'].requiredLength;
      return `Máximo ${maxLength} caracteres`;
    }

    return '';
  }

  dismissError(): void {
    this.errorMessage = null;
  }
}