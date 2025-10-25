package com.hospitalcare.scheduling_service.mappers;

import com.hospitalcare.scheduling_service.dtos.DoctorRequestDTO;
import com.hospitalcare.scheduling_service.dtos.DoctorResponseDTO;
import com.hospitalcare.scheduling_service.entities.Doctor;

public class DoctorMapper {

    public static Doctor toEntity(DoctorRequestDTO dto) {
        return Doctor.builder()
                .name(dto.getName())
                .specialty(dto.getSpecialty())
                .build();
    }

    public static DoctorResponseDTO toResponseDTO(Doctor entity) {
        DoctorResponseDTO dto = new DoctorResponseDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setSpecialty(entity.getSpecialty());

        return dto;
    }

}
