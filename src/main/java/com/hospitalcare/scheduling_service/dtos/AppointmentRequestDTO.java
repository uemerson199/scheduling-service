package com.hospitalcare.scheduling_service.dtos;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class AppointmentRequestDTO {
    private UUID patientId;
    private UUID doctorId;
    private LocalDateTime appointmentTime;
}