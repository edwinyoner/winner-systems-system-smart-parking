package com.winnersystems.smartparking.parking.domain.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidad de dominio que representa un CLIENTE/CONDUCTOR.
 *
 * Un cliente es una persona que utiliza el estacionamiento, identificada por su
 * documento oficial (DNI, CE, Pasaporte, etc.). El sistema registra clientes
 * conforme ingresan al estacionamiento para propósitos de seguridad y auditoría.
 *
 * En Fase 1, los clientes son registrados manualmente por operadores al momento
 * de estacionar un vehículo. En fases posteriores, podrán auto-registrarse desde
 * la aplicación móvil con autenticación OAuth (Google/Apple/Microsoft).
 *
 * Casos de uso:
 * - Seguridad: verificar que quien retira el vehículo es quien lo dejó
 * - Contacto: notificar en caso de emergencia o incidente
 * - Historial: seguimiento de clientes frecuentes
 * - App móvil: vincular con cuenta de usuario (authExternalId)
 *
 * @author Edwin Yoner - Winner Systems - Smart Parking Platform
 * @version 1.0
 */
public class Customer {

   // ========================= CAMPOS DE IDENTIDAD =========================

   private Long id;
   private Long documentTypeId;                           // FK a DocumentType (NOT NULL)
   private String documentNumber;                         // Número único de documento

   // ========================= CAMPOS DE DATOS PERSONALES =========================

   private String firstName;                              // Nombre(s)
   private String lastName;                               // Apellido(s)

   // ========================= CAMPOS DE CONTACTO =========================

   private String phone;                                  // Teléfono (opcional)
   private String email;                                  // Email (opcional)
   private String address;                                // Dirección (opcional)

   // ========================= CAMPOS DE TRACKING =========================

   private LocalDateTime registrationDate;                // Fecha de registro inicial
   private LocalDateTime firstSeenDate;                   // Primera vez en el sistema
   private LocalDateTime lastSeenDate;                    // Última vez vista
   private Integer totalVisits;                           // Contador de visitas acumuladas

   // ========================= CAMPOS DE INTEGRACIÓN APP MÓVIL (Fase 2) =========================

   private Long authExternalId;                           // FK a auth-external (nullable)
   // NULL en Fase 1
   // Se usa en Fase 2 con app móvil

   // ========================= CAMPOS DE AUDITORÍA =========================

   private LocalDateTime createdAt;
   private Long createdBy;                                // ID del usuario que creó
   private LocalDateTime updatedAt;
   private Long updatedBy;                                // ID del usuario que actualizó
   private LocalDateTime deletedAt;                       // Soft delete
   private Long deletedBy;

   // ========================= CONSTRUCTORES =========================

   /**
    * Constructor vacío - Inicializa con valores por defecto.
    */
   public Customer() {
      this.totalVisits = 0;
      this.registrationDate = LocalDateTime.now();
      this.createdAt = LocalDateTime.now();
      this.updatedAt = LocalDateTime.now();
   }

   /**
    * Constructor con identificación.
    *
    * @param documentTypeId ID del tipo de documento
    * @param documentNumber número de documento
    * @param firstName nombre(s)
    * @param lastName apellido(s)
    */
   public Customer(Long documentTypeId, String documentNumber, String firstName, String lastName) {
      this();
      this.documentTypeId = documentTypeId;
      this.documentNumber = documentNumber != null ? documentNumber.toUpperCase().trim() : null;
      this.firstName = firstName;
      this.lastName = lastName;
      this.firstSeenDate = LocalDateTime.now();
      this.lastSeenDate = LocalDateTime.now();
   }

   /**
    * Constructor completo con contacto.
    *
    * @param documentTypeId ID del tipo de documento
    * @param documentNumber número de documento
    * @param firstName nombre(s)
    * @param lastName apellido(s)
    * @param phone teléfono
    * @param email email
    */
   public Customer(Long documentTypeId, String documentNumber, String firstName,
                   String lastName, String phone, String email) {
      this(documentTypeId, documentNumber, firstName, lastName);
      this.phone = phone;
      this.email = email;
   }

