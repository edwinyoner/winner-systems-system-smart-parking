package com.winnersystems.smartparking.auth.application.service.user;

import com.winnersystems.smartparking.auth.application.dto.command.CreateUserCommand;
import com.winnersystems.smartparking.auth.application.dto.command.UpdateUserCommand;
import com.winnersystems.smartparking.auth.application.dto.query.OperatorDto;
import com.winnersystems.smartparking.auth.application.dto.query.PagedResponse;
import com.winnersystems.smartparking.auth.application.dto.query.UserDto;
import com.winnersystems.smartparking.auth.application.dto.query.UserSearchCriteria;
import com.winnersystems.smartparking.auth.application.port.input.user.*;
import com.winnersystems.smartparking.auth.application.port.output.*;
import com.winnersystems.smartparking.auth.domain.exception.EmailAlreadyExistsException;
import com.winnersystems.smartparking.auth.domain.exception.UserNotFoundException;
import com.winnersystems.smartparking.auth.domain.model.Role;
import com.winnersystems.smartparking.auth.domain.model.User;
import com.winnersystems.smartparking.auth.domain.model.VerificationToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Servicio de aplicación para gestión de usuarios.
 * Implementa los 8 casos de uso de user/.
 *
 * @author Edwin Yoner Winner Systems - Smart Parking Platform
 * @version 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserService implements
      CreateUserUseCase,
      UpdateUserUseCase,
      GetUserUseCase,
      ListUsersUseCase,
      DeleteUserUseCase,
      RestoreUserUseCase,
      ResendCredentialsUseCase,
      ListOperatorsUseCase {  // ✅ AGREGADO

   private final UserPersistencePort userPersistencePort;
   private final RolePersistencePort rolePersistencePort;
   private final PasswordEncoderPort passwordEncoderPort;
   private final EmailPort emailPort;
   private final TokenPersistencePort tokenPersistencePort;

   // ========== CREATE USER ==========

   @Override
   public UserDto execute(CreateUserCommand command) {
      // 1. Validar email único
      if (userPersistencePort.findByEmail(command.email()).isPresent()) {
         throw new EmailAlreadyExistsException(command.email());
      }

      // 2. Validar que roles existen
      List<Role> roles = rolePersistencePort.findAllByIds(command.roleIds());
      if (roles.size() != command.roleIds().size()) {
         throw new IllegalArgumentException("Uno o más roles no existen");
      }

      // 3. GUARDAR contraseña en texto plano ANTES de hashear
      String plainPassword = command.password();

      // 4. Hashear password
      String hashedPassword = passwordEncoderPort.encode(plainPassword);

      // 5. Crear usuario
      User user = new User(
            command.firstName(),
            command.lastName(),
            command.email(),
            hashedPassword
      );

      // 6. Asignar campos faltantes
      user.setPhoneNumber(command.phoneNumber());
      user.setCreatedBy(command.createdBy());
      user.setCreatedAt(LocalDateTime.now());

      // 7. Asignar roles
      user.setRoles(new HashSet<>(roles));

      // 8. Guardar usuario
      User savedUser = userPersistencePort.save(user);

      // 9. Generar y GUARDAR token de verificación
      try {
         String tokenValue = java.util.UUID.randomUUID().toString();

         VerificationToken verificationToken = new VerificationToken(
               tokenValue,
               savedUser.getId(),
               command.createdBy()
         );

         verificationToken.setIpAddress(command.ipAddress());

         tokenPersistencePort.saveVerificationToken(verificationToken);

         System.out.println("✅ Token guardado: " + tokenValue);
         System.out.println("✅ Usuario creado (email NO enviado automáticamente)");

         // ✅ Código comentado para envío de email
         /*
         String verificationLink = "http://localhost:4200/verify-email?token=" + tokenValue;
         Set<String> roleNames = savedUser.getRoles().stream()
               .map(Role::getName)
               .collect(Collectors.toSet());

         emailPort.sendWelcomeEmailWithCredentials(
               savedUser.getEmail(),
               savedUser.getFullName(),
               savedUser.getEmail(),
               plainPassword,
               roleNames,
               verificationLink,
               24
         );
         */

         System.out.println("✅ Token guardado: " + tokenValue);
         System.out.println("✅ Email enviado a: " + savedUser.getEmail());

      } catch (Exception e) {
         System.err.println("❌ Error: " + e.getMessage());
         e.printStackTrace();
      }

      return mapToDto(savedUser);
   }

   // ========== UPDATE USER ==========

   @Override
   public UserDto execute(UpdateUserCommand command) {
      // 1. Buscar usuario
      User user = userPersistencePort.findById(command.userId())
            .orElseThrow(() -> new UserNotFoundException(command.userId()));

      // 2. Actualizar campos si no son null
      if (command.firstName() != null) {
         user.setFirstName(command.firstName());
      }

      if (command.lastName() != null) {
         user.setLastName(command.lastName());
      }

      if (command.phoneNumber() != null) {
         user.setPhoneNumber(command.phoneNumber());
      }

      if (command.profilePicture() != null) {
         user.setProfilePicture(command.profilePicture());
      }

      // 3. Actualizar roles si se especifican
      if (command.roleIds() != null) {
         List<Role> roles = rolePersistencePort.findAllByIds(command.roleIds());
         if (roles.size() != command.roleIds().size()) {
            throw new IllegalArgumentException("Uno o más roles no existen");
         }
         user.setRoles(new HashSet<>(roles));
      }

      // 4. Registrar auditoría
      user.setUpdatedBy(command.updatedBy());
      user.setUpdatedAt(LocalDateTime.now());

      // 5. Guardar
      User updated = userPersistencePort.save(user);

      return mapToDto(updated);
   }

   // ========== GET USER ==========

   @Override
   public UserDto execute(Long userId) {
      User user = userPersistencePort.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));

      return mapToDto(user);
   }

   // ========== LIST USERS ==========

   @Override
   public PagedResponse<UserDto> execute(UserSearchCriteria criteria) {
      // Usar page y size del criteria
      int page = criteria.page();
      int size = criteria.size();

      // 1. Buscar usuarios con criterios
      List<User> users = userPersistencePort.findByCriteria(criteria, page, size);

      // 2. Contar total
      long total = userPersistencePort.countByCriteria(criteria);

      // 3. Mapear a DTOs
      List<UserDto> userDtos = users.stream()
            .map(this::mapToDto)
            .toList();

      // 4. Crear respuesta paginada
      return PagedResponse.of(userDtos, page, size, total);
   }

   // ========== DELETE USER (soft delete) ==========

   @Override
   public void executeDelete(Long userId, Long deletedBy) {
      // 1. Buscar usuario
      User user = userPersistencePort.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));

      // 2. Marcar como eliminado (soft delete)
      user.setDeletedBy(deletedBy);
      user.setDeletedAt(LocalDateTime.now());

      // 3. Guardar
      userPersistencePort.save(user);

      // 4. Enviar email de notificación (opcional)
      try {
         emailPort.sendAccountDeactivatedEmail(
               user.getEmail(),
               user.getFullName(),
               "Eliminación solicitada"
         );
      } catch (Exception e) {
         // Log error pero no fallar
      }
   }

   // ========== RESTORE USER ==========

   @Override
   public UserDto executeRestore(Long userId, Long restoredBy) {
      // 1. Buscar usuario
      User user = userPersistencePort.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));

      // 2. Validar que esté eliminado
      if (user.getDeletedAt() == null) {
         throw new IllegalStateException("Usuario no está eliminado");
      }

      // 3. Restaurar
      user.setDeletedAt(null);
      user.setDeletedBy(null);

      // 4. Registrar auditoría
      user.setUpdatedBy(restoredBy);
      user.setUpdatedAt(LocalDateTime.now());

      // 5. Guardar
      User restored = userPersistencePort.save(user);

      // 6. Enviar email de notificación (opcional)
      try {
         emailPort.sendAccountActivatedEmail(user.getEmail(), user.getFullName());
      } catch (Exception e) {
         // Log error pero no fallar
      }

      return mapToDto(restored);
   }

   // ========== RESEND CREDENTIALS ==========

   @Override
   public void execute(Long userId, String plainPassword) {
      // 1. Buscar usuario
      User user = userPersistencePort.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));

      // 2. Buscar token de verificación
      Optional<VerificationToken> tokenOpt = tokenPersistencePort.findVerificationTokenByUserId(userId);

      if (tokenOpt.isEmpty()) {
         throw new IllegalStateException("No existe token de verificación para este usuario");
      }

      VerificationToken token = tokenOpt.get();

      // 3. Construir link de verificación
      String verificationLink = "http://localhost:4200/verify-email?token=" + token.getToken();

      // 4. Obtener nombres de roles
      Set<String> roleNames = user.getRoles().stream()
            .map(Role::getName)
            .collect(Collectors.toSet());

      // 5. Enviar email con credenciales
      emailPort.sendWelcomeEmailWithCredentials(
            user.getEmail(),
            user.getFullName(),
            user.getEmail(),
            plainPassword,
            roleNames,
            verificationLink,
            24
      );

      System.out.println("✅ Credenciales reenviadas a: " + user.getEmail());
   }

   // ========== LIST OPERATORS (NUEVO) ==========

   /**
    * Lista todos los operadores activos del sistema.
    * Un operador es un usuario con rol OPERADOR y status activo.
    *
    * @return lista de operadores disponibles
    */
   @Override
   public List<OperatorDto> listActiveOperators() {
      log.debug("📋 Listando operadores activos");

      // Buscar usuarios con rol OPERADOR y status activo
      List<User> operators = userPersistencePort.findByRoleAndStatus("OPERADOR", true);

      List<OperatorDto> operatorDtos = operators.stream()
            .map(user -> new OperatorDto(
                  user.getId(),
                  user.getFirstName(),
                  user.getLastName(),
                  user.getEmail(),
                  user.getPhoneNumber(),
                  user.getStatus()
            ))
            .toList();

      log.debug("✅ {} operadores activos encontrados", operatorDtos.size());

      return operatorDtos;
   }

   // ========== HELPER METHODS ==========

   private UserDto mapToDto(User user) {
      return new UserDto(
            user.getId(),
            user.getFirstName(),
            user.getLastName(),
            user.getEmail(),
            user.getPhoneNumber(),
            user.getProfilePicture(),
            user.getStatus(),
            user.isEmailVerified(),
            getRoleNames(user),
            user.getCreatedAt(),
            user.getUpdatedAt()
      );
   }

   private Set<String> getRoleNames(User user) {
      return user.getRoles().stream()
            .map(Role::getName)
            .collect(Collectors.toSet());
   }

   private Set<String> getPermissionNames(User user) {
      return user.getRoles().stream()
            .flatMap(role -> role.getPermissions().stream())
            .map(permission -> permission.getName())
            .collect(Collectors.toSet());
   }
}