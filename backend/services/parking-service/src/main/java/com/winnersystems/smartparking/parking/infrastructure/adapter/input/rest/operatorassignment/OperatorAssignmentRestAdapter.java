package com.winnersystems.smartparking.parking.infrastructure.adapter.input.rest.operatorassignment;

import com.winnersystems.smartparking.parking.application.dto.command.AssignOperatorsCommand;
import com.winnersystems.smartparking.parking.application.dto.command.UpdateOperatorAssignmentCommand;
import com.winnersystems.smartparking.parking.application.dto.query.OperatorAssignmentDetailDto;
import com.winnersystems.smartparking.parking.application.dto.query.OperatorAssignmentDto;
import com.winnersystems.smartparking.parking.application.port.input.operatorassignment.*;
import com.winnersystems.smartparking.parking.infrastructure.adapter.input.rest.operatorassignment.dto.request.AssignOperatorsRequest;
import com.winnersystems.smartparking.parking.infrastructure.adapter.input.rest.operatorassignment.dto.request.UpdateOperatorAssignmentRequest;
import com.winnersystems.smartparking.parking.infrastructure.adapter.input.rest.operatorassignment.dto.response.OperatorAssignmentDetailResponse;
import com.winnersystems.smartparking.parking.infrastructure.adapter.input.rest.operatorassignment.dto.response.OperatorAssignmentResponse;
import com.winnersystems.smartparking.parking.infrastructure.adapter.input.rest.operatorassignment.mapper.OperatorAssignmentRestMapper;
import com.winnersystems.smartparking.parking.infrastructure.adapter.output.client.AuthServiceClient;
import com.winnersystems.smartparking.parking.infrastructure.config.security.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * REST Adapter para gestión de asignaciones de operadores.
 *
 * Este adapter es responsable de:
 * 1. Recibir requests HTTP
 * 2. Llamar a los Use Cases (Application Layer)
 * 3. Enriquecer los DTOs con información de auth-service (AuthServiceClient)
 * 4. Retornar responses HTTP
 *
 * @author Edwin Yoner - Winner Systems - Smart Parking Platform
 * @version 1.0
 */
@Slf4j
@RestController
@RequestMapping
@RequiredArgsConstructor
public class OperatorAssignmentRestAdapter {

   private final AssignOperatorsUseCase assignOperatorsUseCase;
   private final ListOperatorAssignmentsUseCase listOperatorAssignmentsUseCase;
   private final GetOperatorAssignmentUseCase getOperatorAssignmentUseCase;
   private final EndOperatorAssignmentUseCase endOperatorAssignmentUseCase;
   private final UpdateOperatorAssignmentUseCase updateOperatorAssignmentUseCase;
   private final OperatorAssignmentRestMapper mapper;
   private final AuthServiceClient authServiceClient;
   private final JwtService jwtService;

   // ========================= ASIGNAR OPERADORES =========================

   /**
    * POST /zones/{zoneId}/operators - Asignar operadores a una zona.
    */
   @PostMapping("/zones/{zoneId}/operators")
   @PreAuthorize("hasAnyRole('ADMIN', 'AUTORIDAD')")
   public ResponseEntity<List<OperatorAssignmentResponse>> assignOperators(
         @PathVariable Long zoneId,
         @Valid @RequestBody AssignOperatorsRequest request,
         HttpServletRequest httpRequest) {

      log.debug("POST /zones/{}/operators - Asignando {} operadores",
            zoneId, request.getAssignments().size());

      // Validar que zoneId del path coincide con el del body
      if (!zoneId.equals(request.getZoneId())) {
         throw new IllegalArgumentException(
               "El zoneId del path no coincide con el del body");
      }

      // Extraer userId del JWT
      Long userId = getUserIdFromToken(httpRequest);

      // 1. VALIDAR que los operadores existen en auth-service ANTES de crear asignaciones
      List<AuthServiceClient.OperatorDto> availableOperators = authServiceClient.getActiveOperators();
      Set<Long> availableOperatorIds = availableOperators.stream()
            .map(AuthServiceClient.OperatorDto::id)
            .collect(Collectors.toSet());

      // Validar cada operador solicitado
      for (AssignOperatorsRequest.OperatorAssignmentData data : request.getAssignments()) {
         if (!availableOperatorIds.contains(data.getOperatorId())) {
            throw new IllegalArgumentException(
                  "Operador no encontrado o inactivo con ID: " + data.getOperatorId());
         }
      }

      // 2. Crear Map para lookup rápido de nombres
      Map<Long, String> operatorNames = availableOperators.stream()
            .collect(Collectors.toMap(
                  AuthServiceClient.OperatorDto::id,
                  AuthServiceClient.OperatorDto::getFullName
            ));

      // 3. Convertir request a command
      AssignOperatorsCommand command = mapper.toCommand(request, userId);

      // 4. Ejecutar caso de uso
      List<OperatorAssignmentDto> assignments = assignOperatorsUseCase.assignOperators(command);

      // 5. Enriquecer DTOs con nombres de operadores
      List<OperatorAssignmentResponse> response = assignments.stream()
            .map(dto -> {
               OperatorAssignmentResponse resp = mapper.toResponse(dto);
               // Enriquecer con nombre del operador
               return new OperatorAssignmentResponse(
                     resp.id(),
                     resp.operatorId(),
                     operatorNames.get(resp.operatorId()),
                     resp.zoneId(),
                     resp.zoneName(),
                     resp.shiftId(),
                     resp.shiftName(),
                     resp.startDate(),
                     resp.endDate(),
                     resp.status(),
                     resp.createdAt()
               );
            })
            .collect(Collectors.toList());

      log.debug("{} asignaciones creadas", response.size());

      return ResponseEntity.status(HttpStatus.CREATED).body(response);
   }

