import { Component, EventEmitter, Input, OnInit, Output } from "@angular/core";
import { CommonModule } from "@angular/common";
import {
  FormArray,
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from "@angular/forms";
import {
  CardModule,
  ButtonModule,
  FormModule,
  GridModule,
  AlertModule,
  BadgeModule,
  SpinnerModule,
  TooltipModule,
} from "@coreui/angular";
import { IconModule } from "@coreui/icons-angular";
import { forkJoin } from "rxjs";
import { ShiftService } from "../../../../../core/services/parking/shift.service";
import { RateService } from "../../../../../core/services/parking/rate.service";
import { ParkingShiftRateService } from "../../../../../core/services/parking/parking-shift-rate.service";
import { Shift } from "../../../../../core/models/parking/shift.model";
import { Rate } from "../../../../../core/models/parking/rate.model";

@Component({
  selector: "app-step4-config",
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    CardModule,
    ButtonModule,
    FormModule,
    GridModule,
    AlertModule,
    BadgeModule,
    SpinnerModule,
    TooltipModule,
    IconModule,
  ],
  templateUrl: "./step4-config.component.html",
  styleUrl: "./step4-config.component.css",
})
export class Step4ConfigComponent implements OnInit {
  @Input() parkingId!: number;
  @Output() configSaved = new EventEmitter<void>();

  // ❌ ELIMINADO: @Input() zoneIds: number[] = [];

  // Catálogos
  shifts: Shift[] = [];
  rates: Rate[] = [];

  // FormArray para las filas de configuración
  form: FormGroup;

  // Estado
  loadingCatalogs = true;
  loadingSave = false;
  errorMessage = "";
  catalogError = "";

  // Límite de configuraciones (máximo 3 turnos)
  readonly MAX_CONFIGS = 3;

  constructor(
    private fb: FormBuilder,
    private shiftService: ShiftService,
    private rateService: RateService,
    private parkingShiftRateService: ParkingShiftRateService,
  ) {
    this.form = this.fb.group({
      configurations: this.fb.array([]),
    });
  }

  ngOnInit(): void {
    this.loadCatalogs();
  }

  // ========================= FORM ARRAY GETTERS =========================

  get configurations(): FormArray {
    return this.form.get("configurations") as FormArray;
  }

  // ========================= CARGA DE CATÁLOGOS =========================

  loadCatalogs(): void {
    this.loadingCatalogs = true;
    this.catalogError = "";

    forkJoin({
      shifts: this.shiftService.getActive(),
      rates: this.rateService.getActive(),
    }).subscribe({
      next: ({ shifts, rates }) => {
        this.shifts = shifts;
        this.rates = rates;
        this.loadingCatalogs = false;

        // Agregar la primera fila por defecto
        if (this.configurations.length === 0) {
          this.addConfiguration();
        }
      },
      error: () => {
        this.loadingCatalogs = false;
        this.catalogError =
          "Error al cargar turnos y tarifas. Intente recargar.";
      },
    });
  }

  // ========================= GESTIÓN DE FILAS =========================

  addConfiguration(): void {
    if (this.configurations.length >= this.MAX_CONFIGS) {
      return;
    }

    const configGroup = this.fb.group({
      shiftId: [null, Validators.required],
      rateId: [null, Validators.required],
      status: [true], // Activo por defecto
    });

    this.configurations.push(configGroup);
  }

  removeConfiguration(index: number): void {
    this.configurations.removeAt(index);
  }

  get canAddMore(): boolean {
    return this.configurations.length < this.MAX_CONFIGS;
  }

  // ========================= VALIDACIONES =========================

  /**
   * Verifica si un turno ya está siendo usado en otra fila.
   * Previene duplicación de turnos.
   */
  isShiftUsed(shiftId: number, currentIndex: number): boolean {
    return this.configurations.controls.some((control, index) => {
      if (index === currentIndex) return false; // No comparar consigo mismo
      return control.get("shiftId")?.value === shiftId;
    });
  }

  /**
   * Obtiene los shifts disponibles para una fila específica.
   * Excluye shifts ya usados en otras filas.
   */
  getAvailableShifts(currentIndex: number): Shift[] {
    return this.shifts.filter((shift) => {
      const isUsed = this.isShiftUsed(shift.id!, currentIndex);
      const isCurrent =
        this.configurations.at(currentIndex).get("shiftId")?.value === shift.id;
      return !isUsed || isCurrent;
    });
  }

  // ========================= HELPERS =========================

  getShiftName(id: number): string {
    return this.shifts.find((s) => s.id === id)?.name ?? "Turno";
  }

  getRateName(id: number): string {
    return this.rates.find((r) => r.id === id)?.name ?? "Tarifa";
  }

  getRateAmount(id: number): string {
    const rate = this.rates.find((r) => r.id === id);
    if (!rate) return "";
    return `${rate.currency ?? "PEN"} ${rate.amount?.toFixed(2)}`;
  }

  get canSubmit(): boolean {
    return this.form.valid && this.configurations.length > 0;
  }

  // ========================= SUBMIT =========================

  onSubmit(): void {
    if (!this.canSubmit) {
      this.form.markAllAsTouched();
      return;
    }

    this.loadingSave = true;
    this.errorMessage = "";

    const request = {
      configurations: this.configurations.value,
    };

    this.parkingShiftRateService.configure(this.parkingId, request).subscribe({
      next: () => {
        this.loadingSave = false;
        this.configSaved.emit();
      },
      error: (err) => {
        this.loadingSave = false;
        this.errorMessage =
          err?.error?.message ||
          "Error al guardar la configuración. Intente nuevamente.";
      },
    });
  }
}
