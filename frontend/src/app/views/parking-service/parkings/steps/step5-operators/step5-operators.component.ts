// ============================================================
// steps/step5-operators/step5-operators.component.ts
// ============================================================
import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import {
  CardModule,
  ButtonModule,
  FormModule,
  GridModule,
  AlertModule,
  BadgeModule,
  TableModule,
  SpinnerModule,
} from '@coreui/angular';
import { IconModule } from '@coreui/icons-angular';
import { Router } from '@angular/router';

// SERVICIOS CORRECTOS
import { UserService } from '../../../../../core/services/user.service';
import { ShiftService } from '../../../../../core/services/parking/shift.service';
import { ZoneService } from '../../../../../core/services/parking/zone.service';
import { OperatorAssignmentService } from '../../../../../core/services/parking/operator-assignment.service';

// MODELOS CORRECTOS
import { Operator } from '../../../../../core/models/parking/operator.model';
import { Shift } from '../../../../../core/models/parking/shift.model';
import { Zone } from '../../../../../core/models/parking/zone.model';
import { AssignOperatorRequest } from '../../../../../core/models/parking/operator-assignment.model';

import { forkJoin } from 'rxjs';

/**
 * Interfaz para las filas de asignación en la tabla temporal
 */
interface AssignmentRow {
  operatorId: number;
  operatorName: string;
  operatorEmail: string;
  zoneId: number;
  zoneName: string;
  shiftId: number;
  shiftName: string;
  startDate: string; // ISO format: "2025-02-28"
  endDate?: string;  // ISO format: "2025-12-31"
}

@Component({
  selector: 'app-step5-operators',
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
    TableModule,
    SpinnerModule,
    IconModule,
  ],
  templateUrl: './step5-operators.component.html',
  styleUrl: './step5-operators.component.css',
})
export class Step5OperatorsComponent implements OnInit {
  // ========== INPUTS/OUTPUTS ==========

  @Input() parkingId!: number;  // ID del parking creado
  @Input() createdZones: Zone[] = []; // Zonas creadas en Step 2
  @Output() completed = new EventEmitter<void>();

  // ========== CATÁLOGOS ==========

  operators: Operator[] = [];
  shifts: Shift[] = [];
  zones: Zone[] = []; // Copia de createdZones para usar en el formulario

  // ========== ASIGNACIONES TEMPORALES ==========

  assignments: AssignmentRow[] = [];

  // ========== FORMULARIO ==========

  form!: FormGroup;

  // ========== ESTADO ==========

  loadingCatalogs = true;
  loadingSave = false;
  errorMessage = '';
  catalogError = '';
  successMessage = '';

  // ========== FECHAS MÍNIMAS/MÁXIMAS ==========