   // ========================= MÉTODOS DE NEGOCIO - INFORMACIÓN =========================

   /**
    * Obtiene el nombre completo del cliente.
    *
    * @return nombre completo (firstName + lastName)
    */
   public String getFullName() {
      return firstName + " " + lastName;
   }

   /**
    * Obtiene las iniciales del cliente.
    *
    * @return iniciales (ej: "JP" para Juan Pérez)
    */
   public String getInitials() {
      String first = firstName != null && !firstName.isEmpty()
            ? firstName.substring(0, 1).toUpperCase()
            : "";
      String last = lastName != null && !lastName.isEmpty()
            ? lastName.substring(0, 1).toUpperCase()
            : "";
      return first + last;
   }

   /**
    * Obtiene un identificador legible del cliente.
    *
    * @return descripción formateada (ej: "Juan Pérez (DNI: 43567890)")
    */
   public String getDisplayName() {
      return getFullName() + " (Doc: " + documentNumber + ")";
   }

   // ========================= MÉTODOS DE NEGOCIO - TRACKING =========================

   /**
    * Registra una nueva visita del cliente.
    * Incrementa el contador y actualiza la última vez vista.
    */
   public void recordVisit() {
      if (this.totalVisits == null) {
         this.totalVisits = 0;
      }
      this.totalVisits++;
      this.lastSeenDate = LocalDateTime.now();
      this.updatedAt = LocalDateTime.now();
   }

   /**
    * Actualiza la última vez que el cliente fue visto.
    * Útil para actualizar sin incrementar visitas.
    */
   public void updateLastSeen() {
      this.lastSeenDate = LocalDateTime.now();
      this.updatedAt = LocalDateTime.now();
   }

   /**
    * Verifica si el cliente es recurrente (ha visitado más de una vez).
    *
    * @return true si totalVisits > 1
    */
   public boolean isRecurrent() {
      return totalVisits != null && totalVisits > 1;
   }

   /**
    * Verifica si el cliente es nuevo (primera visita).
    *
    * @return true si totalVisits <= 1
    */
   public boolean isNew() {
      return totalVisits == null || totalVisits <= 1;
   }

   // ========================= MÉTODOS DE NEGOCIO - APP MÓVIL =========================

   /**
    * Vincula el cliente con una cuenta de la app móvil.
    *
    * @param authExternalId ID del usuario en auth-external
    */
   public void linkMobileAccount(Long authExternalId) {
      this.authExternalId = authExternalId;
      this.updatedAt = LocalDateTime.now();
   }

   /**
    * Desvincula la cuenta de la app móvil.
    */
   public void unlinkMobileAccount() {
      this.authExternalId = null;
      this.updatedAt = LocalDateTime.now();
   }

   /**
    * Verifica si el cliente tiene cuenta en la app móvil.
    *
    * @return true si authExternalId no es null
    */
   public boolean hasMobileAccount() {
      return authExternalId != null;
   }

   // OPCIONAL: Agregar método para verificar si puede usar app móvil
   public boolean canUseMobileApp() {
      return hasEmail() && !isDeleted();
   }

   // OPCIONAL: Método para obtener último uso (si necesitas después)
   public int getDaysSinceLastVisit() {
      if (lastSeenDate == null) return 0;
      return (int) Duration.between(lastSeenDate, LocalDateTime.now()).toDays();
   }

   // ========================= MÉTODOS DE NEGOCIO - SOFT DELETE =========================

   /**
    * Marca el cliente como eliminado (soft delete).
    * Uso: clientes registrados por error.
    *
    * @param deletedByUserId ID del usuario que elimina
    */
   public void markAsDeleted(Long deletedByUserId) {
      this.deletedAt = LocalDateTime.now();
      this.deletedBy = deletedByUserId;
      this.updatedAt = LocalDateTime.now();
   }

