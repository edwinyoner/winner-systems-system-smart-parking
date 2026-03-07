import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  CardComponent,
  CardBodyComponent,
  CardHeaderComponent,
  ColComponent,
  RowComponent,
  BadgeComponent,
  ProgressComponent
} from '@coreui/angular';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import {
  faCircleInfo,
  faRocket,
  faCode,
  faServer,
  faMobileAlt,
  faCloud,
  faShieldAlt,
  faChartLine,
  faUsers,
  faGraduationCap,
  faUniversity,
  faMapMarkerAlt,
  faEnvelope,
  faBrain,
  faMicrochip,
  faDatabase,
  faLaptopCode,
  faCog,
  faNetworkWired,
  faDisplay,
  faCar
} from '@fortawesome/free-solid-svg-icons';
import { 
  faGithub,
  faLinkedin, 
  faJava, 
  faAngular, 
  faDocker, 
  faPython, 
  faGolang,
  faAndroid,
  faApple
} from '@fortawesome/free-brands-svg-icons';

interface Technology {
  name: string;
  category: string;
  icon?: any;
  version?: string;
  color: string;
  description: string;
}

interface Feature {
  title: string;
  description: string;
  icon: any;
  color: string;
}

interface Microservice {
  name: string;
  tech: string;
  database: string;
  port: number;
  status: string;
  description: string;
  icon: any;
  color: string;
}

interface ClientChannel {
  name: string;
  platform: string;
  target: string;
  status: string;
  description: string;
  icon: any;
  color: string;
}

@Component({
  selector: 'app-about',
  standalone: true,
  imports: [
    CommonModule,
    CardComponent,
    CardBodyComponent,
    CardHeaderComponent,
    ColComponent,
    RowComponent,
    BadgeComponent,
    ProgressComponent,
    FontAwesomeModule
  ],
  templateUrl: './about.component.html',
  styleUrls: ['./about.component.css']
})
export class AboutComponent implements OnInit {

  // Iconos
  icons = {
    info: faCircleInfo,
    rocket: faRocket,
    code: faCode,
    server: faServer,
    mobile: faMobileAlt,
    cloud: faCloud,
    shield: faShieldAlt,
    chart: faChartLine,
    users: faUsers,
    graduation: faGraduationCap,
    university: faUniversity,
    location: faMapMarkerAlt,
    email: faEnvelope,
    github: faGithub,
    linkedin: faLinkedin,
    brain: faBrain,
    chip: faMicrochip,
    database: faDatabase,
    laptop: faLaptopCode,
    settings: faCog,
    network: faNetworkWired,
    display: faDisplay,
    car: faCar,
    java: faJava,
    angular: faAngular,
    docker: faDocker,
    python: faPython,
    golang: faGolang,
    android: faAndroid,
    apple: faApple
  };

  // Información del sistema
  systemInfo = {
    name: 'Smart Parking System',
    version: '1.0.0',
    releaseDate: 'Marzo 2026',
    status: 'Producción',
    description: 'Sistema integral de gestión de estacionamientos inteligentes con IA, IoT y 3 canales de cliente (Web, Móvil, Displays LED) para la Municipalidad de Huaraz, desarrollado como proyecto de tesis.',
    university: 'Universidad Nacional Santiago Antúnez de Mayolo',
    faculty: 'Facultad de Ingeniería de Sistemas e Informática',
    location: 'Huaraz, Áncash - Perú'
  };

  // Canales de Cliente (3 tipos)
  clientChannels: ClientChannel[] = [
    {
      name: 'Web Angular',
      platform: 'Angular 20 + CoreUI',
      target: 'Operadores y Administradores',
      status: 'Operativo (93%)',
      description: 'Dashboard administrativo con 14/15 módulos completos. Control total del sistema, transacciones, reportes y gestión.',
      icon: this.icons.angular,
      color: 'danger'
    },
    {
      name: 'App Móvil Flutter',
      platform: 'Flutter 3.x (iOS/Android)',
      target: 'Ciudadanos/Conductores',
      status: 'Planificado',
      description: 'Consulta de disponibilidad en tiempo real, mapas interactivos, navegación GPS y reservas (versión futura).',
      icon: this.icons.mobile,
      color: 'info'
    },
    {
      name: 'Displays LED ESP32',
      platform: 'ESP32 + Panel LED 64x32',
      target: 'Información Pública',
      status: 'Planificado',
      description: 'Pantallas en avenidas principales mostrando disponibilidad en tiempo real (actualización cada 10s).',
      icon: this.icons.display,
      color: 'warning'
    }
  ];

