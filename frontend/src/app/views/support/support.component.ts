import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import {
  CardComponent,
  CardBodyComponent,
  CardHeaderComponent,
  ColComponent,
  RowComponent,
  ButtonDirective,
  FormDirective,
  FormLabelDirective,
  FormControlDirective,
  AccordionModule,
  BadgeComponent,
  AlertComponent
} from '@coreui/angular';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import {
  faHeadset,
  faEnvelope,
  faPhone,
  faMapMarkerAlt,
  faClock,
  faQuestionCircle,
  faCheckCircle,
  faExclamationTriangle,
  faPaperPlane,
  faLifeRing,
  faComments
} from '@fortawesome/free-solid-svg-icons';

interface FAQ {
  id: number;
  question: string;
  answer: string;
  category: string;
}

interface SupportTicket {
  name: string;
  email: string;
  phone?: string;
  category: string;
  priority: string;
  subject: string;
  message: string;
}

@Component({
  selector: 'app-support',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    CardComponent,
    CardBodyComponent,
    CardHeaderComponent,
    ColComponent,
    RowComponent,
    ButtonDirective,
    FormDirective,
    FormLabelDirective,
    FormControlDirective,
    AccordionModule,
    BadgeComponent,
    AlertComponent,
    FontAwesomeModule
  ],
  templateUrl: './support.component.html',
  styleUrls: ['./support.component.css']
})
export class SupportComponent implements OnInit {

  // Iconos
  icons = {
    headset: faHeadset,
    envelope: faEnvelope,
    phone: faPhone,
    location: faMapMarkerAlt,
    clock: faClock,
    question: faQuestionCircle,
    check: faCheckCircle,
    warning: faExclamationTriangle,
    send: faPaperPlane,
    support: faLifeRing,
    chat: faComments
  };

  // Estado
  loading = signal<boolean>(false);
  success = signal<boolean>(false);
  error = signal<string | null>(null);

  // Formulario
  supportForm: FormGroup;

  // FAQ
  faqs: FAQ[] = [
    {
      id: 1,
      question: '¿Cómo registrar un vehículo en el sistema?',
      answer: 'Los vehículos se registran automáticamente al realizar la primera transacción de entrada. Solo necesita ingresar la placa del vehículo y los datos del cliente en el módulo "Entrada de Vehículo".',
      category: 'Transacciones'
    },
    {
      id: 2,
      question: '¿Cómo procesar el pago de una transacción?',
      answer: 'Vaya al módulo "Transacciones Activas", seleccione la transacción y haga clic en "Procesar Pago". El sistema calculará automáticamente el monto según el tiempo transcurrido y la tarifa configurada.',
      category: 'Pagos'
    },
    {
      id: 3,
      question: '¿Cómo registrar una infracción?',
      answer: 'Acceda al módulo "Infracciones" y seleccione "Nueva Infracción". Complete los datos del vehículo, tipo de infracción, evidencia fotográfica y descripción del incidente.',
      category: 'Infracciones'
    },
    {
      id: 4,
      question: '¿Cómo configurar las tarifas por turno?',
      answer: 'En el módulo "Parkings", edite el parking deseado y vaya a la pestaña "Configuración de Tarifas". Asigne una tarifa a cada turno (Mañana, Tarde, Noche) según su necesidad.',
      category: 'Configuración'
    },
    {
      id: 5,
      question: '¿Qué hacer si un espacio no cambia de estado?',
      answer: 'Verifique que la transacción esté correctamente registrada. Si el problema persiste, puede cambiar manualmente el estado del espacio desde el módulo "Espacios" usando el botón "Cambiar Estado".',
      category: 'Espacios'
    },
    {
      id: 6,
      question: '¿Cómo generar reportes diarios?',
      answer: 'Acceda al módulo "Reportes" y seleccione el tipo de reporte deseado (Diario, Ocupación, Ingresos). Configure el rango de fechas y haga clic en "Generar Reporte".',
      category: 'Reportes'
    },
    {
      id: 7,
      question: '¿Cómo asignar operadores a zonas?',
      answer: 'En el módulo "Zonas", seleccione la zona deseada y vaya a "Asignar Operadores". Elija el operador, el turno y las fechas de asignación.',
      category: 'Operadores'
    },
    {
      id: 8,
      question: '¿Qué hacer si olvidé mi contraseña?',
      answer: 'En la pantalla de login, haga clic en "¿Olvidaste tu contraseña?". Ingrese su correo electrónico y recibirá un enlace para restablecer su contraseña.',
      category: 'Cuenta'
    }
  ];

  // Información de contacto
  contactInfo = {
    email: 'soporte@smartparking.com.pe',
    phone: '+51 943 123 456',
    whatsapp: '+51 943 123 456',
    address: 'Jr. José Olaya 530, Huaraz - Áncash',
    schedule: 'Lunes a Domingo: 7:00 AM - 11:00 PM'
  };

  constructor(private fb: FormBuilder) {
    this.supportForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(3)]],
      email: ['', [Validators.required, Validators.email]],
      phone: ['', [Validators.pattern(/^[0-9]{9}$/)]],
      category: ['', Validators.required],
      priority: ['', Validators.required],
      subject: ['', [Validators.required, Validators.minLength(5)]],
      message: ['', [Validators.required, Validators.minLength(20)]]
    });
  }

  ngOnInit(): void {}

  onSubmit(): void {
    if (this.supportForm.invalid) {
      this.supportForm.markAllAsTouched();
      this.error.set('Por favor, complete todos los campos requeridos correctamente');
      return;
    }

    this.loading.set(true);
    this.error.set(null);
    this.success.set(false);

    const ticket: SupportTicket = this.supportForm.value;

    setTimeout(() => {
      console.log('Ticket enviado:', ticket);
      
      this.loading.set(false);
      this.success.set(true);
      this.supportForm.reset();

      setTimeout(() => {
        this.success.set(false);
      }, 5000);
    }, 2000);
  }

  getFieldError(fieldName: string): string {
    const field = this.supportForm.get(fieldName);
    
    if (field?.hasError('required')) {
      return 'Este campo es requerido';
    }
    if (field?.hasError('email')) {
      return 'Email inválido';
    }
    if (field?.hasError('minlength')) {
      const minLength = field.errors?.['minlength'].requiredLength;
      return `Mínimo ${minLength} caracteres`;
    }
    if (field?.hasError('pattern')) {
      return 'Formato inválido (9 dígitos)';
    }
    
    return '';
  }

  isFieldInvalid(fieldName: string): boolean {
    const field = this.supportForm.get(fieldName);
    return !!(field?.invalid && field?.touched);
  }
}