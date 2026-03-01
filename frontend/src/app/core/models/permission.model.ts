/**
 * Modelo de Permiso
 * Representa un permiso del sistema
 */
export interface Permission {
  id: number;
  name: string;
  description: string;
  status: boolean;
  module?: string; // users, roles, parking, rates, reports
  createdAt: string;
  updatedAt: string;
  createdBy?: string;
  updatedBy?: string;
}

/**
 * DTO para crear un nuevo permiso
 */
export interface CreatePermissionRequest {
  name: string;
  description: string;
  module: string;        // Cambiado a requerido (sin ?)
  status: boolean;       
}

/**
 * DTO para actualizar un permiso existente
 */
export interface UpdatePermissionRequest {
  name?: string;
  description?: string;
  status?: boolean;
  module?: string;
}

/**
 * Filtros para búsqueda de permisos
 */
export interface PermissionFilters {
  search?: string;
  module?: string;
  status?: boolean;
  page?: number;
  size?: number;
  sortBy?: string;
  sortDirection?: 'asc' | 'desc';
}

/**
 * Permisos agrupados por módulo
 */
export interface PermissionGroup {
  module: string;
  permissions: Permission[];
}