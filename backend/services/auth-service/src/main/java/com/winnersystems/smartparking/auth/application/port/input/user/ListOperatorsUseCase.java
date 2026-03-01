package com.winnersystems.smartparking.auth.application.port.input.user;

import com.winnersystems.smartparking.auth.application.dto.query.OperatorDto;

import java.util.List;

/**
 * Caso de uso para listar operadores disponibles.
 * Un operador es un usuario con rol OPERADOR y estado activo.
 *
 * Este caso de uso es usado por parking-service para obtener la lista
 * de operadores que pueden ser asignados a zonas de estacionamiento.
 *
 * @author Edwin Yoner - Winner Systems - Smart Parking Platform
 * @version 1.0
 */
public interface ListOperatorsUseCase {

   /**
    * Lista todos los operadores activos del sistema.
    * Filtra usuarios por rol OPERADOR y status activo.
    *
    * @return lista de operadores disponibles para asignación
    */
   List<OperatorDto> listActiveOperators();
}