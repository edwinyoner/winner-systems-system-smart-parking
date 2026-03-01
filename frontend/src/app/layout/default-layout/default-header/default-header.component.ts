import { NgTemplateOutlet } from '@angular/common';
import { Component, computed, inject, input, OnInit, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';

import {
  AvatarComponent,
  BreadcrumbRouterComponent,
  ColorModeService,
  ContainerComponent,
  DropdownComponent,
  DropdownDividerDirective,
  DropdownItemDirective,
  DropdownMenuDirective,
  DropdownToggleDirective,
  HeaderComponent,
  HeaderNavComponent,
  HeaderTogglerDirective,
  NavLinkDirective,
  SidebarToggleDirective,
  BadgeComponent
} from '@coreui/angular';

import { IconDirective } from '@coreui/icons-angular';
import { AuthService } from '../../../core/services/auth.service';
import { AuthContextService } from '../../../core/services/auth-context.service';

@Component({
  selector: 'app-default-header',
  templateUrl: './default-header.component.html',
  styleUrls: ['./default-header.component.scss'],
  imports: [
    ContainerComponent,
    HeaderTogglerDirective,
    SidebarToggleDirective,
    IconDirective,
    HeaderNavComponent,
    NavLinkDirective,
    NgTemplateOutlet,
    BreadcrumbRouterComponent,
    DropdownComponent,
    DropdownToggleDirective,
    AvatarComponent,
    DropdownMenuDirective,
    DropdownItemDirective,
    DropdownDividerDirective,
    BadgeComponent
  ]
})
export class DefaultHeaderComponent extends HeaderComponent implements OnInit {

  readonly #colorModeService = inject(ColorModeService);
  readonly colorMode = this.#colorModeService.colorMode;
  
  private authService = inject(AuthService);
  private authContext = inject(AuthContextService);
  private router = inject(Router);

  // Signals para la información del usuario
  fullName = signal<string>('Usuario');
  userEmail = signal<string>('');
  userInitials = signal<string>('??');
  profilePicture = signal<string>(''); // Cambiar a string vacío
  activeRole = signal<string>('Sin rol');
  roleBadgeColor = signal<string>('secondary');

  readonly colorModes = [
    { name: 'light', text: 'Light', icon: 'cilSun' },
    { name: 'dark', text: 'Dark', icon: 'cilMoon' },
    { name: 'auto', text: 'Auto', icon: 'cilContrast' }
  ];

  readonly icons = computed(() => {
    const currentMode = this.colorMode();
    return this.colorModes.find(mode => mode.name === currentMode)?.icon ?? 'cilSun';
  });

  constructor() {
    super();
  }

  ngOnInit(): void {
    this.loadUserInfo();
  }

  /**
   * Carga la información del usuario usando AuthContextService
   * Proporciona valores por defecto para manejar nulls
   */
  private loadUserInfo(): void {
    this.fullName.set(this.authContext.getFullName() || 'Usuario');
    this.userEmail.set(this.authContext.getUserEmail() || ''); // Manejar null
    this.userInitials.set(this.authContext.getUserInitials() || '??');
    this.profilePicture.set(this.authContext.getProfilePicture() || ''); // Manejar null
    this.activeRole.set(this.authContext.getActiveRole() || 'Sin rol');
    
    // Obtener color del badge según el rol
    const role = this.activeRole();
    const colorMap: { [key: string]: string } = {
      'ADMIN': 'danger',
      'AUTORIDAD': 'warning',
      'OPERADOR': 'info',
      'SUPERVISOR': 'success'
    };
    this.roleBadgeColor.set(colorMap[role] || 'secondary');
  }

  sidebarId = input('sidebar1');

  /**
   * Cierra sesión del usuario
   */
  logout(): void {
    this.authService.logout();
  }

  /**
   * Navega al perfil del usuario
   */
  goToProfile(): void {
    this.router.navigate(['/profile']);
  }

  /**
   * Navega a editar perfil
   */
  goToEditProfile(): void {
    this.router.navigate(['/profile/edit']);
  }

  /**
   * Navega a cambiar contraseña
   */
  goToChangePassword(): void {
    this.router.navigate(['/profile/change-password']);
  }
}