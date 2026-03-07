import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';

/**
 * Componente de diálogo de confirmación
 * 
 * Para confirmar acciones destructivas (eliminar, desactivar, etc.)
 * 
 * Uso:
 * <app-confirm-dialog
 *   [show]="showDialog"
 *   title="Confirmar eliminación"
 *   message="¿Estás seguro de eliminar este usuario?"
 *   confirmText="Eliminar"
 *   cancelText="Cancelar"
 *   confirmClass="danger"
 *   (onConfirm)="handleConfirm()"
 *   (onCancel)="handleCancel()">
 * </app-confirm-dialog>
 */
@Component({
  selector: 'app-confirm-dialog',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './confirm-dialog.component.html',
  styleUrls: ['./confirm-dialog.component.css'] 
})
export class ConfirmDialogComponent {
  
  @Input() show: boolean = false;
  @Input() title: string = 'Confirmar acción';
  @Input() message: string = '¿Estás seguro de realizar esta acción?';
  @Input() confirmText: string = 'Confirmar';
  @Input() cancelText: string = 'Cancelar';
  @Input() confirmClass: 'primary' | 'danger' | 'warning' | 'success' | 'info' = 'primary';
  @Input() icon: string = 'cil-warning';
  
  @Output() onConfirm = new EventEmitter<void>();
  @Output() onCancel = new EventEmitter<void>();

  confirm(): void {
    this.onConfirm.emit();
  }

  cancel(): void {
    this.onCancel.emit();
  }

  /**
   * Cierra el dialog al hacer click en el backdrop
   */
  onBackdropClick(event: MouseEvent): void {
    if ((event.target as HTMLElement).classList.contains('dialog-backdrop')) {
      this.cancel();
    }
  }
}