  // ⭐ ACTUALIZADO: Microservicios con MongoDB en ia-service
  microservices: Microservice[] = [
    {
      name: 'Auth Service',
      tech: 'Java 25 + Spring Boot 3.x',
      database: 'PostgreSQL 15',
      port: 8081,
      status: 'Operativo',
      description: 'Autenticación JWT, gestión de usuarios, roles y permisos con assignedParkings',
      icon: this.icons.shield,
      color: 'success'
    },
    {
      name: 'Parking Service',
      tech: 'Java 25 + Spring Boot 3.x',
      database: 'Oracle 23ai',
      port: 8082,
      status: 'Operativo',
      description: 'Core de negocio: parkings, transacciones, pagos, infracciones. 15 módulos completos.',
      icon: this.icons.server,
      color: 'primary'
    },
    {
      name: 'IoT Service',
      tech: 'Go 1.21 + Gin Framework',
      database: 'MySQL 8.0',
      port: 8083,
      status: 'En Desarrollo',
      description: 'Orquestador IoT: ESP32-CAM (placas), ESP32 Displays (info pública), sensores HC-SR04',
      icon: this.icons.chip,
      color: 'info'
    },
    {
      name: 'IA Service',
      tech: 'Python 3.11 + FastAPI',
      database: 'MongoDB 7.x (NoSQL)', // ⭐ ACTUALIZADO
      port: 8084,
      status: 'En Desarrollo',
      description: 'Reconocimiento con YOLOv8 + EasyOCR. Analytics ML, métricas de confianza y mejora continua del modelo', // ⭐ ACTUALIZADO
      icon: this.icons.brain,
      color: 'warning'
    }
  ];

  // ⭐ ACTUALIZADO: Características con Mejora Continua de IA
  features: Feature[] = [
    {
      title: '3 Canales de Cliente',
      description: 'Angular Web (operadores), Flutter Móvil (ciudadanos), ESP32 Displays LED (información pública en avenidas). Cobertura total de usuarios.',
      icon: this.icons.mobile,
      color: 'primary'
    },
    {
      title: 'Gestión en Tiempo Real',
      description: 'Monitoreo continuo con auto-refresh cada 30s, dashboard interactivo, alertas visuales y WebSocket para updates instantáneos.',
      icon: this.icons.chart,
      color: 'info'
    },
    {
      title: 'Arquitectura de Microservicios',
      description: '4 servicios independientes con 4 bases de datos (PostgreSQL, Oracle, MySQL, MongoDB). Diversificación tecnológica completa: 3 RDBMS + 1 NoSQL.', // ⭐ ACTUALIZADO
      icon: this.icons.network,
      color: 'success'
    },
    {
      title: 'Seguridad Enterprise',
      description: 'JWT con assignedParkings, control de acceso granular por operador, auditoría completa (createdBy/updatedBy) y encriptación.',
      icon: this.icons.shield,
      color: 'danger'
    },
    {
      title: 'IA con Mejora Continua', // ⭐ ACTUALIZADO
      description: 'YOLOv8 + EasyOCR con MongoDB para analytics. Guarda métricas, detecta errores comunes (O vs 0, I vs 1) y genera datasets para reentrenamiento con placas peruanas reales.', // ⭐ ACTUALIZADO
      icon: this.icons.brain,
      color: 'warning'
    },
    {
      title: 'Hardware IoT Dual',
      description: 'ESP32-CAM (captura y reconocimiento), ESP32 Displays LED (info pública), HC-SR04 (ocupación). Integración completa.',
      icon: this.icons.chip,
      color: 'purple'
    }
  ];

