import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Subscription } from 'rxjs';
import { LoadingService } from '../../../core/services/loading.service';

/**
 * Componente de spinner de carga global
 * 
 * Se muestra automáticamente cuando hay peticiones HTTP en curso
 * Controlado por LoadingService + LoadingInterceptor
 */
@Component({
  selector: 'app-loading-spinner',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './loading-spinner.component.html',
  styleUrls: ['./loading-spinner.component.css']
})
export class LoadingSpinnerComponent implements OnInit, OnDestroy {
  
  isLoading = false;
  private subscription?: Subscription;

  constructor(private loadingService: LoadingService) {}

  ngOnInit(): void {
    // Suscribirse al estado de loading
    this.subscription = this.loadingService.loading$.subscribe(
      loading => this.isLoading = loading
    );
  }

  ngOnDestroy(): void {
    // Limpiar suscripción
    if (this.subscription) {
      this.subscription.unsubscribe();
    }
  }
}