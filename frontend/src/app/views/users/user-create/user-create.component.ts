import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { UserService } from '../../../core/services/user.service';
import { RoleService } from '../../../core/services/role.service';
import { AlertMessageComponent } from '../../../shared/components/alert-message/alert-message.component';
import { Role } from '../../../core/models/role.model';
import { CreateUserRequest } from '../../../core/models/user.model';

@Component({
  selector: 'app-user-create',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    AlertMessageComponent
  ],
  templateUrl: './user-create.component.html',
  styleUrls: ['./user-create.component.scss']
})
export class UserCreateComponent implements OnInit {

  userForm!: FormGroup;
  roles: Role[] = [];
  isLoading = false;
  isSendingEmail = false;  
  successMessage: string | null = null;
  errorMessage: string | null = null;

  // Credenciales generadas
  generatedCredentials: {
    userId: number;        
    firstName: string;
    email: string;
    password: string;
    loginUrl: string;
  } | null = null;

  constructor(
    private fb: FormBuilder,
    private userService: UserService,
    private roleService: RoleService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.initForm();
    this.loadRoles();
  }

  private initForm(): void {
    this.userForm = this.fb.group({
      firstName: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(100)]],
      lastName: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(100)]],
      email: ['', [Validators.required, Validators.email]],
      phoneNumber: ['', [Validators.pattern(/^\+?[1-9]\d{1,14}$/)]],
      roleIds: [[], [Validators.required]]
    });
  }

  private loadRoles(): void {
    this.roleService.getAllRoles().subscribe({
      next: (roles) => {
        this.roles = roles;
      },
      error: (error) => {
        console.error('Error al cargar roles:', error);
        this.errorMessage = 'Error al cargar roles';
      }
    });
  }

  generatePassword(): string {
    const length = 12;
    const charset = 'abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789@$!%*?&';
    let password = '';
    
    password += 'ABCDEFGHIJKLMNOPQRSTUVWXYZ'[Math.floor(Math.random() * 26)];
    password += 'abcdefghijklmnopqrstuvwxyz'[Math.floor(Math.random() * 26)];
    password += '0123456789'[Math.floor(Math.random() * 10)];
    password += '@$!%*?&'[Math.floor(Math.random() * 7)];
    
    for (let i = password.length; i < length; i++) {
      password += charset[Math.floor(Math.random() * charset.length)];
    }
    
    return password.split('').sort(() => Math.random() - 0.5).join('');
  }

  onRoleChange(roleId: number, event: any): void {
    const roleIds = this.userForm.get('roleIds')?.value || [];
    
    if (event.target.checked) {
      if (!roleIds.includes(roleId)) {
        roleIds.push(roleId);
      }
    } else {
      const index = roleIds.indexOf(roleId);
      if (index > -1) {
        roleIds.splice(index, 1);
      }
    }
    
    this.userForm.patchValue({ roleIds });
  }

  isRoleSelected(roleId: number): boolean {
    const roleIds = this.userForm.get('roleIds')?.value || [];
    return roleIds.includes(roleId);
  }

  onSubmit(): void {
    if (this.userForm.invalid) {
      this.userForm.markAllAsTouched();
      return;
    }

    this.isLoading = true;
    this.successMessage = null;
    this.errorMessage = null;

    const generatedPassword = this.generatePassword();
    const selectedRoleIds = this.userForm.get('roleIds')?.value || [];
    const roleNames = selectedRoleIds.map((id: number) => {
      const role = this.roles.find(r => r.id === id);
      return role?.name || '';
    }).filter((name: string) => name !== '');

    const userData: CreateUserRequest = {
      firstName: this.userForm.get('firstName')?.value,
      lastName: this.userForm.get('lastName')?.value,
      email: this.userForm.get('email')?.value,
      password: generatedPassword,
      phoneNumber: this.userForm.get('phoneNumber')?.value || undefined,
      roles: roleNames,
      captchaToken: undefined
    };

    this.userService.createUser(userData).subscribe({
      next: (response) => {
        this.isLoading = false;
        this.successMessage = 'Usuario creado exitosamente. Ahora puedes enviar las credenciales por email.';
        
        // Guardar credenciales generadas CON userId
        this.generatedCredentials = {
          userId: response.id,          
          firstName: userData.firstName,
          email: userData.email,
          password: generatedPassword,
          loginUrl: 'http://localhost:4200/login'
        };

        // Reset form
        this.userForm.reset({
          firstName: '',
          lastName: '',
          email: '',
          phoneNumber: '',
          roleIds: []
        });
      },
      error: (error) => {
        this.isLoading = false;
        this.errorMessage = error.error?.message || 'Error al crear el usuario';
        console.error('Error completo:', error);
      }
    });
  }

  /**
   * NUEVO: Envía credenciales por email
   */
  sendCredentialsByEmail(): void {
    if (!this.generatedCredentials) return;

    this.isSendingEmail = true;
    this.successMessage = null;
    this.errorMessage = null;

    this.userService.resendCredentials(
      this.generatedCredentials.userId,
      this.generatedCredentials.password
    ).subscribe({
      next: () => {
        this.isSendingEmail = false;
        this.successMessage = 'Credenciales enviadas exitosamente por email';
        
        // Redirigir a la lista después de 2 segundos
        setTimeout(() => {
          this.router.navigate(['/users'], {
            state: { 
              successMessage: `Credenciales enviadas a ${this.generatedCredentials?.email}` 
            }
          });
        }, 2000);
      },
      error: (error) => {
        this.isSendingEmail = false;
        this.errorMessage = error.error?.message || 'Error al enviar credenciales';
        console.error('Error:', error);
      }
    });
  }

  cancel(): void {
    this.router.navigate(['/users']);
  }

  hasError(fieldName: string, errorType: string): boolean {
    const field = this.userForm.get(fieldName);
    return !!(field?.hasError(errorType) && (field?.dirty || field?.touched));
  }

  getErrorMessage(fieldName: string): string {
    const field = this.userForm.get(fieldName);
    
    if (field?.hasError('required')) return 'Este campo es requerido';
    if (field?.hasError('email')) return 'Email inválido';
    if (field?.hasError('minlength')) {
      const minLength = field.errors?.['minlength'].requiredLength;
      return `Debe tener al menos ${minLength} caracteres`;
    }
    if (field?.hasError('maxlength')) {
      const maxLength = field.errors?.['maxlength'].requiredLength;
      return `No puede exceder ${maxLength} caracteres`;
    }
    if (field?.hasError('pattern')) return 'Formato inválido. Ejemplo: +51987654321';
    
    return '';
  }

  dismissSuccess(): void {
    this.successMessage = null;
  }

  dismissError(): void {
    this.errorMessage = null;
  }
}