   /**
    * Restaura un cliente previamente eliminado.
    */
   public void restore() {
      this.deletedAt = null;
      this.deletedBy = null;
      this.updatedAt = LocalDateTime.now();
   }

   /**
    * Verifica si el cliente está eliminado.
    *
    * @return true si deletedAt no es null
    */
   public boolean isDeleted() {
      return deletedAt != null;
   }

   // ========================= MÉTODOS DE NEGOCIO - ACTUALIZACIÓN DE DATOS =========================

   /**
    * Actualiza los datos personales del cliente.
    *
    * @param firstName nuevo nombre
    * @param lastName nuevo apellido
    */
   public void updatePersonalInfo(String firstName, String lastName) {
      this.firstName = firstName;
      this.lastName = lastName;
      this.updatedAt = LocalDateTime.now();
   }

   /**
    * Actualiza la información de contacto.
    *
    * @param phone nuevo teléfono
    * @param email nuevo email
    * @param address nueva dirección
    */
   public void updateContactInfo(String phone, String email, String address) {
      this.phone = phone;
      this.email = email;
      this.address = address;
      this.updatedAt = LocalDateTime.now();
   }

   /**
    * Actualiza solo el teléfono.
    *
    * @param phone nuevo teléfono
    */
   public void updatePhone(String phone) {
      this.phone = phone;
      this.updatedAt = LocalDateTime.now();
   }

   /**
    * Actualiza solo el email.
    *
    * @param email nuevo email
    */
   public void updateEmail(String email) {
      this.email = email;
      this.updatedAt = LocalDateTime.now();
   }

   /**
    * Actualiza el tipo y número de documento.
    * Útil cuando el cliente renueva o cambia de documento.
    *
    * @param documentTypeId nuevo tipo de documento
    * @param documentNumber nuevo número
    */
   public void updateDocument(Long documentTypeId, String documentNumber) {
      this.documentTypeId = documentTypeId;
      this.documentNumber = documentNumber != null ? documentNumber.toUpperCase().trim() : null;
      this.updatedAt = LocalDateTime.now();
   }

   /**
    * Normaliza el número de documento a formato estándar.
    */
   public void normalizeDocumentNumber() {
      if (this.documentNumber != null) {
         this.documentNumber = this.documentNumber.toUpperCase().trim().replaceAll("\\s+", "");
         this.updatedAt = LocalDateTime.now();
      }
   }

   /**
    * Actualiza el usuario que modificó este registro.
    *
    * @param userId ID del usuario que modifica
    */
   public void updateModifiedBy(Long userId) {
      this.updatedBy = userId;
      this.updatedAt = LocalDateTime.now();
   }

   // ========================= MÉTODOS DE VALIDACIÓN =========================

   /**
    * Verifica si el cliente tiene información de contacto completa.
    *
    * @return true si tiene phone Y email
    */
   public boolean hasCompleteContactInfo() {
      return phone != null && !phone.isEmpty() && email != null && !email.isEmpty();
   }

   /**
    * Verifica si tiene teléfono registrado.
    *
    * @return true si phone no es null ni vacío
    */
   public boolean hasPhone() {
      return phone != null && !phone.isEmpty();
   }

   /**
    * Verifica si tiene email registrado.
    *
    * @return true si email no es null ni vacío
    */
   public boolean hasEmail() {
      return email != null && !email.isEmpty();
   }

   /**
    * Verifica si tiene dirección registrada.
    *
    * @return true si address no es null ni vacío
    */
   public boolean hasAddress() {
      return address != null && !address.isEmpty();
   }

   // ========================= GETTERS Y SETTERS =========================

   public Long getId() {
      return id;
   }

   public void setId(Long id) {
      this.id = id;
   }

   public Long getDocumentTypeId() {
      return documentTypeId;
   }