  // ⭐ ACTUALIZADO: Stack tecnológico con MongoDB
  technologies: Technology[] = [
    // Frontend & Clientes
    {
      name: 'Angular',
      category: 'Frontend Web',
      icon: this.icons.angular,
      version: '20',
      color: 'danger',
      description: 'Framework SPA con standalone components'
    },
    {
      name: 'Flutter',
      category: 'App Móvil',
      icon: this.icons.mobile,
      version: '3.x',
      color: 'info',
      description: 'iOS + Android (ciudadanos)'
    },
    {
      name: 'TypeScript',
      category: 'Frontend Web',
      icon: this.icons.code,
      version: '5.x',
      color: 'info',
      description: 'Tipado estático para JavaScript'
    },
    {
      name: 'CoreUI',
      category: 'Frontend Web',
      icon: this.icons.laptop,
      version: '5.x',
      color: 'purple',
      description: 'Framework de componentes UI'
    },
    
    // Backend - Microservicios
    {
      name: 'Java',
      category: 'Backend - Auth/Parking',
      icon: this.icons.java,
      version: '25',
      color: 'danger',
      description: 'Spring Boot 3.x + Spring Framework 6.x'
    },
    {
      name: 'Go',
      category: 'Backend - IoT',
      icon: this.icons.golang,
      version: '1.21',
      color: 'info',
      description: 'iot-service con Gin Framework'
    },
    {
      name: 'Python',
      category: 'Backend - IA',
      icon: this.icons.python,
      version: '3.11',
      color: 'warning',
      description: 'ia-service con FastAPI + YOLO'
    },
    
    // ⭐ ACTUALIZADO: Bases de Datos (4 diferentes: 3 SQL + 1 NoSQL)
    {
      name: 'Oracle Database',
      category: 'Base de Datos SQL',
      icon: this.icons.database,
      version: '23ai',
      color: 'danger',
      description: 'parking-service (core de negocio)'
    },
    {
      name: 'PostgreSQL',
      category: 'Base de Datos SQL',
      icon: this.icons.database,
      version: '15',
      color: 'info',
      description: 'auth-service (autenticación)'
    },
    {
      name: 'MySQL',
      category: 'Base de Datos SQL',
      icon: this.icons.database,
      version: '8.0',
      color: 'primary',
      description: 'iot-service (dispositivos IoT)'
    },
    {
      name: 'MongoDB', // ⭐ NUEVO
      category: 'Base de Datos NoSQL',
      icon: this.icons.database,
      version: '7.x',
      color: 'success',
      description: 'ia-service (analytics ML + métricas)'
    },
    {
      name: 'Redis',
      category: 'Cache',
      icon: this.icons.database,
      version: '7',
      color: 'danger',
      description: 'Cache + Pub/Sub para displays'
    },
    
    // Infraestructura
    {
      name: 'Spring Cloud',
      category: 'Infraestructura',
      icon: this.icons.cloud,
      version: '2023.x',
      color: 'success',
      description: 'Gateway + Config + Eureka'
    },
    {
      name: 'RabbitMQ',
      category: 'Message Broker',
      icon: this.icons.network,
      version: '3.12',
      color: 'warning',
      description: 'Event-driven async processing'
    },
    {
      name: 'MinIO',
      category: 'Object Storage',
      icon: this.icons.database,
      version: 'Latest',
      color: 'info',
      description: 'Almacenamiento imágenes ESP32-CAM'
    },
    {
      name: 'Docker',
      category: 'DevOps',
      icon: this.icons.docker,
      version: 'Latest',
      color: 'info',
      description: 'Contenedorización de servicios'
    }
  ];