   // ========================= LISTAR ASIGNACIONES =========================

   /**
    * GET /zones/{zoneId}/operators - Lista todas las asignaciones de una zona.
    */
   @GetMapping("/zones/{zoneId}/operators")
   @PreAuthorize("hasAnyRole('ADMIN', 'AUTORIDAD', 'OPERADOR')")
   public ResponseEntity<List<OperatorAssignmentResponse>> listByZone(@PathVariable Long zoneId) {
      log.debug("GET /zones/{}/operators - Listando asignaciones", zoneId);

      List<OperatorAssignmentDto> assignments = listOperatorAssignmentsUseCase.listByZone(zoneId);

      List<OperatorAssignmentResponse> response = enrichWithOperatorNames(assignments);

      return ResponseEntity.ok(response);
   }

   /**
    * GET /zones/{zoneId}/operators/active - Lista asignaciones activas de una zona.
    */
   @GetMapping("/zones/{zoneId}/operators/active")
   @PreAuthorize("hasAnyRole('ADMIN', 'AUTORIDAD', 'OPERADOR')")
   public ResponseEntity<List<OperatorAssignmentResponse>> listActiveByZone(@PathVariable Long zoneId) {
      log.debug("GET /zones/{}/operators/active - Listando asignaciones activas", zoneId);

      List<OperatorAssignmentDto> assignments = listOperatorAssignmentsUseCase.listActiveByZone(zoneId);

      List<OperatorAssignmentResponse> response = enrichWithOperatorNames(assignments);

      return ResponseEntity.ok(response);
   }

   /**
    * GET /operators/{operatorId}/assignments - Lista asignaciones de un operador.
    */
   @GetMapping("/operators/{operatorId}/assignments")
   @PreAuthorize("hasAnyRole('ADMIN', 'AUTORIDAD')")
   public ResponseEntity<List<OperatorAssignmentResponse>> listByOperator(@PathVariable Long operatorId) {
      log.debug("GET /operators/{}/assignments - Listando asignaciones", operatorId);

      List<OperatorAssignmentDto> assignments = listOperatorAssignmentsUseCase.listByOperator(operatorId);

      List<OperatorAssignmentResponse> response = enrichWithOperatorNames(assignments);

      return ResponseEntity.ok(response);
   }

   // ========================= OBTENER ASIGNACIÓN =========================

   /**
    * GET /operator-assignments/{id} - Obtiene una asignación específica con detalles completos.
    */
   @GetMapping("/operator-assignments/{id}")
   @PreAuthorize("hasAnyRole('ADMIN', 'AUTORIDAD', 'OPERADOR')")
   public ResponseEntity<OperatorAssignmentDetailResponse> getAssignment(@PathVariable Long id) {
      log.debug("🔍 GET /operator-assignments/{} - Obteniendo asignación", id);

      OperatorAssignmentDetailDto assignment = getOperatorAssignmentUseCase.getAssignment(id);

      // Cargar información completa del operador desde auth-service
      AuthServiceClient.UserDto operator = authServiceClient.getUserById(assignment.operator().id());

      // Construir response enriquecido
      OperatorAssignmentDetailResponse response = new OperatorAssignmentDetailResponse(
            assignment.id(),
            assignment.startDate(),
            assignment.endDate(),
            assignment.status(),
            assignment.durationDays(),
            new OperatorAssignmentDetailResponse.OperatorInfo(
                  operator.id(),
                  operator.firstName(),
                  operator.lastName(),
                  operator.getFullName(),
                  operator.email(),
                  operator.phoneNumber()
            ),
            new OperatorAssignmentDetailResponse.ZoneInfo(
                  assignment.zone().id(),
                  assignment.zone().name(),
                  assignment.zone().code()
            ),
            new OperatorAssignmentDetailResponse.ShiftInfo(
                  assignment.shift().id(),
                  assignment.shift().name(),
                  assignment.shift().startTime(),
                  assignment.shift().endTime()
            ),
            assignment.createdAt(),
            assignment.createdBy(),
            assignment.updatedAt(),
            assignment.updatedBy()
      );

      return ResponseEntity.ok(response);
   }

