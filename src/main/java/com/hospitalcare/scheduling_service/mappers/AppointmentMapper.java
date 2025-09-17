package com.hospitalcare.scheduling_service.mappers;

import com.hospitalcare.scheduling_service.dtos.AppointmentResponseDTO;
import com.hospitalcare.scheduling_service.entities.Appointment;

public class AppointmentMapper {

    public static AppointmentResponseDTO toResponseDTO(Appointment entity) {
        AppointmentResponseDTO dto = new AppointmentResponseDTO();
        dto.setId(entity.getId());
        dto.setPatientId(entity.getPatientId());
        dto.setDoctorId(entity.getDoctor().getId());
        dto.setDoctorName(entity.getDoctor().getName());
        dto.setAppointmentTime(entity.getAppointmentTime());
        dto.setStatus(entity.getStatus());
        return dto;
    }
}