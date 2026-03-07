import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import {
  CardComponent,
  CardBodyComponent,
  ColComponent,
  RowComponent,
  NavModule,
  AccordionModule,
  ButtonDirective
} from '@coreui/angular';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import {
  faBook,
  faRocket,
  faUserShield,
  faCar,
  faMoneyBill,
  faExclamationTriangle,
  faChartBar,
  faCog,
  faDownload,
  faPlayCircle,
  faFileAlt,
  faQuestionCircle,
  faCheckCircle
} from '@fortawesome/free-solid-svg-icons';

interface ManualSection {
  id: string;
  title: string;
  icon: any;
  color: string;
  subsections: ManualSubsection[];
}

interface ManualSubsection {
  id: string;
  title: string;
  content: string;
  steps?: string[];
  tips?: string[];
  warnings?: string[];
}

@Component({
  selector: 'app-user-manual',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    CardComponent,
    CardBodyComponent,
    ColComponent,
    RowComponent,
    NavModule,
    AccordionModule,
    ButtonDirective,
    FontAwesomeModule
  ],
  templateUrl: './user-manual.component.html',
  styleUrls: ['./user-manual.component.css']
})
export class UserManualComponent implements OnInit {

  // Iconos
  icons = {
    book: faBook,
    rocket: faRocket,
    user: faUserShield,
    car: faCar,
    money: faMoneyBill,
    warning: faExclamationTriangle,
    chart: faChartBar,
    settings: faCog,
    download: faDownload,
    play: faPlayCircle,
    file: faFileAlt,
    question: faQuestionCircle,
    check: faCheckCircle
  };

  activeTab = signal<string>('getting-started');

