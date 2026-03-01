package com.winnersystems.smartparking.parking.application.port.input.transaction;

import com.winnersystems.smartparking.parking.application.dto.query.TransactionDetailDto;

/**
 * Puerto de entrada para obtener los DETALLES COMPLETOS de una transacción.
 *
 * Responsabilidades:
 * - Buscar transacción por ID
 * - Cargar todos los datos relacionados (vehículo, cliente, zona, espacio, pago, etc.)
 * - Construir DTO con información completa
 *
 * Usado por:
 * - Vista de detalle de transacción en dashboard
 * - Generación de comprobantes
 * - Consultas de auditoría
 *
 * @author Edwin Yoner - Winner Systems - Smart Parking Platform
 * @version 1.0
 */
public interface GetTransactionUseCase {

   /**
    * Obtiene los detalles completos de una transacción por su ID.
    *
    * @param transactionId ID de la transacción
    * @return TransactionDetailDto con todos los detalles
    *
    * @throws IllegalArgumentException si no existe transacción con ese ID
    */
   TransactionDetailDto getTransactionById(Long transactionId);

   /**
    * Obtiene la transacción activa de un vehículo por su placa.
    *
    * Útil para:
    * - Buscar vehículo que está dentro para registrar su salida
    * - Consultar estado actual de un vehículo
    *
    * @param plateNumber placa del vehículo
    * @return TransactionDetailDto de la transacción activa, o null si no tiene
    */
   TransactionDetailDto getActiveTransactionByPlate(String plateNumber);
}