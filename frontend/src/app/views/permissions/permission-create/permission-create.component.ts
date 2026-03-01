import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';

// Services
import { PermissionService } from '../../../core/services/permission.service';
import { AuthContextService } from '../../../core/services/auth-context.service';

// Models
import { CreatePermissionRequest } from '../../../core/models/permission.model';

// Components
import { AlertMessageComponent } from '../../../shared/components/alert-message/alert-message.component';

@Component({
  selector: 'app-permission-create',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    AlertMessageComponent
  ],
  templateUrl: './permission-create.component.html',
  styleUrls: ['./permission-create.component.scss']
})
export class PermissionCreateComponent implements OnInit {
  
  permissionForm!: FormGroup;
  
  isLoading = false;
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

  // Acciones comunes CRUD
  commonActions = [
    { value: 'create', label: 'Crear', icon: 'fa-plus' },
    { value: 'read', label: 'Leer', icon: 'fa-eye' },
    { value: 'update', label: 'Actualizar', icon: 'fa-edit' },
    { value: 'delete', label: 'Eliminar', icon: 'fa-trash' }
  ];
  
  constructor(
    private fb: FormBuilder,
    private permissionService: PermissionService,
    private authContext: AuthContextService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.initForm();
    this.setupFormListeners();
  }

  /**
   * Control de permisos (patrón aprendido)
   */
  hasPermission(permission: string): boolean {
    return this.authContext.hasPermission(permission);
  }

  private initForm(): void {
    this.permissionForm = this.fb.group({
      module: ['', [Validators.required]],
      action: ['', [Validators.required]],
      name: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(100)]],
      description: ['', [Validators.maxLength(255)]],
      status: [true]
    });
  }

  private setupFormListeners(): void {
    // Auto-generar nombre del permiso cuando cambian módulo o acción
    this.permissionForm.get('module')?.valueChanges.subscribe(() => {
      this.generatePermissionName();
    });

    this.permissionForm.get('action')?.valueChanges.subscribe(() => {
      this.generatePermissionName();
    });
  }

  /**
   * Genera automáticamente el nombre del permiso
   * Formato: module.action (ej: users.create)
   */
  private generatePermissionName(): void {
    const module = this.permissionForm.get('module')?.value;
    const action = this.permissionForm.get('action')?.value;

    if (module && action) {
      const permissionName = `${module}.${action}`;
      this.permissionForm.get('name')?.setValue(permissionName, { emitEvent: false });
    }
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

    const request: CreatePermissionRequest = {
      name: this.permissionForm.value.name.trim(),
      description: this.permissionForm.value.description?.trim() || '',
      module: this.permissionForm.value.module,
      status: this.permissionForm.value.status
    };

    this.permissionService.createPermission(request).subscribe({
      next: (permission) => {
        this.isLoading = false;
        
        // Navegar a la lista con mensaje de éxito
        this.router.navigate(['/permissions'], {
          state: {
            successMessage: `Permiso "${permission.name}" creado exitosamente`
          }
        });
      },
      error: (error) => {
        this.isLoading = false;
        this.errorMessage = error.error?.message || 'Error al crear el permiso. Verifica que el nombre no esté duplicado.';
        console.error('Error creating permission:', error);
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
}