  // ⭐ ACTUALIZADO: Estadísticas (ahora son 4 BD)
  projectStats = [
    { label: 'Canales de Cliente', value: 3, icon: this.icons.mobile, color: 'primary' },
    { label: 'Microservicios', value: 4, icon: this.icons.server, color: 'info' },
    { label: 'Bases de Datos', value: 4, icon: this.icons.database, color: 'success' }, // ⭐ CAMBIADO de 3 a 4
    { label: 'Componentes Angular', value: 90, icon: this.icons.angular, color: 'danger' },
    { label: 'Endpoints API', value: 155, icon: this.icons.code, color: 'warning' }, // ⭐ ACTUALIZADO
    { label: 'Tablas de BD', value: 35, icon: this.icons.database, color: 'purple' } // ⭐ ACTUALIZADO (32 + 3 colecciones MongoDB)
  ];

  // ⭐ ACTUALIZADO: Fases del proyecto con MongoDB
  projectPhases = [
    {
      phase: 'Fase 1 - Core System',
      status: 'Completado',
      progress: 100,
      items: [
        '✅ auth-service: JWT, roles, permisos, usuarios (PostgreSQL)',
        '✅ parking-service Backend: 15 módulos de dominio completos (Oracle)',
        '✅ Frontend Angular 20: Dashboard + 14/15 módulos operativos',
        '✅ Control de acceso por operador con assignedParkings',
        '✅ Infrastructure: Gateway, Config Server, Eureka Server'
      ]
    },
    {
      phase: 'Fase 2 - Frontend Completo',
      status: 'En Progreso',
      progress: 93,
      items: [
        '✅ Parking, Zone, Space, Shift, Rate, ParkingShiftRate',
        '✅ DocumentType, PaymentType, OperatorAssignment',
        '✅ Transaction (Entry, Exit, Payment, List, Detail)',
        '✅ Customer, Vehicle, CustomerVehicle',
        '⏳ Infraction (pendiente - último módulo)'
      ]
    },
    {
      phase: 'Fase 3 - IoT & IA Services',
      status: 'Planificado',
      progress: 0,
      items: [
        '⏳ iot-service: Go + MySQL + REST API + WebSocket',
        '⏳ ia-service: Python + FastAPI + YOLOv8 + EasyOCR + MongoDB', // ⭐ ACTUALIZADO
        '⏳ MongoDB Collections: plate_recognitions, model_metrics, training_datasets', // ⭐ NUEVO
        '⏳ Analytics ML: confianza promedio, errores comunes, mejora continua', // ⭐ NUEVO
        '⏳ Event-driven con RabbitMQ (async processing)',
        '⏳ Endpoint público para ESP32 Displays LED'
      ]
    },
    {
      phase: 'Fase 4 - App Móvil Flutter',
      status: 'Planificado',
      progress: 0,
      items: [
        '⏳ Flutter 3.x: UI/UX diseño para iOS + Android',
        '⏳ Mapa interactivo con disponibilidad en tiempo real',
        '⏳ Integración Google Maps + WebSocket',
        '⏳ Filtros por cercanía y tipo de vehículo',
        '⏳ (Futuro) Login ciudadano + reservas + pagos'
      ]
    },
    {
      phase: 'Fase 5 - Hardware IoT',
      status: 'Planificado',
      progress: 0,
      items: [
        '⏳ ESP32-CAM (2-3 unidades): Firmware captura automática',
        '⏳ ESP32 Displays LED (3-5 unidades): Firmware consulta backend',
        '⏳ Paneles LED 64x32 en avenidas principales',
        '⏳ HC-SR04: Sensores de ocupación por espacio',
        '⏳ Instalación piloto en 1 parking de Huaraz'
      ]
    }
  ];

  // Equipo de desarrollo
  teamMembers = [
    {
      name: 'Edwin Yoner',
      role: 'Full Stack Developer & Arquitecto',
      email: 'edwin.yoner@winner-systems.com',
      github: 'edwinyoner',
      linkedin: 'edwinyoner'
    }
  ];

  ngOnInit(): void {}

  getTechnologyColor(color: string): string {
    const colorMap: Record<string, string> = {
      primary: '#00E5FF',
      secondary: '#9E9E9E',
      info: '#00BCD4',
      success: '#00C853',
      warning: '#FFC400',
      danger: '#FF1744',
      purple: '#9C27B0'
    };
    return colorMap[color] || '#2C3E50';
  }
}