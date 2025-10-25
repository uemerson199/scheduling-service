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
// import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SchedulingService {

    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final RedisTemplate<String, String> redisTemplate;
    // private final KafkaTemplate<String, AppointmentResponseDTO> kafkaTemplate;
    private final WebClient patientServiceWebClient;

    /**
     * Valida se um paciente existe no serviço de pacientes.
     * @param patientId O ID do paciente a ser validado.
     * @return PatientResponseDTO se o paciente for válido.
     * @throws RuntimeException se o paciente não for encontrado ou ocorrer um erro.
     */
    private PatientResponseDTO validatePatient(UUID patientId) {
        log.info("Validando paciente com ID: {}", patientId);
        PatientResponseDTO patient = patientServiceWebClient.get()
                .uri("/{patientId}", patientId)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        clientResponse -> clientResponse.bodyToMono(String.class).
                                map(body -> new RuntimeException
                                        ("Erro ao buscar paciente: " + body + " Status: " + clientResponse.statusCode())))
                .bodyToMono(PatientResponseDTO.class)
                .block();

        if (patient == null || patient.getId() == null) {
            throw new RuntimeException("Paciente com ID " + patientId + " não encontrado no Patient Service.");
        }
        log.info("Paciente {} validado com sucesso: {}", patient.getId(), patient.getName());
        return patient;
    }

    @Transactional
    public AppointmentResponseDTO scheduleAppointment(AppointmentRequestDTO requestDTO) {

        Doctor doctor = doctorRepository.findById(requestDTO.getDoctorId())
                .orElseThrow(() -> new RuntimeException("Médico não encontrado!"));

        validatePatient(requestDTO.getPatientId());

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

            // TODO: Reativar a integração com Kafka quando o notification-service estiver pronto
            // kafkaTemplate.send("appointment_events", responseDTO);
            // log.info("Evento de agendamento enviado para o tópico Kafka.");

            return responseDTO;

        } finally {
            redisTemplate.delete(lockKey);
        }
    }

    public List<AppointmentResponseDTO> getAllAppointments() {
        log.info("Buscando todos os agendamentos");
        return appointmentRepository.findAll()
                .stream()
                .map(AppointmentMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    public AppointmentResponseDTO getAppointmentById(UUID id) {
        log.info("Buscando agendamento com ID: {}", id);
        return appointmentRepository.findById(id)
                .map(AppointmentMapper::toResponseDTO)
                .orElseThrow(() -> new RuntimeException("Agendamento com ID " + id + " não encontrado."));
    }

    @Transactional
    public AppointmentResponseDTO updateAppointment(UUID id, AppointmentRequestDTO requestDTO) {
        log.info("Atualizando agendamento com ID: {}", id);

        Appointment existingAppointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Agendamento com ID " + id + " não encontrado."));

        Doctor newDoctor = doctorRepository.findById(requestDTO.getDoctorId())
                .orElseThrow(() -> new RuntimeException("Médico com ID " + requestDTO.getDoctorId() + " não encontrado!"));

        validatePatient(requestDTO.getPatientId());

        String lockKey = "appointment_lock:%s:%s".formatted(requestDTO.getDoctorId(), requestDTO.getAppointmentTime());
        Boolean lockAcquired = redisTemplate.opsForValue().setIfAbsent(lockKey, "locked", Duration.ofSeconds(10));

        if (Boolean.FALSE.equals(lockAcquired)) {
            log.warn("Tentativa de atualização concorrente para o mesmo horário. LockKey: {}", lockKey);
            throw new RuntimeException("Este horário está sendo processado. Tente novamente em alguns segundos.");
        }

        try {
            boolean timeOrDoctorChanged = !existingAppointment.getDoctor().getId().equals(requestDTO.getDoctorId()) ||
                    !existingAppointment.getAppointmentTime().equals(requestDTO.getAppointmentTime());

            if (timeOrDoctorChanged) {
                boolean isSlotTaken = appointmentRepository.existsByDoctorIdAndAppointmentTime(requestDTO.getDoctorId(), requestDTO.getAppointmentTime());
                if (isSlotTaken) {
                    throw new RuntimeException("Este novo horário não está disponível.");
                }
            }

            existingAppointment.setPatientId(requestDTO.getPatientId());
            existingAppointment.setDoctor(newDoctor);
            existingAppointment.setAppointmentTime(requestDTO.getAppointmentTime());
            existingAppointment.setStatus("UPDATED");

            Appointment updatedAppointment = appointmentRepository.save(existingAppointment);
            log.info("Agendamento {} atualizado com sucesso.", updatedAppointment.getId());

            return AppointmentMapper.toResponseDTO(updatedAppointment);

        } finally {
            redisTemplate.delete(lockKey);
        }
    }


    public void deleteAppointment(UUID id) {
        log.info("Deletando agendamento com ID: {}", id);
        if (!appointmentRepository.existsById(id)) {
            throw new RuntimeException("Agendamento com ID " + id + " não encontrado.");
        }
        appointmentRepository.deleteById(id);
        log.info("Agendamento {} deletado com sucesso.", id);
    }
}
