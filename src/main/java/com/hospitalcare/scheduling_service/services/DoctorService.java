package com.hospitalcare.scheduling_service.services;

import com.hospitalcare.scheduling_service.dtos.DoctorRequestDTO;
import com.hospitalcare.scheduling_service.dtos.DoctorResponseDTO;
import com.hospitalcare.scheduling_service.entities.Doctor;
import com.hospitalcare.scheduling_service.exceptions.ResourceNotFoundException;
import com.hospitalcare.scheduling_service.mappers.DoctorMapper;
import com.hospitalcare.scheduling_service.repositories.DoctorRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DoctorService {

    private final DoctorRepository doctorRepository;

    public Optional<DoctorResponseDTO> getDoctorById(UUID id) {
        return doctorRepository.findById(id)
                .map(DoctorMapper::toResponseDTO);
    }

    public List<DoctorResponseDTO> getAllDoctors() {
        return doctorRepository.findAll()
                .stream()
                .map(DoctorMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    public DoctorResponseDTO createDoctor(DoctorRequestDTO requestDTO) {
        Doctor entity = DoctorMapper.toEntity(requestDTO);
        Doctor savedEntity = doctorRepository.save(entity);
        return DoctorMapper.toResponseDTO(savedEntity);
    }

    @Transactional
    public DoctorResponseDTO updateDoctor(UUID id, DoctorRequestDTO requestDTO) {
        try {
            Doctor doctor = doctorRepository.getReferenceById(id);

            doctor.setName(requestDTO.getName());
            doctor.setSpecialty(requestDTO.getSpecialty());

            doctor = doctorRepository.save(doctor);

            return DoctorMapper.toResponseDTO(doctor);
        } catch (EntityNotFoundException e) {
            throw new ResourceNotFoundException("Resource Not Found");
        }
    }

    public void deleteDoctorById(UUID id) {
        if (!doctorRepository.existsById(id)) {
            throw new ResourceNotFoundException("Resource Not Found!");
        }
        doctorRepository.deleteById(id);
    }

}
