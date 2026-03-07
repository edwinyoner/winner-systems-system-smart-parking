import { Component, Input, Output, EventEmitter, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

/**
 * Componente de tabla de datos reutilizable
 * 
 * Características:
 * - Paginación integrada
 * - Ordenamiento por columnas
 * - Acciones personalizables (ver, editar, eliminar)
 * - Estado de carga
 * - Responsive
 * - Mensaje cuando no hay datos
 * 
 * Uso:
 * <app-data-table
 *   [data]="customers"
 *   [columns]="columns"
 *   [totalRecords]="totalRecords"
 *   [pageSize]="pageSize"
 *   [currentPage]="currentPage"
 *   [loading]="loading"
 *   [showActions]="true"
 *   (onView)="viewCustomer($event)"
 *   (onEdit)="editCustomer($event)"
 *   (onDelete)="deleteCustomer($event)"
 *   (onPageChange)="loadPage($event)"
 *   (onPageSizeChange)="changePageSize($event)">
 * </app-data-table>
 */
@Component({
  selector: 'app-data-table',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './data-table.component.html',
  styleUrls: ['./data-table.component.css']
})
export class DataTableComponent implements OnInit {
  
  // ==========================================
  // INPUTS
  // ==========================================
  
  /** Datos a mostrar en la tabla */
  @Input() data: any[] = [];
  
  /** Configuración de columnas */
  @Input() columns: TableColumn[] = [];
  
  /** Total de registros (para paginación) */
  @Input() totalRecords: number = 0;
  
  /** Tamaño de página actual */
  @Input() pageSize: number = 10;
  
  /** Página actual (0-indexed) */
  @Input() currentPage: number = 0;
  
  /** Estado de carga */
  @Input() loading: boolean = false;
  
  /** Mostrar columna de acciones */
  @Input() showActions: boolean = true;
  
  /** Acciones disponibles */
  @Input() actions: TableAction[] = [
    { name: 'view', icon: 'cil-eye', color: 'info', label: 'Ver' },
    { name: 'edit', icon: 'cil-pencil', color: 'warning', label: 'Editar' },
    { name: 'delete', icon: 'cil-trash', color: 'danger', label: 'Eliminar' }
  ];
  
  /** Mensaje cuando no hay datos */
  @Input() emptyMessage: string = 'No se encontraron registros';
  
  /** Opciones de tamaño de página */
  @Input() pageSizeOptions: number[] = [5, 10, 20, 50, 100];
  
  /** Mostrar información de paginación */
  @Input() showPaginationInfo: boolean = true;
  
  /** Columna por defecto para ordenar */
  @Input() defaultSortColumn?: string;
  
  /** Dirección por defecto de ordenamiento */
  @Input() defaultSortDirection: 'asc' | 'desc' = 'asc';
  
  // ==========================================
  // OUTPUTS
  // ==========================================
  
  /** Evento cuando se hace clic en "Ver" */
  @Output() onView = new EventEmitter<any>();
  
  /** Evento cuando se hace clic en "Editar" */
  @Output() onEdit = new EventEmitter<any>();
  
  /** Evento cuando se hace clic en "Eliminar" */
  @Output() onDelete = new EventEmitter<any>();
  
  /** Evento cuando cambia de página */
  @Output() onPageChange = new EventEmitter<number>();
  
  /** Evento cuando cambia el tamaño de página */
  @Output() onPageSizeChange = new EventEmitter<number>();
  
  /** Evento cuando se ordena por columna */
  @Output() onSort = new EventEmitter<SortEvent>();
  
  /** Evento para acciones personalizadas */
  @Output() onCustomAction = new EventEmitter<CustomActionEvent>();
  
  // ==========================================
  // PROPIEDADES INTERNAS
  // ==========================================
  
  sortColumn?: string;
  sortDirection: 'asc' | 'desc' = 'asc';
  
  // ==========================================
  // LIFECYCLE
  // ==========================================
  
  ngOnInit(): void {
    // Aplicar ordenamiento por defecto
    if (this.defaultSortColumn) {
      this.sortColumn = this.defaultSortColumn;
      this.sortDirection = this.defaultSortDirection;
    }
  }
  
  // ==========================================
  // MÉTODOS PÚBLICOS
  // ==========================================
  
  /**
   * Obtiene el valor de una columna para un registro
   */
  getCellValue(row: any, column: TableColumn): any {
    if (column.valueGetter) {
      return column.valueGetter(row);
    }
    
    // Soporte para propiedades anidadas (ej: "customer.firstName")
    const keys = column.field.split('.');
    let value = row;
    
    for (const key of keys) {
      value = value?.[key];
    }
    
    return value;
  }
  
  /**
   * Formatea el valor de una celda según el tipo
   */
  formatCellValue(value: any, column: TableColumn): string {
    if (value === null || value === undefined) {
      return '-';
    }
    
    if (column.formatter) {
      return column.formatter(value);
    }
    
    switch (column.type) {
      case 'date':
        return this.formatDate(value);
      case 'datetime':
        return this.formatDateTime(value);
      case 'currency':
        return this.formatCurrency(value);
      case 'number':
        return this.formatNumber(value);
      case 'boolean':
        return value ? 'Sí' : 'No';
      case 'badge':
        return value;
      default:
        return String(value);
    }
  }
  
  /**
   * Obtiene la clase CSS para una celda badge
   */
  getBadgeClass(value: any, column: TableColumn): string {
    if (column.badgeClass) {
      return column.badgeClass(value);
    }
    return 'badge-secondary';
  }
  
  /**
   * Ordena por columna
   */
  sortBy(column: TableColumn): void {
    if (!column.sortable) return;
    
    // Cambiar dirección si es la misma columna
    if (this.sortColumn === column.field) {
      this.sortDirection = this.sortDirection === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortColumn = column.field;
      this.sortDirection = 'asc';
    }
    
    // Emitir evento
    this.onSort.emit({
      column: this.sortColumn,
      direction: this.sortDirection
    });
  }
  
  /**
   * Cambia de página
   */
  goToPage(page: number): void {
    if (page < 0 || page >= this.getTotalPages()) return;
    this.onPageChange.emit(page);
  }
  
  /**
   * Cambia el tamaño de página
   */
  changePageSize(): void {
    this.onPageSizeChange.emit(this.pageSize);
  }
  
  /**
   * Ejecuta una acción
   */
  executeAction(action: TableAction, row: any): void {
    switch (action.name) {
      case 'view':
        this.onView.emit(row);
        break;
      case 'edit':
        this.onEdit.emit(row);
        break;
      case 'delete':
        this.onDelete.emit(row);
        break;
      default:
        this.onCustomAction.emit({ action: action.name, row });
    }
  }
  
  /**
   * Verifica si una acción debe mostrarse para un registro
   */
  shouldShowAction(action: TableAction, row: any): boolean {
    if (action.condition) {
      return action.condition(row);
    }
    return true;
  }
  
  /**
   * Calcula el total de páginas
   */
  getTotalPages(): number {
    return Math.ceil(this.totalRecords / this.pageSize);
  }
  
  /**
   * Genera array de números de página para la paginación
   */
  getPageNumbers(): number[] {
    const totalPages = this.getTotalPages();
    const maxVisible = 5;
    const pages: number[] = [];
    
    if (totalPages <= maxVisible) {
      for (let i = 0; i < totalPages; i++) {
        pages.push(i);
      }
    } else {
      // Lógica para mostrar páginas alrededor de la actual
      const start = Math.max(0, this.currentPage - 2);
      const end = Math.min(totalPages - 1, this.currentPage + 2);
      
      for (let i = start; i <= end; i++) {
        pages.push(i);
      }
    }
    
    return pages;
  }
  
  /**
   * Obtiene el rango de registros mostrados
   */
  getRecordsRange(): string {
    if (this.totalRecords === 0) return '0 - 0 de 0';
    
    const start = this.currentPage * this.pageSize + 1;
    const end = Math.min((this.currentPage + 1) * this.pageSize, this.totalRecords);
    
    return `${start} - ${end} de ${this.totalRecords}`;
  }
  
  // ==========================================
  // MÉTODOS PRIVADOS (FORMATTERS)
  // ==========================================
  
  private formatDate(value: any): string {
    if (!value) return '-';
    const date = new Date(value);
    return date.toLocaleDateString('es-PE');
  }
  
  private formatDateTime(value: any): string {
    if (!value) return '-';
    const date = new Date(value);
    return date.toLocaleString('es-PE');
  }
  
  private formatCurrency(value: any): string {
    if (value === null || value === undefined) return '-';
    return `S/ ${Number(value).toFixed(2)}`;
  }
  
  private formatNumber(value: any): string {
    if (value === null || value === undefined) return '-';
    return Number(value).toLocaleString('es-PE');
  }
}

// ==========================================
// INTERFACES
// ==========================================

/**
 * Configuración de columna
 */
export interface TableColumn {
  /** Campo del objeto a mostrar */
  field: string;
  
  /** Encabezado de la columna */
  header: string;
  
  /** Tipo de dato */
  type?: 'text' | 'number' | 'date' | 'datetime' | 'currency' | 'boolean' | 'badge' | 'custom';
  
  /** Es ordenable */
  sortable?: boolean;
  
  /** Ancho de la columna (CSS) */
  width?: string;
  
  /** Alineación del texto */
  align?: 'left' | 'center' | 'right';
  
  /** Función personalizada para obtener el valor */
  valueGetter?: (row: any) => any;
  
  /** Función personalizada para formatear el valor */
  formatter?: (value: any) => string;
  
  /** Función para obtener clase de badge */
  badgeClass?: (value: any) => string;
}

/**
 * Configuración de acción
 */
export interface TableAction {
  /** Nombre de la acción */
  name: string;
  
  /** Icono (clase CSS) */
  icon: string;
  
  /** Color (CoreUI colors) */
  color: 'primary' | 'secondary' | 'success' | 'danger' | 'warning' | 'info';
  
  /** Etiqueta del botón */
  label: string;
  
  /** Condición para mostrar la acción */
  condition?: (row: any) => boolean;
}

/**
 * Evento de ordenamiento
 */
export interface SortEvent {
  column: string;
  direction: 'asc' | 'desc';
}

/**
 * Evento de acción personalizada
 */
export interface CustomActionEvent {
  action: string;
  row: any;
}