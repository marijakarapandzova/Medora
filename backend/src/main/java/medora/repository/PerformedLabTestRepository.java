package medora.repository;


import medora.models.domain.PerformedLabTests;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PerformedLabTestRepository extends JpaRepository<PerformedLabTests, Long> {

    List<PerformedLabTests> findByPatientPatientId(Long patientId);

    List<PerformedLabTests> findByDoctorDoctorId(Long doctorId);
}