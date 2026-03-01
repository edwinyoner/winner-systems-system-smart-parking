package com.winnersystems.smartparking.parking.application.dto.command;

/**
 * Comando para registrar la SALIDA de un vehículo del estacionamiento.
 *
 * Este comando actualiza una Transaction de ACTIVE a COMPLETED y calcula
 * el monto a pagar basado en la duración y tarifa aplicable.
 *
 * @author Edwin Yoner - Winner Systems - Smart Parking Platform
 * @version 1.0
 */
public record RecordExitCommand(
      // ========================= IDENTIFICACIÓN =========================
      Long transactionId,              // ID de la transacción activa (opcional si se usa plateNumber)
      String plateNumber,              // Buscar por placa (alternativo)

      // ========================= DOCUMENTO DE SALIDA (SEGURIDAD) =========================
      Long exitDocumentTypeId,         // Tipo de documento salida (NOT NULL)
      String exitDocumentNumber,       // Número documento salida (NOT NULL)
      // DEBE coincidir con entrada (anti-robo)

      // ========================= REGISTRO =========================
      Long operatorId,                 // Operador que registra salida (NOT NULL)
      String exitMethod,               // MANUAL, CAMERA_AI, SENSOR

      // ========================= EVIDENCIA (OPCIONAL) =========================
      String photoUrl,                 // URL foto salida (opcional)
      Double plateConfidence,          // Confianza IA (0.0-1.0)

      // ========================= OBSERVACIONES =========================
      String notes                     // Notas adicionales (opcional)
) {
   // Validaciones en constructor compacto
   public RecordExitCommand {
      // Al menos uno de los dos debe estar presente
      if (transactionId == null && (plateNumber == null || plateNumber.isBlank())) {
         throw new IllegalArgumentException("Debe proporcionar transactionId o plateNumber");
      }

      if (exitDocumentTypeId == null) {
         throw new IllegalArgumentException("exitDocumentTypeId es requerido");
      }
      if (exitDocumentNumber == null || exitDocumentNumber.isBlank()) {
         throw new IllegalArgumentException("exitDocumentNumber es requerido");
      }
      if (operatorId == null) {
         throw new IllegalArgumentException("operatorId es requerido");
      }

      // Normalizar valores
      if (plateNumber != null) {
         plateNumber = plateNumber.toUpperCase().trim();
      }
      if (exitDocumentNumber != null) {
         exitDocumentNumber = exitDocumentNumber.toUpperCase().trim();
      }

      // Validar exitMethod si está presente
      if (exitMethod != null &&
            !exitMethod.equals("MANUAL") &&
            !exitMethod.equals("CAMERA_AI") &&
            !exitMethod.equals("SENSOR")) {
         throw new IllegalArgumentException("exitMethod debe ser MANUAL, CAMERA_AI o SENSOR");
      }

      // Validar plateConfidence si está presente
      if (plateConfidence != null && (plateConfidence < 0.0 || plateConfidence > 1.0)) {
         throw new IllegalArgumentException("plateConfidence debe estar entre 0.0 y 1.0");
      }
   }
}