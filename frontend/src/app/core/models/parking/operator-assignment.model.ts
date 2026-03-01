/**
 * Modelo para la asignación de operadores a zonas
 * Representa la relación entre un operador, una zona y un turno
 */
export interface OperatorAssignment {
  id: number;
  zoneId: number;
  zoneName: string;
  userId: number;
  userName: string;
  userEmail: string;
  shiftId: number;
  shiftName: string;
  assignedAt: Date;
  startDate: Date;
  endDate?: Date;
  isActive: boolean;
}

/**
 * Request para asignar un operador a una zona
 */
export interface AssignOperatorRequest {
  userId: number;
  shiftId: number;
  startDate: string; // Formato ISO: "2025-02-28"
  endDate?: string;  // Formato ISO: "2025-12-31" (opcional)
}

/**
 * Response con información completa de la asignación
 */
export interface AssignOperatorResponse {
  id: number;
  zoneId: number;
  zoneName: string;
  userId: number;
  userName: string;
  userEmail: string;
  shiftId: number;
  shiftName: string;
  startDate: string;
  endDate?: string;
  assignedAt: string;
  isActive: boolean;
}