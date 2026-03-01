package com.winnersystems.smartparking.parking.application.port.input.transaction;

import com.winnersystems.smartparking.parking.application.dto.command.CancelTransactionCommand;
import com.winnersystems.smartparking.parking.application.dto.query.TransactionDto;

/**
 * Puerto de entrada para cancelar una transacción.
 *
 * Responsabilidades:
 * - Validar que la transacción esté en estado ACTIVE
 * - Marcar transacción como CANCELLED
 * - Liberar el espacio ocupado
 * - Registrar razón de cancelación
 *
 * Reglas de negocio:
 * - Solo se pueden cancelar transacciones ACTIVE
 * - Debe proporcionarse una razón
 * - El espacio se libera automáticamente
 *
 * Usado por:
 * - Errores operativos
 * - Situaciones excepcionales
 *
 * @author Edwin Yoner - Winner Systems - Smart Parking Platform
 * @version 1.0
 */
public interface CancelTransactionUseCase {

   /**
    * Cancela una transacción activa.
    *
    * @param transactionId ID de la transacción
    * @param command razón de cancelación
    * @return TransactionDto con estado actualizado
    *
    * @throws com.winnersystems.smartparking.parking.domain.exception.InvalidTransactionStateException
    *         si la transacción no está en estado ACTIVE
    */
   TransactionDto cancelTransaction(Long transactionId, CancelTransactionCommand command);
}