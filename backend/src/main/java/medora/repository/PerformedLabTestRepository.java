package medora.repository;


import medora.models.domain.PerformedLabTests;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PerformedLabTestRepository extends JpaRepository<PerformedLabTests, Long> {

    List<PerformedLabTests> findByPatientPatientId(Long patientId);

    List<PerformedLabTests> findByDoctorDoctorId(Long doctorId);

    // UC020 – Auto billing: Get lab tests for a patient on a specific date
    @Query("""
        SELECT plt FROM PerformedLabTests plt
        WHERE plt.patient.patientId = :patientId
        AND plt.testDate = :testDate
    """)
    List<PerformedLabTests> findByPatientAndDate(@Param("patientId") Long patientId, @Param("testDate") java.time.LocalDate testDate);

}