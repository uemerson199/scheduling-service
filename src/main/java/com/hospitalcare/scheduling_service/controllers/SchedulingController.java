package com.hospitalcare.scheduling_service.controllers;

import com.hospitalcare.scheduling_service.dtos.AppointmentRequestDTO;
import com.hospitalcare.scheduling_service.dtos.AppointmentResponseDTO;
import com.hospitalcare.scheduling_service.services.SchedulingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/appointments")
@RequiredArgsConstructor
public class SchedulingController {

    private final SchedulingService schedulingService;

    @PostMapping
    public ResponseEntity<AppointmentResponseDTO> scheduleAppointment(@RequestBody AppointmentRequestDTO requestDTO) {
        try {
            AppointmentResponseDTO response = schedulingService.scheduleAppointment(requestDTO);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
    }
}