  today = new Date().toISOString().split('T')[0]; // "2025-02-28"
  maxDate = new Date(new Date().setFullYear(new Date().getFullYear() + 1))
    .toISOString()
    .split('T')[0]; // 1 año adelante

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private userService: UserService,
    private shiftService: ShiftService,
    private zoneService: ZoneService,
    private operatorAssignmentService: OperatorAssignmentService
  ) {}

  ngOnInit(): void {
    this.zones = [...this.createdZones]; // Copiar zonas recibidas
    this.initForm();
    this.loadCatalogs();
  }

  // ========== INICIALIZAR FORMULARIO ==========

  initForm(): void {
    this.form = this.fb.group({
      operatorId: [null, Validators.required],
      zoneId: [null, Validators.required],
      shiftId: [null, Validators.required],
      startDate: [this.today, Validators.required],
      endDate: [null], // Opcional
    });
  }

  // ========== CARGAR CATÁLOGOS ==========

  loadCatalogs(): void {
    this.loadingCatalogs = true;
    this.catalogError = '';

    forkJoin({
      operators: this.userService.getOperators(), // Método correcto
      shifts: this.shiftService.getActive(),
    }).subscribe({
      next: ({ operators, shifts }) => {
        this.operators = operators;
        this.shifts = shifts;
        this.loadingCatalogs = false;
      },
      error: (error) => {
        console.error('Error cargando catálogos:', error);
        this.loadingCatalogs = false;
        this.catalogError = 'Error al cargar operadores y turnos. Intente recargar.';
      },
    });
  }

  // ========== AGREGAR ASIGNACIÓN TEMPORAL ==========

  addAssignment(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.errorMessage = 'Complete todos los campos obligatorios.';
      return;
    }

    const { operatorId, zoneId, shiftId, startDate, endDate } = this.form.value;

    // Validar fechas
    if (endDate && new Date(endDate) < new Date(startDate)) {
      this.errorMessage = 'La fecha de fin debe ser posterior a la fecha de inicio.';
      return;
    }

    // Verificar duplicado (mismo operador, zona y turno)
    const exists = this.assignments.some(
      (a) =>
        a.operatorId === +operatorId &&
        a.zoneId === +zoneId &&
        a.shiftId === +shiftId
    );

    if (exists) {
      this.errorMessage = 'Esta combinación operador/zona/turno ya fue agregada.';
      return;
    }

    // Obtener nombres para mostrar en la tabla
    const operator = this.operators.find((o) => o.id === +operatorId);
    const zone = this.zones.find((z) => z.id === +zoneId);
    const shift = this.shifts.find((s) => s.id === +shiftId);

    // Agregar a la lista temporal
    this.assignments.push({
      operatorId: +operatorId,
      operatorName: operator ? `${operator.firstName} ${operator.lastName}` : `Operador ${operatorId}`,
      operatorEmail: operator?.email ?? '',
      zoneId: +zoneId,
      zoneName: zone ? `${zone.code} - ${zone.name}` : `Zona ${zoneId}`,
      shiftId: +shiftId,
      shiftName: shift?.name ?? `Turno ${shiftId}`,
      startDate,
      endDate: endDate || undefined,
    });

    // Resetear formulario
    this.form.reset({ startDate: this.today });
    this.errorMessage = '';
    this.successMessage = 'Asignación agregada correctamente.';
    setTimeout(() => (this.successMessage = ''), 3000);
  }

  // ========== REMOVER ASIGNACIÓN TEMPORAL ==========

  removeAssignment(index: number): void {
    this.assignments.splice(index, 1);
  }

  // ========== FINALIZAR Y GUARDAR EN BACKEND ==========

  onFinish(): void {
    // Si no hay asignaciones, finalizar directamente
    if (this.assignments.length === 0) {
      this.navigateToList();
      return;
    }

    // Confirmar antes de guardar
    if (!confirm(`¿Desea guardar ${this.assignments.length} asignación(es)?`)) {
      return;
    }

    this.loadingSave = true;
    this.errorMessage = '';

    // Crear observables para todas las asignaciones
    const requests = this.assignments.map((assignment) => {
      const request: AssignOperatorRequest = {
        userId: assignment.operatorId,
        shiftId: assignment.shiftId,
        startDate: assignment.startDate,
        endDate: assignment.endDate,
      };

      return this.operatorAssignmentService.assignOperator(
        assignment.zoneId,
        request
      );
    });

    // Ejecutar todas las asignaciones en paralelo
    forkJoin(requests).subscribe({
      next: (responses) => {
        console.log('Asignaciones guardadas exitosamente:', responses);
        this.loadingSave = false;
        this.successMessage = `${responses.length} asignación(es) guardada(s) exitosamente.`;
        setTimeout(() => this.navigateToList(), 1500);
      },
      error: (error) => {
        console.error('Error guardando asignaciones:', error);
        this.loadingSave = false;
        this.errorMessage =
          error.error?.message ||
          'Error al guardar las asignaciones. Verifique los datos e intente nuevamente.';
      },
    });
  }

  // ========== NAVEGACIÓN ==========

  navigateToList(): void {
    this.router.navigate(['/parking-service/parkings']);
  }

  // ========== OMITIR ASIGNACIONES ==========

  skipAssignments(): void {
    if (
      this.assignments.length > 0 &&
      !confirm('Tiene asignaciones sin guardar. ¿Desea continuar sin guardarlas?')
    ) {
      return;
    }
    this.navigateToList();
  }

  // ========== HELPERS PARA VISTA ==========

  getOperatorName(id: number): string {
    const op = this.operators.find((o) => o.id === id);
    return op ? `${op.firstName} ${op.lastName}` : `Operador ${id}`;
  }

  getZoneName(id: number): string {
    const zone = this.zones.find((z) => z.id === id);
    return zone ? `${zone.code} - ${zone.name}` : `Zona ${id}`;
  }

  getShiftName(id: number): string {
    return this.shifts.find((s) => s.id === id)?.name ?? `Turno ${id}`;
  }

  formatDate(dateStr?: string): string {
    if (!dateStr) return 'Sin límite';
    return new Date(dateStr).toLocaleDateString('es-PE');
  }
}