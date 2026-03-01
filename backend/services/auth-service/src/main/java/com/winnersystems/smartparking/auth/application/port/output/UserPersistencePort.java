package com.winnersystems.smartparking.auth.application.port.output;

import com.winnersystems.smartparking.auth.application.dto.query.UserSearchCriteria;
import com.winnersystems.smartparking.auth.domain.model.User;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida para persistencia de usuarios.
 *
 * @author Edwin Yoner Winner Systems - Smart Parking Platform
 * @version 1.0
 */
public interface UserPersistencePort {

   /**
    * Guarda un nuevo usuario o actualiza uno existente.
    *
    * <p>Usado en:</p>
    * <ul>
    *   <li>CreateUserUseCase - Crear nuevo usuario</li>
    *   <li>UpdateUserUseCase - Actualizar usuario existente</li>
    *   <li>DeleteUserUseCase - Marcar como eliminado (soft delete)</li>
    *   <li>RestoreUserUseCase - Restaurar usuario eliminado</li>
    * </ul>
    *
    * @param user usuario a guardar
    * @return usuario guardado con ID generado
    */
   User save(User user);

   /**
    * Busca un usuario por su ID.
    *
    * <p>Usado en:</p>
    * <ul>
    *   <li>GetUserUseCase - Obtener usuario específico</li>
    *   <li>UpdateUserUseCase - Cargar usuario antes de actualizar</li>
    *   <li>DeleteUserUseCase - Cargar usuario antes de eliminar</li>
    *   <li>RestoreUserUseCase - Cargar usuario antes de restaurar</li>
    * </ul>
    *
    * @param id identificador del usuario
    * @return Optional con el usuario si existe, empty si no
    */
   Optional<User> findById(Long id);

   /**
    * Busca un usuario por su email único.
    *
    * <p>Usado en:</p>
    * <ul>
    *   <li>LoginUseCase - Autenticación por email</li>
    *   <li>CreateUserUseCase - Validar que email no existe</li>
    *   <li>ForgotPasswordUseCase - Buscar usuario para reset</li>
    * </ul>
    *
    * @param email email del usuario (único en sistema)
    * @return Optional con el usuario si existe, empty si no
    */
   Optional<User> findByEmail(String email);

   /**
    * Busca usuarios con criterios de búsqueda y paginación.
    *
    * <p>Usado en ListUsersUseCase para búsqueda avanzada con filtros:</p>
    * <ul>
    *   <li>searchTerm - Búsqueda en firstName, lastName, email</li>
    *   <li>status - Filtrar por activo/inactivo</li>
    *   <li>emailVerified - Filtrar por email verificado</li>
    *   <li>includeDeleted - Incluir usuarios eliminados</li>
    *   <li>roleName - Filtrar por rol específico</li>
    *   <li>sortBy, sortDirection - Ordenamiento personalizado</li>
    * </ul>
    *
    * @param criteria criterios de búsqueda (puede ser null para listar todos)
    * @param page número de página (0-based)
    * @param size tamaño de página
    * @return lista de usuarios que coinciden con los criterios
    */
   List<User> findByCriteria(UserSearchCriteria criteria, int page, int size);

   /**
    * Cuenta usuarios que coinciden con los criterios.
    *
    * <p>Usado en ListUsersUseCase para calcular total de páginas
    * en la respuesta paginada.</p>
    *
    * @param criteria criterios de búsqueda (mismos que findByCriteria)
    * @return total de usuarios que coinciden
    */
   long countByCriteria(UserSearchCriteria criteria);

   // ========== NUEVO MÉTODO AGREGADO ==========

   /**
    * Busca usuarios por rol y estado.
    * Usado para obtener operadores activos.
    *
    * <p>Usado en ListOperatorsUseCase para obtener lista de operadores
    * disponibles para asignación a zonas de estacionamiento.</p>
    *
    * @param roleName nombre del rol (ej: "OPERADOR")
    * @param status estado del usuario (true = activo, false = inactivo)
    * @return lista de usuarios que cumplen los criterios
    */
   List<User> findByRoleAndStatus(String roleName, Boolean status);
}