package com.hospitalcare.scheduling_service.services;

import com.hospitalcare.scheduling_service.dtos.AppointmentRequestDTO;
import com.hospitalcare.scheduling_service.dtos.AppointmentResponseDTO;
import com.hospitalcare.scheduling_service.dtos.PatientResponseDTO;
import com.hospitalcare.scheduling_service.entities.Appointment;
import com.hospitalcare.scheduling_service.entities.Doctor;
import com.hospitalcare.scheduling_service.mappers.AppointmentMapper;
import com.hospitalcare.scheduling_service.repositories.AppointmentRepository;
import com.hospitalcare.scheduling_service.repositories.DoctorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class SchedulingService {

    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final KafkaTemplate<String, AppointmentResponseDTO> kafkaTemplate;
    private final WebClient patientServiceWebClient;

    @Transactional
    public AppointmentResponseDTO scheduleAppointment(AppointmentRequestDTO requestDTO) {

        Doctor doctor = doctorRepository.findById(requestDTO.getDoctorId())
                .orElseThrow(() -> new RuntimeException("Médico não encontrado!"));


        log.info("Validando paciente com ID: {}", requestDTO.getPatientId());
        PatientResponseDTO patient = patientServiceWebClient.get()
                .uri("/{patientId}", requestDTO.getPatientId())
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        clientResponse -> clientResponse.bodyToMono(String.class).
                                map(body -> new RuntimeException
                                        ("Erro ao buscar paciente: " + body + " Status: " + clientResponse.statusCode())))
                .bodyToMono(PatientResponseDTO.class)
                .block();

        if (patient == null || patient.getId() == null) {
            throw new RuntimeException("Paciente com ID " + requestDTO.getPatientId() + " não encontrado no Patient Service.");
        }
        log.info("Paciente {} validado com sucesso: {}", patient.getId(), patient.getName());


        String lockKey = "appointment_lock:%s:%s".formatted(requestDTO.getDoctorId(), requestDTO.getAppointmentTime());
        Boolean lockAcquired = redisTemplate.opsForValue().setIfAbsent(lockKey, "locked", Duration.ofSeconds(10));

        if (Boolean.FALSE.equals(lockAcquired)) {
            log.warn("Tentativa de agendamento concorrente para o mesmo horário. LockKey: {}", lockKey);
            throw new RuntimeException("Este horário está sendo processado por outra pessoa. Tente novamente em alguns segundos.");
        }

        try {
            boolean isSlotTaken = appointmentRepository.existsByDoctorIdAndAppointmentTime(requestDTO.getDoctorId(), requestDTO.getAppointmentTime());
            if (isSlotTaken) {
                throw new RuntimeException("Este horário não está mais disponível.");
            }

            Appointment appointment = Appointment.builder()
                    .patientId(requestDTO.getPatientId())
                    .doctor(doctor)
                    .appointmentTime(requestDTO.getAppointmentTime())
                    .status("SCHEDULED")
                    .build();

            Appointment savedAppointment = appointmentRepository.save(appointment);
            log.info("Agendamento {} criado com sucesso.", savedAppointment.getId());

            AppointmentResponseDTO responseDTO = AppointmentMapper.toResponseDTO(savedAppointment);

            kafkaTemplate.send("appointment_events", responseDTO);
            log.info("Evento de agendamento enviado para o tópico Kafka.");

            return responseDTO;

        } finally {
            redisTemplate.delete(lockKey);
        }
    }
}