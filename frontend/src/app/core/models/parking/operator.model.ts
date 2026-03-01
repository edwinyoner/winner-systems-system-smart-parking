/**
 * Modelo simplificado para operadores
 * Proviene del auth-service endpoint /users/operators
 */
export interface Operator {
  id: number;
  firstName: string;
  lastName: string;
  username: string;
  email: string;
  isActive: boolean;
}

/**
 * Modelo extendido con información completa del operador
 * (Opcional - si necesitas más detalles en el futuro)
 */
export interface OperatorDetail extends Operator {
  phoneNumber?: string;
  createdAt?: Date;
  updatedAt?: Date;
}