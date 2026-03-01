import { INavData } from '@coreui/angular';

export interface INavDataWithPermissions extends INavData {
  requiredPermissions?: string[];  // Permisos requeridos
}

export const navItems: INavDataWithPermissions[] = [
  // PANEL PRINCIPAL
  {
    title: true,
    name: 'PANEL PRINCIPAL',
    class: 'nav-title'
  },
  {
    name: 'Dashboard',
    url: '/dashboard',
    icon: 'fa-solid fa-gauge-high',
    badge: { color: 'info', text: 'INICIO' }
    // Sin permisos = accesible para todos
  },

  // GESTIÓN DE ACCESOS
  {
    title: true,
    name: 'GESTIÓN DE ACCESOS',
    class: 'nav-title',
    requiredPermissions: ['users.read']  // Solo quien tenga users.read
  },
  {
    name: 'Usuarios',
    url: '/users',
    icon: 'fa-solid fa-user',
    requiredPermissions: ['users.read']  // ✅
  },
  {
    name: 'Roles',
    url: '/roles',
    icon: 'fa-solid fa-shield-halved',
    requiredPermissions: ['roles.read']  // ✅
  },
  {
    name: 'Permisos',
    url: '/permissions',
    icon: 'fa-solid fa-lock',
    requiredPermissions: ['permissions.read']  // ✅
  },

  // GESTIÓN DE ESTACIONAMIENTO
  {
    title: true,
    name: 'GESTIÓN DE ESTACIONAMIENTO',
    class: 'nav-title'
  },
  {
    name: 'Espacios',
    url: '/parking/spaces',
    icon: 'fa-solid fa-table-cells',
    requiredPermissions: ['parking.read'],
    badge: { color: 'success', text: '250' }
  },
  {
    name: 'Vehículos',
    url: '/parking/vehicles',
    icon: 'fa-solid fa-car',
    requiredPermissions: ['parking.read']
  },
  {
    name: 'Tarifas',
    url: '/parking/rates',
    icon: 'fa-solid fa-dollar-sign',
    requiredPermissions: ['rates.read']
  },
  {
    name: 'Infracciones',
    url: '/parking/violations',
    icon: 'fa-solid fa-triangle-exclamation',
    badge: { color: 'danger', text: '3' },
    requiredPermissions: ['parking.read']
  },

  // CLIENTES Y PAGOS
  {
    title: true,
    name: 'CLIENTES Y PAGOS',
    class: 'nav-title',
    requiredPermissions: ['rates.read']
  },
  {
    name: 'Clientes',
    url: '/clients',
    icon: 'fa-solid fa-address-book',
    requiredPermissions: ['rates.read']
  },
  {
    name: 'Pagos',
    url: '/payments',
    icon: 'fa-solid fa-credit-card',
    requiredPermissions: ['rates.read']
  },
  {
    name: 'Facturación',
    url: '/billing',
    icon: 'fa-solid fa-file-invoice-dollar',
    requiredPermissions: ['users.delete']  // Solo ADMIN
  },

  // MONITOREO IOT
  {
    title: true,
    name: 'MONITOREO Y CONTROL IOT',
    class: 'nav-title'
  },
  {
    name: 'Sensores',
    url: '/iot/sensors',
    icon: 'fa-solid fa-wifi',
    badge: { color: 'info', text: 'ACTIVO' }
  },
  {
    name: 'Cámaras',
    url: '/iot/cameras',
    icon: 'fa-solid fa-video'
  },
  {
    name: 'Barreras',
    url: '/iot/barriers',
    icon: 'fa-solid fa-road-barrier'
  },

  // REPORTES
  {
    title: true,
    name: 'REPORTES Y ANALÍTICAS',
    class: 'nav-title',
    requiredPermissions: ['reports.view']
  },
  {
    name: 'Reportes Generales',
    url: '/reports/general',
    icon: 'fa-solid fa-chart-pie',
    requiredPermissions: ['reports.view']
  },

  // CONFIGURACIÓN
  {
    title: true,
    name: 'CONFIGURACIÓN DEL SISTEMA',
    class: 'nav-title',
    requiredPermissions: ['users.delete']  // Solo ADMIN
  },
  {
    name: 'Configuración General',
    url: '/settings/general',
    icon: 'fa-solid fa-gear',
    requiredPermissions: ['users.delete']  // Solo ADMIN
  },

  // SOPORTE
  {
    title: true,
    name: 'SOPORTE',
    class: 'nav-title'
  },
  {
    name: 'Soporte Técnico',
    url: '/support',
    icon: 'fa-solid fa-headset',
    badge: { color: 'success', text: 'EN LÍNEA' }
  },

  // DOCUMENTACIÓN
  {
    title: true,
    name: 'DOCUMENTACIÓN',
    class: 'nav-title'
  },
  {
    name: 'Manual de Usuario',
    url: '/docs/user-manual',
    icon: 'fa-solid fa-book'
  },
  {
    name: 'Acerca del Sistema',
    url: '/about',
    icon: 'fa-solid fa-circle-info'
  }
];