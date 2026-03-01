import { Component, OnInit } from "@angular/core";
import { CommonModule } from "@angular/common";
import { FormBuilder, FormGroup, ReactiveFormsModule } from "@angular/forms";
import { Router } from "@angular/router";
import { debounceTime, distinctUntilChanged } from "rxjs/operators";

import { RateService } from "../../../../core/services/parking/rate.service";
import { Rate } from "../../../../core/models/parking/rate.model";
// ParkingPagedResponse para parking-service
import { ParkingPagedResponse } from "../../../../core/models/pagination.model";
import { AlertMessageComponent } from "../../../../shared/components/alert-message/alert-message.component";

@Component({
  selector: "app-rate-list",
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, AlertMessageComponent],
  templateUrl: "./rate-list.component.html",
  styleUrls: ["./rate-list.component.css"],
})
export class RateListComponent implements OnInit {
  filterForm!: FormGroup;
  rates: Rate[] = [];
  isLoading = false;
  successMessage: string | null = null;
  errorMessage: string | null = null;

  currentPage = 1;
  pageSize = 10;
  totalItems = 0;
  totalPages = 0;
  pageSizeOptions = [5, 10, 15, 20, 25, 50];

  showDeleteConfirm = false;
  rateToDelete: Rate | null = null;

  Math = Math;

  constructor(
    private fb: FormBuilder,
    private rateService: RateService,
    private router: Router,
  ) {
    const state = window.history.state;
    if (state?.["successMessage"]) {
      this.successMessage = state["successMessage"];
      setTimeout(() => (this.successMessage = null), 5000);
    }
  }

  ngOnInit(): void {
    this.initFilterForm();
    this.loadRates();
    this.setupFilterListeners();
  }

  private initFilterForm(): void {
    this.filterForm = this.fb.group({
      search: [""],
      status: [""],
    });
  }

  private setupFilterListeners(): void {
    this.filterForm
      .get("search")
      ?.valueChanges.pipe(debounceTime(400), distinctUntilChanged())
      .subscribe(() => {
        this.currentPage = 1;
        this.loadRates();
      });

    this.filterForm.get("status")?.valueChanges.subscribe(() => {
      this.currentPage = 1;
      this.loadRates();
    });
  }

  loadRates(): void {
    this.isLoading = true;

    const search = this.filterForm.get("search")?.value ?? "";
    const status = this.filterForm.get("status")?.value ?? "";

    this.rateService
      .getAll(this.currentPage - 1, this.pageSize, search, status)
      .subscribe({
        next: (response: ParkingPagedResponse<Rate>) => {
          this.rates = response.content;
          this.totalItems = response.totalElements;
          this.totalPages = response.totalPages;
          this.currentPage = response.pageNumber + 1;
          this.pageSize = response.pageSize;
          this.isLoading = false;
        },
        error: (error) => {
          this.isLoading = false;
          this.errorMessage = "Error al cargar las tarifas del sistema";
          console.error(error);
        },
      });
  }

  clearFilters(): void {
    this.filterForm.reset({ search: "", status: "" }, { emitEvent: false });
    this.currentPage = 1;
    this.loadRates();
  }

  onPageSizeChange(event: any): void {
    this.pageSize = Number(event.target.value);
    this.currentPage = 1;
    this.loadRates();
  }

  goToPage(page: number): void {
    if (page >= 1 && page <= this.totalPages) {
      this.currentPage = page;
      this.loadRates();
    }
  }

  getPages(): number[] {
    const pages = [];
    const max = 5;
    let start = Math.max(1, this.currentPage - Math.floor(max / 2));
    let end = Math.min(this.totalPages, start + max - 1);
    if (end - start + 1 < max) start = Math.max(1, end - max + 1);
    for (let i = start; i <= end; i++) pages.push(i);
    return pages;
  }

  createRate(): void {
    this.router.navigate(["/rates/create"]);
  }
  viewRate(id: number): void {
    this.router.navigate(["/rates", id]);
  }
  editRate(id: number): void {
    this.router.navigate(["/rates", id, "edit"]);
  }

  confirmDelete(rate: Rate): void {
    this.rateToDelete = rate;
    this.showDeleteConfirm = true;
  }

  cancelDelete(): void {
    this.showDeleteConfirm = false;
    this.rateToDelete = null;
  }

  deleteRate(): void {
    if (!this.rateToDelete?.id) return;
    this.isLoading = true;

    this.rateService.delete(this.rateToDelete.id).subscribe({
      next: () => {
        this.isLoading = false;
        this.successMessage = `Tarifa "${this.rateToDelete?.name}" eliminada exitosamente`;
        this.showDeleteConfirm = false;
        this.rateToDelete = null;
        this.loadRates();
      },
      error: (error) => {
        this.isLoading = false;
        this.errorMessage =
          error.error?.message || "No se pudo eliminar la tarifa";
        this.showDeleteConfirm = false;
      },
    });
  }

  formatCurrency(amount: number, currency: string): string {
    const symbol =
      currency === "PEN" ? "S/." : currency === "USD" ? "$" : currency;
    return `${symbol} ${amount.toFixed(2)}`;
  }

  dismissSuccess(): void {
    this.successMessage = null;
  }
  dismissError(): void {
    this.errorMessage = null;
  }
}
