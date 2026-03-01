import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, catchError, throwError } from 'rxjs';
import { environment } from '../../../../environments/environment';
import {
  OperatorAssignment,
  AssignOperatorRequest,
  AssignOperatorResponse,
} from '../../models/parking/operator-assignment.model';

/**
 * Servicio para gestión de asignación de operadores a zonas
 * Maneja la relación entre operadores, zonas y turnos
 */
@Injectable({
  providedIn: 'root',
})
export class OperatorAssignmentService {
  private apiUrl = `${environment.apiUrl}/parking-service/v1/zones`;

  constructor(private http: HttpClient) {}

  // ========== ASIGNAR OPERADOR A ZONA ==========

  /**
   * Asigna un operador a una zona específica en un turno
   * @param zoneId ID de la zona
   * @param request Datos de la asignación (userId, shiftId, startDate, endDate)
   * @returns Observable con la asignación creada
   */
  assignOperator(
    zoneId: number,
    request: AssignOperatorRequest
  ): Observable<AssignOperatorResponse> {
    return this.http
      .post<AssignOperatorResponse>(
        `${this.apiUrl}/${zoneId}/operators`,
        request
      )
      .pipe(catchError(this.handleError));
  }

  // ========== OBTENER ASIGNACIONES DE UNA ZONA ==========

  /**
   * Obtiene todas las asignaciones activas de una zona
   * @param zoneId ID de la zona
   * @returns Observable con lista de asignaciones
   */
  getAssignmentsByZone(zoneId: number): Observable<OperatorAssignment[]> {
    return this.http
      .get<OperatorAssignment[]>(`${this.apiUrl}/${zoneId}/operators`)
      .pipe(catchError(this.handleError));
  }

  // ========== OBTENER ASIGNACIONES DE UN PARKING ==========

  /**
   * Obtiene todas las asignaciones de todas las zonas de un parking
   * @param parkingId ID del parking
   * @returns Observable con lista de asignaciones
   */
  getAssignmentsByParking(parkingId: number): Observable<OperatorAssignment[]> {
    return this.http
      .get<OperatorAssignment[]>(
        `${environment.apiUrl}/parking-service/v1/parkings/${parkingId}/operators`
      )
      .pipe(catchError(this.handleError));
  }

  // ========== OBTENER ASIGNACIÓN POR ID ==========

  /**
   * Obtiene una asignación específica
   * @param zoneId ID de la zona
   * @param assignmentId ID de la asignación
   * @returns Observable con la asignación
   */
  getAssignmentById(
    zoneId: number,
    assignmentId: number
  ): Observable<OperatorAssignment> {
    return this.http
      .get<OperatorAssignment>(
        `${this.apiUrl}/${zoneId}/operators/${assignmentId}`
      )
      .pipe(catchError(this.handleError));
  }

  // ========== REMOVER ASIGNACIÓN ==========

  /**
   * Elimina (desactiva) una asignación de operador
   * @param zoneId ID de la zona
   * @param assignmentId ID de la asignación
   * @returns Observable void
   */
  removeAssignment(zoneId: number, assignmentId: number): Observable<void> {
    return this.http
      .delete<void>(`${this.apiUrl}/${zoneId}/operators/${assignmentId}`)
      .pipe(catchError(this.handleError));
  }

  // ========== ACTUALIZAR ASIGNACIÓN ==========

  /**
   * Actualiza las fechas de una asignación existente
   * @param zoneId ID de la zona
   * @param assignmentId ID de la asignación
   * @param request Nuevas fechas
   * @returns Observable con la asignación actualizada
   */
  updateAssignment(
    zoneId: number,
    assignmentId: number,
    request: { startDate: string; endDate?: string }
  ): Observable<AssignOperatorResponse> {
    return this.http
      .put<AssignOperatorResponse>(
        `${this.apiUrl}/${zoneId}/operators/${assignmentId}`,
        request
      )
      .pipe(catchError(this.handleError));
  }

  // ========== MANEJO DE ERRORES ==========

  private handleError(error: any): Observable<never> {
    console.error('Error en OperatorAssignmentService:', error);
    return throwError(() => error);
  }
}