   public void setDocumentTypeId(Long documentTypeId) {
      this.documentTypeId = documentTypeId;
   }

   public String getDocumentNumber() {
      return documentNumber;
   }

   public void setDocumentNumber(String documentNumber) {
      this.documentNumber = documentNumber != null ? documentNumber.toUpperCase().trim() : null;
   }

   public String getFirstName() {
      return firstName;
   }

   public void setFirstName(String firstName) {
      this.firstName = firstName;
   }

   public String getLastName() {
      return lastName;
   }

   public void setLastName(String lastName) {
      this.lastName = lastName;
   }

   public String getPhone() {
      return phone;
   }

   public void setPhone(String phone) {
      this.phone = phone;
   }

   public String getEmail() {
      return email;
   }

   public void setEmail(String email) {
      this.email = email;
   }

   public String getAddress() {
      return address;
   }

   public void setAddress(String address) {
      this.address = address;
   }

   public LocalDateTime getRegistrationDate() {
      return registrationDate;
   }

   public void setRegistrationDate(LocalDateTime registrationDate) {
      this.registrationDate = registrationDate;
   }

   public LocalDateTime getFirstSeenDate() {
      return firstSeenDate;
   }

   public void setFirstSeenDate(LocalDateTime firstSeenDate) {
      this.firstSeenDate = firstSeenDate;
   }

   public LocalDateTime getLastSeenDate() {
      return lastSeenDate;
   }

   public void setLastSeenDate(LocalDateTime lastSeenDate) {
      this.lastSeenDate = lastSeenDate;
   }

   public Integer getTotalVisits() {
      return totalVisits;
   }

   public void setTotalVisits(Integer totalVisits) {
      this.totalVisits = totalVisits;
   }

   public Long getAuthExternalId() {
      return authExternalId;
   }

   public void setAuthExternalId(Long authExternalId) {
      this.authExternalId = authExternalId;
   }

   public LocalDateTime getCreatedAt() {
      return createdAt;
   }

   public void setCreatedAt(LocalDateTime createdAt) {
      this.createdAt = createdAt;
   }

   public Long getCreatedBy() {
      return createdBy;
   }

   public void setCreatedBy(Long createdBy) {
      this.createdBy = createdBy;
   }

   public LocalDateTime getUpdatedAt() {
      return updatedAt;
   }

   public void setUpdatedAt(LocalDateTime updatedAt) {
      this.updatedAt = updatedAt;
   }

   public Long getUpdatedBy() {
      return updatedBy;
   }

   public void setUpdatedBy(Long updatedBy) {
      this.updatedBy = updatedBy;
   }

   public LocalDateTime getDeletedAt() {
      return deletedAt;
   }

   public void setDeletedAt(LocalDateTime deletedAt) {
      this.deletedAt = deletedAt;
   }

   public Long getDeletedBy() {
      return deletedBy;
   }

   public void setDeletedBy(Long deletedBy) {
      this.deletedBy = deletedBy;
   }

   // ========================= EQUALS, HASHCODE Y TOSTRING =========================

   /**
    * Dos clientes son iguales si tienen el mismo ID o mismo documento.
    */
   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Customer customer = (Customer) o;
      return Objects.equals(id, customer.id) ||
            (Objects.equals(documentTypeId, customer.documentTypeId) &&
                  Objects.equals(documentNumber, customer.documentNumber));
   }

   /**
    * HashCode basado en ID y documento únicamente.
    */
   @Override
   public int hashCode() {
      return Objects.hash(id, documentTypeId, documentNumber);
   }

   /**
    * ToString para logging y debugging.
    */
   @Override
   public String toString() {
      return "Customer{" +
            "id=" + id +
            ", documentNumber='" + documentNumber + '\'' +
            ", fullName='" + getFullName() + '\'' +
            ", totalVisits=" + totalVisits +
            ", isRecurrent=" + isRecurrent() +
            ", hasMobileAccount=" + hasMobileAccount() +
            '}';
   }
}