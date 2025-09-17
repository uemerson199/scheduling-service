package com.hospitalcare.scheduling_service.repositories;

import com.hospitalcare.scheduling_service.entities.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, UUID> {

}