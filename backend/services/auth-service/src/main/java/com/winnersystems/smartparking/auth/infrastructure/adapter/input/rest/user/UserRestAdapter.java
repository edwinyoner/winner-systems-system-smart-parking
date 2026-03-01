package com.winnersystems.smartparking.auth.infrastructure.adapter.input.rest.user;

import com.winnersystems.smartparking.auth.application.dto.command.CreateUserCommand;
import com.winnersystems.smartparking.auth.application.dto.command.UpdateUserCommand;
import com.winnersystems.smartparking.auth.application.dto.query.OperatorDto;
import com.winnersystems.smartparking.auth.application.dto.query.PagedResponse;
import com.winnersystems.smartparking.auth.application.dto.query.UserDto;
import com.winnersystems.smartparking.auth.application.dto.query.UserSearchCriteria;
import com.winnersystems.smartparking.auth.application.port.input.user.*;
import com.winnersystems.smartparking.auth.infrastructure.adapter.input.rest.auth.dto.response.MessageResponse;
import com.winnersystems.smartparking.auth.infrastructure.adapter.input.rest.user.dto.request.CreateUserRequest;
import com.winnersystems.smartparking.auth.infrastructure.adapter.input.rest.user.dto.request.UpdateUserRequest;
import com.winnersystems.smartparking.auth.infrastructure.adapter.input.rest.user.dto.response.UserResponse;
import com.winnersystems.smartparking.auth.infrastructure.adapter.input.rest.user.mapper.UserRestMapper;
import com.winnersystems.smartparking.auth.infrastructure.config.security.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Adapter para gestión de usuarios.
 *
 * @author Edwin Yoner Winner Systems - Smart Parking Platform
 * @version 1.0
 */
