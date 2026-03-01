package com.winnersystems.smartparking.auth.infrastructure.adapter.output.persistence.user.repository;

import com.winnersystems.smartparking.auth.infrastructure.adapter.output.persistence.user.entity.UserEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository de Spring Data JPA para UserEntity.
 *
 * <p>Spring Data JPA provee automáticamente:</p>
 * <ul>
 *   <li>findById(Long) - Buscar por ID</li>
 *   <li>save(Entity) - Guardar o actualizar</li>
 * </ul>
 *
 * <p>JpaSpecificationExecutor provee:</p>
 * <ul>
 *   <li>findAll(Specification, Pageable) - Búsquedas complejas con paginación</li>
 *   <li>count(Specification) - Contar con criterios</li>
 * </ul>
 *
 * @author Edwin Yoner Winner Systems - Smart Parking Platform
 * @version 1.0
 */
@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long>, JpaSpecificationExecutor<UserEntity> {

   /**
    * Busca usuario por email cargando roles y permisos.
    *
    * @param email email del usuario
    * @return Optional con usuario si existe
    */
   @EntityGraph(attributePaths = {"roles", "roles.permissions"})
   Optional<UserEntity> findByEmail(String email);

   /**
    * Busca usuario por ID cargando roles y permisos.
    *
    * @param id ID del usuario
    * @return Optional con usuario si existe
    */
   @EntityGraph(attributePaths = {"roles", "roles.permissions"})
   Optional<UserEntity> findById(Long id);

   /**
    * Verifica si existe un usuario con el email dado.
    *
    * @param email email a verificar
    * @return true si existe, false si no
    */
   boolean existsByEmail(String email);

   // ========== NUEVO MÉTODO AGREGADO ==========

   /**
    * Busca usuarios por nombre de rol y estado.
    * Filtra usuarios activos (deletedAt IS NULL).
    *
    * <p>Usado por ListOperatorsUseCase para obtener operadores disponibles.</p>
    *
    * @param roleName nombre del rol (ej: "OPERADOR")
    * @param status estado del usuario (true = activo, false = inactivo)
    * @return lista de usuarios que cumplen los criterios
    */
   @Query("""
         SELECT DISTINCT u FROM UserEntity u
         JOIN u.roles r
         WHERE r.name = :roleName
         AND u.status = :status
         AND u.deletedAt IS NULL
         ORDER BY u.firstName, u.lastName
         """)
   @EntityGraph(attributePaths = {"roles"})
   List<UserEntity> findByRoleNameAndStatus(
         @Param("roleName") String roleName,
         @Param("status") Boolean status
   );
}