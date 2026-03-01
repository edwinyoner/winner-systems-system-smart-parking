import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';

// Services
import { RoleService } from '../../../core/services/role.service';
import { AuthContextService } from '../../../core/services/auth-context.service';

// Models
import { Role, RoleFilters } from '../../../core/models/role.model';
import { PaginatedResponse } from '../../../core/models/pagination.model';

// Components
import { AlertMessageComponent } from '../../../shared/components/alert-message/alert-message.component';

@Component({
  selector: 'app-role-list',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    AlertMessageComponent
  ],
  templateUrl: './role-list.component.html',
  styleUrls: ['./role-list.component.scss']
})
export class RoleListComponent implements OnInit {
  
  filterForm!: FormGroup;
  roles: Role[] = [];
  isLoading = false;
  successMessage: string | null = null;
  errorMessage: string | null = null;

  // Paginación
  currentPage = 1;
  pageSize = 10;
  totalItems = 0;
  totalPages = 0;
  pageSizeOptions = [5, 10, 15, 20, 25, 50];

  // Confirmación de eliminación
  showDeleteConfirm = false;
  roleToDelete: Role | null = null;

  // Math para el template
  Math = Math;

  constructor(
    private fb: FormBuilder,
    private roleService: RoleService,
    private authContext: AuthContextService,
    private router: Router
  ) {
    // Capturar mensajes de éxito desde la navegación
    const state = window.history.state;
    if (state?.['successMessage']) {
      this.successMessage = state['successMessage'];
      setTimeout(() => (this.successMessage = null), 5000);
    }
  }

  ngOnInit(): void {
    this.initFilterForm();
    this.loadRoles();
    this.setupFilterListeners();
  }

  /**
   * Control de permisos (patrón aprendido)
   */
  hasPermission(permission: string): boolean {
    return this.authContext.hasPermission(permission);
  }

  private initFilterForm(): void {
    this.filterForm = this.fb.group({
      search: [''],
      status: ['']
    });
  }

  private setupFilterListeners(): void {
    // Búsqueda con debounce
    this.filterForm.get('search')?.valueChanges
      .pipe(debounceTime(400), distinctUntilChanged())
      .subscribe(() => {
        this.currentPage = 1;
        this.loadRoles();
      });

    // Estado
    this.filterForm.get('status')?.valueChanges.subscribe(() => {
      this.currentPage = 1;
      this.loadRoles();
    });
  }

  loadRoles(): void {
    this.isLoading = true;
    const val = this.filterForm.value;

    const filters: RoleFilters = {
      search: val.search || undefined,
      status: val.status !== '' ? val.status === 'true' : undefined,
      page: this.currentPage - 1,
      size: this.pageSize,
      sortBy: 'id',
      sortDirection: 'desc'
    };

    this.roleService.getRoles(filters).subscribe({
      next: (res: PaginatedResponse<Role>) => {
        this.roles = res.content;
        this.totalItems = res.totalElements;
        this.totalPages = res.totalPages;
        this.currentPage = res.number + 1;
        this.isLoading = false;
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMessage = 'Error al cargar los roles del sistema';
        console.error(err);
      }
    });
  }

  clearFilters(): void {
    this.filterForm.reset({
      search: '',
      status: ''
    }, { emitEvent: false });
    this.currentPage = 1;
    this.loadRoles();
  }

  // ========== NAVEGACIÓN Y EVENTOS ==========

  onPageSizeChange(event: any): void {
    this.pageSize = Number(event.target.value);
    this.currentPage = 1;
    this.loadRoles();
  }

  goToPage(page: number): void {
    if (page >= 1 && page <= this.totalPages) {
      this.currentPage = page;
      this.loadRoles();
    }
  }

  getPages(): number[] {
    const pages = [];
    const maxVisiblePages = 5;
    let startPage = Math.max(1, this.currentPage - Math.floor(maxVisiblePages / 2));
    let endPage = Math.min(this.totalPages, startPage + maxVisiblePages - 1);

    if (endPage - startPage + 1 < maxVisiblePages) {
      startPage = Math.max(1, endPage - maxVisiblePages + 1);
    }

    for (let i = startPage; i <= endPage; i++) {
      pages.push(i);
    }
    return pages;
  }

  createRole(): void {
    this.router.navigate(['/roles/create']);
  }

  viewRole(id: number): void {
    this.router.navigate(['/roles', id]);
  }

  editRole(id: number): void {
    this.router.navigate(['/roles', id, 'edit']);
  }

  confirmDelete(role: Role): void {
    this.roleToDelete = role;
    this.showDeleteConfirm = true;
  }

  cancelDelete(): void {
    this.showDeleteConfirm = false;
    this.roleToDelete = null;
  }

  deleteRole(): void {
    if (!this.roleToDelete) return;
    this.isLoading = true;

    this.roleService.deleteRole(this.roleToDelete.id).subscribe({
      next: () => {
        this.isLoading = false;
        this.successMessage = `Rol "${this.roleToDelete?.name}" eliminado exitosamente`;
        this.showDeleteConfirm = false;
        this.roleToDelete = null;
        this.loadRoles();
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMessage = err.error?.message || 'No se pudo eliminar el rol. Verifica que no tenga usuarios asignados.';
        this.showDeleteConfirm = false;
      }
    });
  }

  // ========== FORMATEADORES ==========

  getPermissionCount(role: Role): number {
    return role.permissions?.length || 0;
  }

  formatDate(date: string | Date): string {
    if (!date) return 'N/A';
    return new Date(date).toLocaleDateString('es-PE', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit'
    });
  }

  dismissSuccess(): void {
    this.successMessage = null;
  }

  dismissError(): void {
    this.errorMessage = null;
  }
}