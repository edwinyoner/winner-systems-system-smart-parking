import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';

import { ShiftService } from '../../../../core/services/parking/shift.service';
import { Shift } from '../../../../core/models/parking/shift.model';
// Importar ParkingPagedResponse (no PaginatedResponse)
import { ParkingPagedResponse } from '../../../../core/models/pagination.model';
import { AlertMessageComponent } from '../../../../shared/components/alert-message/alert-message.component';

@Component({
  selector: 'app-shift-list',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, AlertMessageComponent],
  templateUrl: './shift-list.component.html',
  styleUrls: ['./shift-list.component.css']
})
export class ShiftListComponent implements OnInit {

  filterForm!: FormGroup;
  shifts: Shift[] = [];
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
  shiftToDelete: Shift | null = null;

  Math = Math;

  constructor(
    private fb: FormBuilder,
    private shiftService: ShiftService,
    private router: Router
  ) {
    const state = window.history.state;
    if (state?.['successMessage']) {
      this.successMessage = state['successMessage'];
      setTimeout(() => (this.successMessage = null), 5000);
    }
  }

  ngOnInit(): void {
    this.initFilterForm();
    this.loadShifts();
    this.setupFilterListeners();
  }

  private initFilterForm(): void {
    this.filterForm = this.fb.group({
      search: [''],
      status: ['']
    });
  }

  private setupFilterListeners(): void {
    this.filterForm.get('search')?.valueChanges
      .pipe(debounceTime(400), distinctUntilChanged())
      .subscribe(() => {
        this.currentPage = 1;
        this.loadShifts();
      });

    this.filterForm.get('status')?.valueChanges.subscribe(() => {
      this.currentPage = 1;
      this.loadShifts();
    });
  }

  loadShifts(): void {
    this.isLoading = true;

    const search = this.filterForm.get("search")?.value ?? "";
    const status = this.filterForm.get("status")?.value ?? "";

    this.shiftService.getAll(this.currentPage - 1, this.pageSize, search, status).subscribe({
      next: (response: ParkingPagedResponse<Shift>) => {
        this.shifts      = response.content;
        this.totalItems  = response.totalElements;
        this.totalPages  = response.totalPages;
        this.currentPage = response.pageNumber + 1;  // base 0 → base 1
        this.pageSize    = response.pageSize;
        this.isLoading   = false;
      },
      error: (error) => {
        this.isLoading    = false;
        this.errorMessage = 'Error al cargar los turnos del sistema';
        console.error(error);
      }
    });
  }

  clearFilters(): void {
    this.filterForm.reset({ search: '', status: '' }, { emitEvent: false });
    this.currentPage = 1;
    this.loadShifts();
  }

  onPageSizeChange(event: any): void {
    this.pageSize = Number(event.target.value);
    this.currentPage = 1;
    this.loadShifts();
  }

  goToPage(page: number): void {
    if (page >= 1 && page <= this.totalPages) {
      this.currentPage = page;
      this.loadShifts();
    }
  }

  getPages(): number[] {
    const pages = [];
    const max = 5;
    let start = Math.max(1, this.currentPage - Math.floor(max / 2));
    let end   = Math.min(this.totalPages, start + max - 1);
    if (end - start + 1 < max) start = Math.max(1, end - max + 1);
    for (let i = start; i <= end; i++) pages.push(i);
    return pages;
  }

  createShift(): void { this.router.navigate(['/shifts/create']); }
  viewShift(id: number): void { this.router.navigate(['/shifts', id]); }
  editShift(id: number): void { this.router.navigate(['/shifts', id, 'edit']); }

  confirmDelete(shift: Shift): void {
    this.shiftToDelete = shift;
    this.showDeleteConfirm = true;
  }

  cancelDelete(): void {
    this.showDeleteConfirm = false;
    this.shiftToDelete = null;
  }

  deleteShift(): void {
    if (!this.shiftToDelete?.id) return;
    this.isLoading = true;

    this.shiftService.delete(this.shiftToDelete.id).subscribe({
      next: () => {
        this.isLoading     = false;
        this.successMessage = `Turno "${this.shiftToDelete?.name}" eliminado exitosamente`;
        this.showDeleteConfirm = false;
        this.shiftToDelete = null;
        this.loadShifts();
      },
      error: (error) => {
        this.isLoading     = false;
        this.errorMessage  = error.error?.message || 'No se pudo eliminar el turno';
        this.showDeleteConfirm = false;
      }
    });
  }

  formatTime(time: string): string {
    if (!time) return 'N/A';
    return time.substring(0, 5);
  }

  dismissSuccess(): void { this.successMessage = null; }
  dismissError(): void   { this.errorMessage = null; }
}