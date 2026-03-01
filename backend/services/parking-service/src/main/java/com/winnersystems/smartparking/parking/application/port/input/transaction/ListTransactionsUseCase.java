package com.winnersystems.smartparking.parking.application.port.input.transaction;

import com.winnersystems.smartparking.parking.application.dto.query.PagedResponse;
import com.winnersystems.smartparking.parking.application.dto.query.TransactionDto;

import java.time.LocalDateTime;

/**
 * Puerto de entrada para listar TODAS las transacciones (historial completo).
 *
 * Responsabilidades:
 * - Listar transacciones con filtros múltiples
 * - Ordenar por diferentes criterios
 * - Paginar resultados
 *
 * Usado por:
 * - Historial de transacciones
 * - Reportes financieros
 * - Auditoría
 * - Búsquedas avanzadas
 *
 * @author Edwin Yoner - Winner Systems - Smart Parking Platform
 * @version 1.0
 */
public interface ListTransactionsUseCase {

   /**
    * Lista todas las transacciones con paginación.
    *
    * @param pageNumber número de página (0-indexed)
    * @param pageSize tamaño de página
    * @param sortBy campo para ordenar (createdAt, totalAmount, etc.)
    * @param sortDirection dirección (ASC, DESC)
    * @return PagedResponse con lista de transacciones
    */
   PagedResponse<TransactionDto> listAllTransactions(int pageNumber, int pageSize, String sortBy, String sortDirection);

   /**
    * Lista transacciones por rango de fechas.
    *
    * @param startDate fecha inicio
    * @param endDate fecha fin
    * @param pageNumber número de página
    * @param pageSize tamaño de página
    * @return PagedResponse con transacciones en el rango
    */
   PagedResponse<TransactionDto> listTransactionsByDateRange(LocalDateTime startDate, LocalDateTime endDate,
                                                             int pageNumber, int pageSize);

   /**
    * Lista transacciones por estado.
    *
    * @param status estado (ACTIVE, COMPLETED, CANCELLED)
    * @param pageNumber número de página
    * @param pageSize tamaño de página
    * @return PagedResponse con transacciones del estado especificado
    */
   PagedResponse<TransactionDto> listTransactionsByStatus(String status, int pageNumber, int pageSize);

   /**
    * Lista transacciones por estado de pago.
    *
    * @param paymentStatus estado de pago (PENDING, PAID, OVERDUE)
    * @param pageNumber número de página
    * @param pageSize tamaño de página
    * @return PagedResponse con transacciones del estado de pago especificado
    */
   PagedResponse<TransactionDto> listTransactionsByPaymentStatus(String paymentStatus, int pageNumber, int pageSize);

   /**
    * Lista transacciones de una zona específica.
    *
    * @param zoneId ID de la zona
    * @param pageNumber número de página
    * @param pageSize tamaño de página
    * @return PagedResponse con transacciones de la zona
    */
   PagedResponse<TransactionDto> listTransactionsByZone(Long zoneId, int pageNumber, int pageSize);

   /**
    * Busca transacciones por placa de vehículo.
    *
    * @param plateNumber placa (búsqueda exacta o parcial)
    * @param pageNumber número de página
    * @param pageSize tamaño de página
    * @return PagedResponse con transacciones que coinciden
    */
   PagedResponse<TransactionDto> searchTransactionsByPlate(String plateNumber, int pageNumber, int pageSize);
}