   // ========================= ACTUALIZAR ASIGNACIÓN =========================

   /**
    * PATCH /operator-assignments/{id} - Actualiza las fechas de una asignación.
    */
   @PatchMapping("/operator-assignments/{id}")
   @PreAuthorize("hasAnyRole('ADMIN', 'AUTORIDAD')")
   public ResponseEntity<OperatorAssignmentResponse> updateAssignment(
         @PathVariable Long id,
         @Valid @RequestBody UpdateOperatorAssignmentRequest request,
         HttpServletRequest httpRequest) {

      log.debug("PATCH /operator-assignments/{} - Actualizando asignación", id);

      Long userId = getUserIdFromToken(httpRequest);

      UpdateOperatorAssignmentCommand command = mapper.toCommand(id, request, userId);

      OperatorAssignmentDto updated = updateOperatorAssignmentUseCase.updateAssignment(command);

      // Nombre del operador
      OperatorAssignmentResponse response = enrichSingleAssignment(updated);

      return ResponseEntity.ok(response);
   }

   // ========================= FINALIZAR ASIGNACIÓN =========================

   /**
    * DELETE /operator-assignments/{id} - Finaliza una asignación (marca endDate como hoy).
    */
   @DeleteMapping("/operator-assignments/{id}")
   @PreAuthorize("hasAnyRole('ADMIN', 'AUTORIDAD')")
   public ResponseEntity<OperatorAssignmentResponse> endAssignment(
         @PathVariable Long id,
         HttpServletRequest httpRequest) {

      log.debug("DELETE /operator-assignments/{} - Finalizando asignación", id);

      Long userId = getUserIdFromToken(httpRequest);

      OperatorAssignmentDto ended = endOperatorAssignmentUseCase.endNow(id, userId);

      // Nombre del operador
      OperatorAssignmentResponse response = enrichSingleAssignment(ended);

      return ResponseEntity.ok(response);
   }

   // ========================= HELPER METHODS =========================

   /**
    * Enriquece una lista de asignaciones con nombres de operadores desde auth-service.
    */
   private List<OperatorAssignmentResponse> enrichWithOperatorNames(List<OperatorAssignmentDto> assignments) {
      if (assignments.isEmpty()) {
         return List.of();
      }

      // Obtener todos los operadores activos
      List<AuthServiceClient.OperatorDto> operators = authServiceClient.getActiveOperators();
      Map<Long, String> operatorNames = operators.stream()
            .collect(Collectors.toMap(
                  AuthServiceClient.OperatorDto::id,
                  AuthServiceClient.OperatorDto::getFullName
            ));

      // Enriquecer cada asignación
      return assignments.stream()
            .map(dto -> {
               OperatorAssignmentResponse resp = mapper.toResponse(dto);
               return new OperatorAssignmentResponse(
                     resp.id(),
                     resp.operatorId(),
                     operatorNames.get(resp.operatorId()),
                     resp.zoneId(),
                     resp.zoneName(),
                     resp.shiftId(),
                     resp.shiftName(),
                     resp.startDate(),
                     resp.endDate(),
                     resp.status(),
                     resp.createdAt()
               );
            })
            .collect(Collectors.toList());
   }

   /**
    * Enriquece una asignación individual con nombre del operador.
    */
   private OperatorAssignmentResponse enrichSingleAssignment(OperatorAssignmentDto dto) {
      // Obtener información del operador
      AuthServiceClient.UserDto operator = authServiceClient.getUserById(dto.operatorId());

      OperatorAssignmentResponse resp = mapper.toResponse(dto);
      return new OperatorAssignmentResponse(
            resp.id(),
            resp.operatorId(),
            operator.getFullName(),
            resp.zoneId(),
            resp.zoneName(),
            resp.shiftId(),
            resp.shiftName(),
            resp.startDate(),
            resp.endDate(),
            resp.status(),
            resp.createdAt()
      );
   }

   /**
    * Extrae el userId del JWT token.
    */
   private Long getUserIdFromToken(HttpServletRequest request) {
      String token = extractTokenFromRequest(request);
      return jwtService.extractUserId(token);
   }

   /**
    * Extrae el token JWT del header Authorization.
    */
   private String extractTokenFromRequest(HttpServletRequest request) {
      String authHeader = request.getHeader("Authorization");
      if (authHeader != null && authHeader.startsWith("Bearer ")) {
         return authHeader.substring(7);
      }
      throw new IllegalArgumentException("Token JWT no encontrado en el header Authorization");
   }
}