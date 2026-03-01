package com.winnersystems.smartparking.parking.application.port.input.transaction;

import com.winnersystems.smartparking.parking.application.dto.command.UpdateTransactionCommand;
import com.winnersystems.smartparking.parking.application.dto.query.TransactionDto;

/**
 * Puerto de entrada para actualizar notas/observaciones de una transacción.
 *
 * Responsabilidades:
 * - Actualizar campos editables (notes)
 * - Validar que la transacción exista
 *
 * Usado por:
 * - Operadores que necesitan agregar observaciones
 * - Correcciones administrativas
 *
 * @author Edwin Yoner - Winner Systems - Smart Parking Platform
 * @version 1.0
 */
public interface UpdateTransactionUseCase {

   /**
    * Actualiza las notas/observaciones de una transacción.
    *
    * @param transactionId ID de la transacción
    * @param command datos a actualizar
    * @return TransactionDto actualizado
    *
    * @throws IllegalArgumentException si no existe transacción con ese ID
    */
   TransactionDto updateTransaction(Long transactionId, UpdateTransactionCommand command);
}