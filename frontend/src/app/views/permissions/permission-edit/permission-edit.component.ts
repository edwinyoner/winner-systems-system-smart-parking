import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';

// Services
import { PermissionService } from '../../../core/services/permission.service';
import { AuthContextService } from '../../../core/services/auth-context.service';

// Models
import { Permission, UpdatePermissionRequest } from '../../../core/models/permission.model';

// Components
import { AlertMessageComponent } from '../../../shared/components/alert-message/alert-message.component';

@Component({
  selector: 'app-permission-edit',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    AlertMessageComponent
  ],
  templateUrl: './permission-edit.component.html',
  styleUrls: ['./permission-edit.component.scss']
})
export class PermissionEditComponent implements OnInit {
  
  permissionForm!: FormGroup;
  
  permissionId!: number;
  permission: Permission | null = null;
  
  isLoading = false;
  isLoadingPermission = false;
  errorMessage: string | null = null;
  
  // Módulos predefinidos del sistema
  availableModules = [
    { value: 'users', label: 'Usuarios', icon: 'fa-users' },
    { value: 'roles', label: 'Roles', icon: 'fa-user-shield' },
    { value: 'permissions', label: 'Permisos', icon: 'fa-key' },
    { value: 'parking', label: 'Estacionamiento', icon: 'fa-parking' },
    { value: 'rates', label: 'Tarifas', icon: 'fa-money-bill-wave' },
    { value: 'reports', label: 'Reportes', icon: 'fa-chart-bar' },
    { value: 'sensors', label: 'Sensores', icon: 'fa-sensor' },
    { value: 'cameras', label: 'Cámaras', icon: 'fa-camera' },
    { value: 'barriers', label: 'Barreras', icon: 'fa-door-closed' },
    { value: 'dashboard', label: 'Dashboard', icon: 'fa-tachometer-alt' }
  ];
  
  constructor(
    private fb: FormBuilder,
    private permissionService: PermissionService,
    private authContext: AuthContextService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.initForm();
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

  private initForm(): void {
    this.permissionForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(100)]],
      description: ['', [Validators.maxLength(255)]],
      module: [''],
      status: [true]
    });
  }

  private loadPermission(): void {
    this.isLoadingPermission = true;
    
    this.permissionService.getPermissionById(this.permissionId).subscribe({
      next: (permission) => {
        this.permission = permission;
        this.populateForm(permission);
        this.isLoadingPermission = false;
      },
      error: (error) => {
        this.isLoadingPermission = false;
        this.errorMessage = 'Error al cargar el permiso';
        console.error('Error loading permission:', error);
      }
    });
  }

  private populateForm(permission: Permission): void {
    this.permissionForm.patchValue({
      name: permission.name,
      description: permission.description || '',
      module: permission.module || '',
      status: permission.status
    });
  }

  /**
   * Permite edición manual del nombre del permiso
   */
  onNameManualChange(event: any): void {
    const value = event.target.value;
    this.permissionForm.get('name')?.setValue(value, { emitEvent: false });
  }

  // ========== FORMULARIO ==========

  onSubmit(): void {
    // Validar formulario
    if (this.permissionForm.invalid) {
      this.markFormGroupTouched(this.permissionForm);
      return;
    }

    this.isLoading = true;
    this.errorMessage = null;

    const request: UpdatePermissionRequest = {
      name: this.permissionForm.value.name.trim(),
      description: this.permissionForm.value.description?.trim() || '',
      module: this.permissionForm.value.module || undefined,
      status: this.permissionForm.value.status
    };

    this.permissionService.updatePermission(this.permissionId, request).subscribe({
      next: (permission) => {
        this.isLoading = false;
        
        // Navegar a la lista con mensaje de éxito
        this.router.navigate(['/permissions'], {
          state: {
            successMessage: `Permiso "${permission.name}" actualizado exitosamente`
          }
        });
      },
      error: (error) => {
        this.isLoading = false;
        this.errorMessage = error.error?.message || 'Error al actualizar el permiso. Verifica que el nombre no esté duplicado.';
        console.error('Error updating permission:', error);
      }
    });
  }

  cancel(): void {
    this.router.navigate(['/permissions']);
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
    const control = this.permissionForm.get(field);
    return !!(control?.hasError(error) && control?.touched);
  }

  getErrorMessage(field: string): string {
    const control = this.permissionForm.get(field);

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

  /**
   * Obtiene el nombre del módulo para mostrar
   */
  getModuleLabel(): string {
    if (!this.permission?.module) return 'Sin módulo';
    
    const moduleObj = this.availableModules.find(m => m.value === this.permission?.module);
    return moduleObj?.label || this.permission.module;
  }
}