@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserRestAdapter {

   private final CreateUserUseCase createUserUseCase;
   private final UpdateUserUseCase updateUserUseCase;
   private final GetUserUseCase getUserUseCase;
   private final ListUsersUseCase listUsersUseCase;
   private final DeleteUserUseCase deleteUserUseCase;
   private final RestoreUserUseCase restoreUserUseCase;
   private final ResendCredentialsUseCase resendCredentialsUseCase;
   private final ListOperatorsUseCase listOperatorsUseCase;  // ✅ AGREGADO
   private final UserRestMapper mapper;

   // ========== CREATE USER ==========

   /**
    * POST /users - Crear usuario
    */
   @PostMapping
   @PreAuthorize("hasAuthority('users.create')")
   public ResponseEntity<UserResponse> createUser(
         @Valid @RequestBody CreateUserRequest request,
         HttpServletRequest httpRequest,
         Authentication authentication) {

      Long createdBy = getUserIdFromAuthentication(authentication);
      CreateUserCommand command = mapper.toCommand(request, httpRequest, createdBy);
      UserDto userDto = createUserUseCase.execute(command);
      UserResponse response = mapper.toResponse(userDto);

      return ResponseEntity.status(HttpStatus.CREATED).body(response);
   }

   // ========== LIST USERS ==========

   /**
    * GET /users - Listar usuarios con paginación y filtros
    */
   @GetMapping
   @PreAuthorize("hasAuthority('users.read')")
   public ResponseEntity<PagedResponse<UserResponse>> getUsers(
         @RequestParam(required = false) String search,
         @RequestParam(required = false) Long roleId,
         @RequestParam(required = false) Boolean status,
         @RequestParam(required = false) Boolean emailVerified,
         @RequestParam(defaultValue = "0") int page,
         @RequestParam(defaultValue = "10") int size,
         @RequestParam(defaultValue = "id") String sortBy,
         @RequestParam(defaultValue = "desc") String sortDirection
   ) {
      UserSearchCriteria criteria = new UserSearchCriteria(
            search,
            roleId,
            status,
            emailVerified,
            page,
            size,
            sortBy,
            sortDirection
      );

      PagedResponse<UserDto> result = listUsersUseCase.execute(criteria);

      PagedResponse<UserResponse> response = new PagedResponse<>(
            result.content().stream().map(mapper::toResponse).toList(),
            result.number(),
            result.size(),
            result.totalElements(),
            result.totalPages(),
            result.first(),
            result.last(),
            result.hasNext(),
            result.hasPrevious()
      );

      return ResponseEntity.ok(response);
   }

   // ========== LIST ACTIVE USERS ==========

   /**
    * GET /users/active - Solo usuarios activos
    */
   @GetMapping("/active")
   @PreAuthorize("hasAuthority('users.read')")
   public ResponseEntity<PagedResponse<UserResponse>> listActiveUsers(
         @RequestParam(defaultValue = "0") int page,
         @RequestParam(defaultValue = "10") int size) {

      UserSearchCriteria criteria = new UserSearchCriteria(
            null, null, true, null,
            page, size, "id", "desc"
      );

      PagedResponse<UserDto> result = listUsersUseCase.execute(criteria);

      PagedResponse<UserResponse> response = new PagedResponse<>(
            result.content().stream().map(mapper::toResponse).toList(),
            result.number(),
            result.size(),
            result.totalElements(),
            result.totalPages(),
            result.first(),
            result.last(),
            result.hasNext(),
            result.hasPrevious()
      );

      return ResponseEntity.ok(response);
   }

   // ========== LIST OPERATORS (NUEVO ENDPOINT) ==========

   /**
    * GET /users/operators - Lista todos los operadores activos
    *
    * Este endpoint es usado por parking-service para obtener operadores disponibles
    * para asignar a zonas de estacionamiento.
    *
    * @return lista de operadores activos
    */
   @GetMapping("/operators")
   @PreAuthorize("hasAnyRole('ADMIN', 'AUTORIDAD')")
   public ResponseEntity<List<OperatorDto>> listOperators() {
      log.debug("📋 GET /users/operators - Listando operadores activos");

      List<OperatorDto> operators = listOperatorsUseCase.listActiveOperators();

      log.debug("✅ {} operadores encontrados", operators.size());

      return ResponseEntity.ok(operators);
   }

   // ========== GET USER BY ID ==========

   /**
    * GET /users/{id} - Obtener usuario por ID
    */
   @GetMapping("/{id}")
   @PreAuthorize("hasAuthority('users.read') or #id == authentication.principal.userId")
   public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
      UserDto userDto = getUserUseCase.execute(id);
      UserResponse response = mapper.toResponse(userDto);
      return ResponseEntity.ok(response);
   }

   // ========== UPDATE USER ==========

   /**
    * PUT /users/{id} - Actualizar usuario
    */
   @PutMapping("/{id}")
   @PreAuthorize("hasAuthority('users.update') or #id == authentication.principal.userId")
   public ResponseEntity<UserResponse> updateUser(
         @PathVariable Long id,
         @Valid @RequestBody UpdateUserRequest request,
         Authentication authentication) {

      Long updatedBy = getUserIdFromAuthentication(authentication);
      UpdateUserCommand command = mapper.toCommand(id, request, updatedBy);
      UserDto userDto = updateUserUseCase.execute(command);
      UserResponse response = mapper.toResponse(userDto);

      return ResponseEntity.ok(response);
   }

   // ========== DELETE USER ==========

   /**
    * DELETE /users/{id} - Eliminar usuario (soft delete)
    */
   @DeleteMapping("/{id}")
   @PreAuthorize("hasAuthority('users.delete')")
   public ResponseEntity<MessageResponse> deleteUser(
         @PathVariable Long id,
         Authentication authentication) {

      Long deletedBy = getUserIdFromAuthentication(authentication);
      deleteUserUseCase.executeDelete(id, deletedBy);

      return ResponseEntity.ok(
            new MessageResponse("Usuario eliminado exitosamente")
      );
   }

   // ========== RESTORE USER ==========

   /**
    * POST /users/{id}/restore - Restaurar usuario
    */
   @PostMapping("/{id}/restore")
   @PreAuthorize("hasAuthority('users.restore')")
   public ResponseEntity<UserResponse> restoreUser(
         @PathVariable Long id,
         Authentication authentication) {

      Long restoredBy = getUserIdFromAuthentication(authentication);
      UserDto userDto = restoreUserUseCase.executeRestore(id, restoredBy);
      UserResponse response = mapper.toResponse(userDto);

      return ResponseEntity.ok(response);
   }

   // ========== RESEND CREDENTIALS ==========

   /**
    * POST /users/{id}/resend-credentials - Reenviar credenciales por email
    */
   @PostMapping("/{id}/resend-credentials")
   @PreAuthorize("hasAuthority('users.create')")
   public ResponseEntity<MessageResponse> resendCredentials(
         @PathVariable Long id,
         @RequestParam String password) {

      resendCredentialsUseCase.execute(id, password);

      return ResponseEntity.ok(
            new MessageResponse("Credenciales enviadas exitosamente por email")
      );
   }

   // ========== MÉTODO AUXILIAR ==========

   private Long getUserIdFromAuthentication(Authentication authentication) {
      Object principal = authentication.getPrincipal();

      if (principal instanceof CustomUserDetails userDetails) {
         return userDetails.getUserId();
      }

      throw new IllegalStateException("No se pudo obtener el ID del usuario autenticado");
   }
}