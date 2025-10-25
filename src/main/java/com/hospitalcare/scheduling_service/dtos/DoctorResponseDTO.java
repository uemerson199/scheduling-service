package com.hospitalcare.scheduling_service.dtos;

import lombok.Data;

import java.util.UUID;

@Data
public class DoctorResponseDTO {
    private UUID id;
    private String name;
    private String specialty;

}
