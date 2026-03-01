package com.winnersystems.smartparking.parking.infrastructure.adapter.input.rest.operatorassignment.mapper;

import com.winnersystems.smartparking.parking.application.dto.command.AssignOperatorsCommand;
import com.winnersystems.smartparking.parking.application.dto.command.UpdateOperatorAssignmentCommand;
import com.winnersystems.smartparking.parking.application.dto.query.OperatorAssignmentDetailDto;
import com.winnersystems.smartparking.parking.application.dto.query.OperatorAssignmentDto;
import com.winnersystems.smartparking.parking.infrastructure.adapter.input.rest.operatorassignment.dto.request.AssignOperatorsRequest;
import com.winnersystems.smartparking.parking.infrastructure.adapter.input.rest.operatorassignment.dto.request.UpdateOperatorAssignmentRequest;
import com.winnersystems.smartparking.parking.infrastructure.adapter.input.rest.operatorassignment.dto.response.OperatorAssignmentDetailResponse;
import com.winnersystems.smartparking.parking.infrastructure.adapter.input.rest.operatorassignment.dto.response.OperatorAssignmentResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper para convertir entre REST DTOs y Application DTOs/Commands.
 *
 * @author Edwin Yoner - Winner Systems - Smart Parking Platform
 * @version 1.0
 */
@Component
public class OperatorAssignmentRestMapper {

   // ========================= REQUEST → COMMAND =========================

   /**
    * Convierte AssignOperatorsRequest a AssignOperatorsCommand.
    */
   public AssignOperatorsCommand toCommand(AssignOperatorsRequest request, Long createdBy) {
      List<AssignOperatorsCommand.OperatorAssignmentData> assignments = request.getAssignments().stream()
            .map(data -> new AssignOperatorsCommand.OperatorAssignmentData(
                  data.getOperatorId(),
                  data.getStartDate(),
                  data.getEndDate()
            ))
            .collect(Collectors.toList());

      return new AssignOperatorsCommand(
            request.getZoneId(),
            request.getShiftId(),
            assignments,
            createdBy
      );
   }

   /**
    * Convierte UpdateOperatorAssignmentRequest a UpdateOperatorAssignmentCommand.
    */
   public UpdateOperatorAssignmentCommand toCommand(Long assignmentId,
                                                    UpdateOperatorAssignmentRequest request,
                                                    Long updatedBy) {
      return new UpdateOperatorAssignmentCommand(
            assignmentId,
            request.getStartDate(),
            request.getEndDate(),
            updatedBy
      );
   }

   // ========================= DTO → RESPONSE =========================

   /**
    * Convierte OperatorAssignmentDto a OperatorAssignmentResponse.
    */
   public OperatorAssignmentResponse toResponse(OperatorAssignmentDto dto) {
      return new OperatorAssignmentResponse(
            dto.id(),
            dto.operatorId(),
            dto.operatorName(),
            dto.zoneId(),
            dto.zoneName(),
            dto.shiftId(),
            dto.shiftName(),
            dto.startDate(),
            dto.endDate(),
            dto.status(),
            dto.createdAt()
      );
   }

   /**
    * Convierte OperatorAssignmentDetailDto a OperatorAssignmentDetailResponse.
    */
   public OperatorAssignmentDetailResponse toDetailResponse(OperatorAssignmentDetailDto dto) {
      return new OperatorAssignmentDetailResponse(
            dto.id(),
            dto.startDate(),
            dto.endDate(),
            dto.status(),
            dto.durationDays(),
            new OperatorAssignmentDetailResponse.OperatorInfo(
                  dto.operator().id(),
                  dto.operator().firstName(),
                  dto.operator().lastName(),
                  dto.operator().fullName(),
                  dto.operator().email(),
                  dto.operator().phoneNumber()
            ),
            new OperatorAssignmentDetailResponse.ZoneInfo(
                  dto.zone().id(),
                  dto.zone().name(),
                  dto.zone().code()
            ),
            new OperatorAssignmentDetailResponse.ShiftInfo(
                  dto.shift().id(),
                  dto.shift().name(),
                  dto.shift().startTime(),
                  dto.shift().endTime()
            ),
            dto.createdAt(),
            dto.createdBy(),
            dto.updatedAt(),
            dto.updatedBy()
      );
   }
}