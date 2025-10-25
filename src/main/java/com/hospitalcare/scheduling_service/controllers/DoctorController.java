package com.hospitalcare.scheduling_service.controllers;

import com.hospitalcare.scheduling_service.dtos.DoctorRequestDTO;
import com.hospitalcare.scheduling_service.dtos.DoctorResponseDTO;
import com.hospitalcare.scheduling_service.services.DoctorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/doctors")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;

    @GetMapping("/{id}")
    public ResponseEntity<DoctorResponseDTO> getDoctorById(@PathVariable UUID id) {
        return doctorService.getDoctorById(id)
                .map(doctor -> new ResponseEntity<>(doctor, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping
    public ResponseEntity<List<DoctorResponseDTO>> getAllDoctors() {
        List<DoctorResponseDTO> doctors = doctorService.getAllDoctors();
        return new ResponseEntity<>(doctors, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<DoctorResponseDTO> createDoctor(@RequestBody DoctorRequestDTO requestDTO) {
        DoctorResponseDTO savedDoctor = doctorService.createDoctor(requestDTO);
        return new ResponseEntity<>(savedDoctor, HttpStatus.CREATED);
    }

    @PutMapping(value = "/{id}")
    public ResponseEntity<DoctorResponseDTO> update(@PathVariable UUID id, @RequestBody DoctorRequestDTO dto) {
        DoctorResponseDTO responseDTO = doctorService.updateDoctor(id, dto);
        return ResponseEntity.ok(responseDTO);
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Void> deleteDoctorById(@PathVariable UUID id) {
        doctorService.deleteDoctorById(id);
        return ResponseEntity.noContent().build();
    }



}