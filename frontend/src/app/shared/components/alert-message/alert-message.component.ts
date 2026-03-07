import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';

/**
 * Componente de mensajes de alerta
 * 
 * Tipos: success, error, warning, info
 * Puede ser dismissible (con botón X para cerrar)
 * 
 * Uso:
 * <app-alert-message 
 *   type="success" 
 *   message="Operación exitosa"
 *   [dismissible]="true"
 *   (onDismiss)="handleDismiss()">
 * </app-alert-message>
 */
@Component({
  selector: 'app-alert-message',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './alert-message.component.html',
  styleUrls: ['./alert-message.component.css']  
})
export class AlertMessageComponent {
  
  @Input() type: 'success' | 'error' | 'warning' | 'info' = 'info';
  @Input() message: string = '';
  @Input() dismissible: boolean = true;
  @Input() icon: string = ''; // Icono personalizado (opcional)
  @Input() compact: boolean = false; // Variante compacta
  
  @Output() onDismiss = new EventEmitter<void>();

  visible = true;

  /**
   * Obtiene el icono según el tipo de alerta
   * Usa CoreUI icons
   */
  getIcon(): string {
    if (this.icon) {
      return this.icon;
    }

    switch (this.type) {
      case 'success':
        return 'cil-check-circle';
      case 'error':
        return 'cil-x-circle';
      case 'warning':
        return 'cil-warning';
      case 'info':
        return 'cil-info';
      default:
        return 'cil-info';
    }
  }

  /**
   * Cierra la alerta
   */
  dismiss(): void {
    this.visible = false;
    this.onDismiss.emit();
  }
}