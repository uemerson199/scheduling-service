package com.hospitalcare.scheduling_service.repositories;

import com.hospitalcare.scheduling_service.entities.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {

    boolean existsByDoctorIdAndAppointmentTime(UUID doctorId, LocalDateTime appointmentTime);

}