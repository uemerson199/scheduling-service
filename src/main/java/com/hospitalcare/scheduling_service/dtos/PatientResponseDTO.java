package com.hospitalcare.scheduling_service.dtos;

import lombok.Data;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class PatientResponseDTO {
    private UUID id;
    private String name;
    private LocalDate dob;
    private String cpf;
}