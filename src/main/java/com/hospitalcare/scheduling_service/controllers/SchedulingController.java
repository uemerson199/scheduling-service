package com.hospitalcare.scheduling_service.controllers;

import com.hospitalcare.scheduling_service.dtos.AppointmentRequestDTO;
import com.hospitalcare.scheduling_service.dtos.AppointmentResponseDTO;
import com.hospitalcare.scheduling_service.services.SchedulingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/appointments")
@RequiredArgsConstructor
public class SchedulingController {

    private final SchedulingService schedulingService;

    @GetMapping
    public ResponseEntity<List<AppointmentResponseDTO>> getAllAppointments() {
        List<AppointmentResponseDTO> appointments = schedulingService.getAllAppointments();
        return new ResponseEntity<>(appointments, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AppointmentResponseDTO> getAppointmentById(@PathVariable UUID id) {
        try {
            AppointmentResponseDTO appointment = schedulingService.getAppointmentById(id);
            return new ResponseEntity<>(appointment, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping
    public ResponseEntity<AppointmentResponseDTO> scheduleAppointment(@RequestBody AppointmentRequestDTO requestDTO) {
        try {
            AppointmentResponseDTO response = schedulingService.scheduleAppointment(requestDTO);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<AppointmentResponseDTO> updateAppointment(@PathVariable UUID id, @RequestBody AppointmentRequestDTO requestDTO) {
        try {
            AppointmentResponseDTO response = schedulingService.updateAppointment(id, requestDTO);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("não encontrado")) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
    }


    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Void> deleteAppointment(@PathVariable UUID id) {
        try {
            schedulingService.deleteAppointment(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
