package com.hospitalcare.scheduling_service.dtos;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class AppointmentResponseDTO {
    private UUID id;
    private UUID patientId;
    private UUID doctorId;
    private String doctorName;
    private LocalDateTime appointmentTime;
    private String status;
}