  manualSections: ManualSection[] = [
    {
      id: 'getting-started',
      title: 'Primeros Pasos',
      icon: this.icons.rocket,
      color: 'primary',
      subsections: [
        {
          id: 'intro',
          title: 'Introducción al Sistema',
          content: 'Smart Parking es un sistema integral de gestión de estacionamientos diseñado para la Municipalidad de Huaraz. Permite controlar entradas, salidas, pagos, infracciones y generar reportes en tiempo real.',
          tips: [
            'El sistema está optimizado para navegadores Chrome y Edge',
            'Se recomienda una resolución mínima de 1366x768',
            'Los datos se sincronizan automáticamente cada 30 segundos'
          ]
        },
        {
          id: 'login',
          title: 'Iniciar Sesión',
          content: 'Para acceder al sistema, debe contar con credenciales válidas proporcionadas por el administrador.',
          steps: [
            'Ingrese su nombre de usuario o email',
            'Ingrese su contraseña',
            'Haga clic en "Iniciar Sesión"',
            'Si es su primer acceso, se le pedirá cambiar la contraseña'
          ],
          warnings: [
            'Después de 3 intentos fallidos, su cuenta se bloqueará por 15 minutos',
            'Las contraseñas expiran cada 90 días por seguridad'
          ]
        },
        {
          id: 'dashboard',
          title: 'Panel Principal',
          content: 'El dashboard muestra un resumen completo del estado del sistema en tiempo real.',
          steps: [
            'Estadísticas generales: parkings, zonas, espacios',
            'Estado de ocupación en tiempo real',
            'Ingresos del día y del mes',
            'Transacciones activas con alertas',
            'Top clientes y vehículos frecuentes'
          ]
        }
      ]
    },
    {
      id: 'users-roles',
      title: 'Usuarios y Roles',
      icon: this.icons.user,
      color: 'info',
      subsections: [
        {
          id: 'user-types',
          title: 'Tipos de Usuario',
          content: 'El sistema cuenta con 3 tipos de usuarios con diferentes niveles de acceso.',
          steps: [
            'ADMIN: Acceso completo al sistema, configuración y reportes',
            'AUTORIDAD: Visualización de reportes y estadísticas',
            'OPERADOR: Gestión de transacciones diarias'
          ]
        },
        {
          id: 'user-management',
          title: 'Gestión de Usuarios',
          content: 'Los administradores pueden crear, editar y desactivar usuarios.',
          steps: [
            'Ir a "Usuarios" en el menú lateral',
            'Clic en "Nuevo Usuario"',
            'Completar datos: nombre, email, rol',
            'Asignar permisos específicos',
            'Guardar y enviar credenciales al correo'
          ]
        },
        {
          id: 'permissions',
          title: 'Permisos y Roles',
          content: 'Los roles agrupan permisos para facilitar la administración.',
          steps: [
            'Ir a "Roles y Permisos"',
            'Crear nuevo rol o editar existente',
            'Seleccionar permisos por módulo',
            'Asignar rol a usuarios'
          ]
        }
      ]
    },
    {
      id: 'transactions',
      title: 'Transacciones',
      icon: this.icons.car,
      color: 'success',
      subsections: [
        {
          id: 'vehicle-entry',
          title: 'Registro de Entrada',
          content: 'Proceso para registrar la entrada de un vehículo al estacionamiento.',
          steps: [
            'Ir a "Transacciones" > "Entrada de Vehículo"',
            'Seleccionar Parking, Zona y Espacio disponible',
            'Ingresar placa del vehículo (obligatorio)',
            'Ingresar datos del cliente: documento, nombres, apellidos',
            'Opcionalmente: teléfono, email, observaciones',
            'Seleccionar operador responsable',
            'Clic en "Registrar Entrada"'
          ],
          tips: [
            'El vehículo se crea automáticamente si es nuevo',
            'El cliente se crea automáticamente si es nuevo',
            'El espacio cambia a estado OCUPADO automáticamente',
            'Se puede subir foto de entrada como evidencia'
          ],
          warnings: [
            'Verificar que la placa esté correctamente escrita',
            'El documento del cliente debe coincidir en entrada y salida'
          ]
        },
        {
          id: 'vehicle-exit',
          title: 'Registro de Salida',
          content: 'Proceso para registrar la salida de un vehículo.',
          steps: [
            'Ir a "Transacciones" > "Salida de Vehículo"',
            'Buscar por placa o seleccionar de la lista',
            'Verificar documento del cliente (debe coincidir)',
            'El sistema calcula automáticamente el tiempo y monto',
            'Confirmar datos y clic en "Registrar Salida"'
          ],
          tips: [
            'El espacio se libera automáticamente',
            'Se genera un comprobante con el monto a pagar'
          ]
        }
      ]
    },
    {
      id: 'payments',
      title: 'Pagos e Ingresos',
      icon: this.icons.money,
      color: 'warning',
      subsections: [
        {
          id: 'payment-types',
          title: 'Tipos de Pago',
          content: 'El sistema soporta múltiples métodos de pago.',
          steps: [
            'Efectivo',
            'Tarjeta de Crédito/Débito',
            'Yape',
            'Plin',
            'Transferencia Bancaria'
          ]
        }
      ]
    },
    {
      id: 'reports',
      title: 'Reportes',
      icon: this.icons.chart,
      color: 'purple',
      subsections: [
        {
          id: 'daily-report',
          title: 'Reporte Diario',
          content: 'Resumen de actividades del día.',
          steps: [
            'Ir a "Reportes" > "Reporte Diario"',
            'Seleccionar fecha',
            'Generar reporte',
            'Exportar a PDF o Excel'
          ]
        }
      ]
    },
    {
      id: 'configuration',
      title: 'Configuración',
      icon: this.icons.settings,
      color: 'dark',
      subsections: [
        {
          id: 'parking-config',
          title: 'Configurar Parkings',
          content: 'Crear y configurar estacionamientos.',
          steps: [
            'Ir a "Parkings" > "Nuevo Parking"',
            'Paso 1: Información básica',
            'Paso 2: Crear zonas',
            'Paso 3: Crear espacios',
            'Paso 4: Configurar tarifas',
            'Paso 5: Asignar operadores'
          ]
        }
      ]
    }
  ];

  ngOnInit(): void {}

  setActiveTab(tabId: string): void {
    this.activeTab.set(tabId);
  }

  downloadPDF(): void {
    console.log('Descargando manual en PDF...');
    alert('Función de descarga en desarrollo. El manual completo estará disponible próximamente.');
  }
}