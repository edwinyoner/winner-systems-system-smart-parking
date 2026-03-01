package com.winnersystems.smartparking.parking.application.dto.command;

/**
 * Comando para registrar la ENTRADA de un vehículo al estacionamiento.
 *
 * Este comando captura toda la información necesaria para crear una Transaction
 * en estado ACTIVE.
 *
 * @author Edwin Yoner - Winner Systems - Smart Parking Platform
 * @version 1.0
 */
public record RecordEntryCommand(
      // ========================= IDENTIFICACIÓN =========================
      String plateNumber,              // Placa del vehículo (NOT NULL)
      String vehicleColor,             // Color del vehículo (opcional)
      String vehicleBrand,             // Marca del vehículo (opcional)

      // ========================= UBICACIÓN =========================
      Long parkingId,                  // ID del parking (NOT NULL)
      Long zoneId,                     // ID de la zona (NOT NULL)
      Long spaceId,                    // ID del espacio específico (NOT NULL)

      // ========================= CONDUCTOR =========================
      Long documentTypeId,             // Tipo de documento (NOT NULL)
      String documentNumber,           // Número de documento (NOT NULL)
      String customerFirstName,        // Nombre del conductor (opcional)
      String customerLastName,         // Apellido del conductor (opcional)
      String customerPhone,            // Teléfono (opcional)
      String customerEmail,            // Email (opcional)

      // ========================= REGISTRO =========================
      Long operatorId,                 // Operador que registra (NOT NULL)
      String entryMethod,              // MANUAL, CAMERA_AI, SENSOR

      // ========================= EVIDENCIA (OPCIONAL) =========================
      String photoUrl,                 // URL foto entrada (opcional)
      Double plateConfidence,          // Confianza IA (0.0-1.0)

      // ========================= OBSERVACIONES =========================
      String notes                     // Notas adicionales (opcional)
) {
   // Validaciones en constructor compacto
   public RecordEntryCommand {
      if (plateNumber == null || plateNumber.isBlank()) {
         throw new IllegalArgumentException("plateNumber es requerido");
      }
      if (parkingId == null) {
         throw new IllegalArgumentException("parkingId es requerido");
      }
      if (zoneId == null) {
         throw new IllegalArgumentException("zoneId es requerido");
      }
      if (spaceId == null) {
         throw new IllegalArgumentException("spaceId es requerido");
      }
      if (documentTypeId == null) {
         throw new IllegalArgumentException("documentTypeId es requerido");
      }
      if (documentNumber == null || documentNumber.isBlank()) {
         throw new IllegalArgumentException("documentNumber es requerido");
      }
      if (operatorId == null) {
         throw new IllegalArgumentException("operatorId es requerido");
      }

      // Normalizar placa a mayúsculas
      plateNumber = plateNumber != null ? plateNumber.toUpperCase().trim() : null;
      documentNumber = documentNumber != null ? documentNumber.toUpperCase().trim() : null;

      // Validar entryMethod si está presente
      if (entryMethod != null &&
            !entryMethod.equals("MANUAL") &&
            !entryMethod.equals("CAMERA_AI") &&
            !entryMethod.equals("SENSOR")) {
         throw new IllegalArgumentException("entryMethod debe ser MANUAL, CAMERA_AI o SENSOR");
      }

      // Validar plateConfidence si está presente
      if (plateConfidence != null && (plateConfidence < 0.0 || plateConfidence > 1.0)) {
         throw new IllegalArgumentException("plateConfidence debe estar entre 0.0 y 1.0");
      }
   }
}