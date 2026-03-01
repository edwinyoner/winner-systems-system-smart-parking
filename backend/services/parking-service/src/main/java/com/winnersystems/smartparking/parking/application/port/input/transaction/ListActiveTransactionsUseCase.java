package com.winnersystems.smartparking.parking.application.port.input.transaction;

import com.winnersystems.smartparking.parking.application.dto.query.ActiveTransactionDto;
import com.winnersystems.smartparking.parking.application.dto.query.PagedResponse;

/**
 * Puerto de entrada para listar transacciones ACTIVAS (vehículos actualmente dentro).
 *
 * Responsabilidades:
 * - Listar vehículos actualmente estacionados
 * - Calcular tiempo transcurrido en tiempo real
 * - Calcular monto acumulado actual
 * - Detectar vehículos que exceden tiempo recomendado
 * - Filtrar por zona, espacio, placa, etc.
 *
 * Usado por:
 * - Dashboard de operador (monitoreo en tiempo real)
 * - Pantallas de supervisión
 * - Reportes de ocupación actual
 *
 * @author Edwin Yoner - Winner Systems - Smart Parking Platform
 * @version 1.0
 */
public interface ListActiveTransactionsUseCase {

   /**
    * Lista todas las transacciones activas con paginación.
    *
    * @param pageNumber número de página (0-indexed)
    * @param pageSize tamaño de página
    * @return PagedResponse con lista de transacciones activas
    */
   PagedResponse<ActiveTransactionDto> listAllActiveTransactions(int pageNumber, int pageSize);

   /**
    * Lista transacciones activas de una zona específica.
    *
    * @param zoneId ID de la zona
    * @param pageNumber número de página
    * @param pageSize tamaño de página
    * @return PagedResponse con transacciones activas de la zona
    */
   PagedResponse<ActiveTransactionDto> listActiveTransactionsByZone(Long zoneId, int pageNumber, int pageSize);

   /**
    * Busca transacciones activas por placa (búsqueda parcial).
    *
    * @param plateNumber placa completa o parcial
    * @param pageNumber número de página
    * @param pageSize tamaño de página
    * @return PagedResponse con transacciones que coinciden
    */
   PagedResponse<ActiveTransactionDto> searchActiveTransactionsByPlate(String plateNumber, int pageNumber, int pageSize);

   /**
    * Lista transacciones que exceden el tiempo recomendado (alertas).
    *
    * Útil para detectar vehículos que llevan mucho tiempo estacionados.
    *
    * @param pageNumber número de página
    * @param pageSize tamaño de página
    * @return PagedResponse con transacciones que requieren atención
    */
   PagedResponse<ActiveTransactionDto> listOverdueTransactions(int pageNumber, int pageSize);
}