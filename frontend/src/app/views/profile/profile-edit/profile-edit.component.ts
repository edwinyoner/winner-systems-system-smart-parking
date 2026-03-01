import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { CurrentUserService } from '../../../core/services/current-user.service';
import { AuthService } from '../../../core/services/auth.service';
import { AlertMessageComponent } from '../../../shared/components/alert-message/alert-message.component';

@Component({
  selector: 'app-profile-edit',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    AlertMessageComponent
  ],
  templateUrl: './profile-edit.component.html',
  styleUrls: ['./profile-edit.component.scss']
})
export class ProfileEditComponent implements OnInit {

  profileForm!: FormGroup;
  isLoading = false;
  successMessage: string | null = null;
  errorMessage: string | null = null;
  profilePicturePreview: string | null = null;

  constructor(
    private fb: FormBuilder,
    private currentUserService: CurrentUserService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.initForm();
    this.loadUserData();
  }

  private initForm(): void {
    this.profileForm = this.fb.group({
      firstName: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(100)]],
      lastName: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(100)]],
      phoneNumber: ['', [Validators.pattern(/^\+?[1-9]\d{1,14}$/)]],
      profilePicture: ['', [Validators.maxLength(500)]],
      email: [{ value: '', disabled: true }]
    });
  }

  private loadUserData(): void {
    const currentUser = this.currentUserService.getCurrentUser();
    
    if (currentUser) {
      this.profileForm.patchValue({
        firstName: currentUser.firstName,
        lastName: currentUser.lastName,
        phoneNumber: currentUser.phoneNumber || '',
        profilePicture: currentUser.profilePicture || '',
        email: currentUser.email
      });
      
      this.profilePicturePreview = currentUser.profilePicture || null;
    }
  }

  onProfilePictureChange(): void {
    const url = this.profileForm.get('profilePicture')?.value;
    this.profilePicturePreview = url || null;
  }

  onSubmit(): void {
    if (this.profileForm.invalid) {
      this.profileForm.markAllAsTouched();
      return;
    }

    this.isLoading = true;
    this.successMessage = null;
    this.errorMessage = null;

    const updateData = {
      firstName: this.profileForm.get('firstName')?.value,
      lastName: this.profileForm.get('lastName')?.value,
      phoneNumber: this.profileForm.get('phoneNumber')?.value || null,
      profilePicture: this.profileForm.get('profilePicture')?.value || null
    };

    this.authService.updateProfile(updateData).subscribe({
      next: (response) => {
        this.isLoading = false;
        
        // ACTUALIZAR MANUALMENTE EL CurrentUserService
        const currentUser = this.currentUserService.getCurrentUser();
        if (currentUser) {
          const updatedUser = {
            ...currentUser,
            firstName: updateData.firstName,
            lastName: updateData.lastName,
            phoneNumber: updateData.phoneNumber || currentUser.phoneNumber,
            profilePicture: updateData.profilePicture || currentUser.profilePicture
          };
          this.currentUserService.setCurrentUser(updatedUser);
        }

        this.successMessage = 'Perfil actualizado exitosamente';
        
        // REDIRIGIR A PROFILE (SIN CERRAR SESIÓN)
        setTimeout(() => {
          this.router.navigate(['/profile']);
        }, 1500);
      },
      error: (error) => {
        this.isLoading = false;
        this.errorMessage = error.error?.message || 'Error al actualizar el perfil';
      }
    });
  }

  cancel(): void {
    this.router.navigate(['/profile']);
  }

  hasError(fieldName: string, errorType: string): boolean {
    const field = this.profileForm.get(fieldName);
    return !!(field?.hasError(errorType) && (field?.dirty || field?.touched));
  }

  getErrorMessage(fieldName: string): string {
    const field = this.profileForm.get(fieldName);
    
    if (field?.hasError('required')) return 'Este campo es requerido';
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