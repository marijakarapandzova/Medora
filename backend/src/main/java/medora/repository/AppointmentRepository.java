package medora.repository;

import medora.models.domain.Appointment;
import medora.models.enums.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    /**
     * Get appointments for a patient
     */
    List<Appointment> findByPatientPatientIdOrderByAppointmentDateAscAppointmentTimeAsc(
            Long patientId
    );

    /**
     * Get appointments for a doctor
     */
    List<Appointment> findByDoctorDoctorIdOrderByAppointmentDateAscAppointmentTimeAsc(
            Long doctorId
    );

    /**
     * Get doctor schedule for a specific date
     */
    List<Appointment> findByDoctorDoctorIdAndAppointmentDateOrderByAppointmentTimeAsc(
            Long doctorId,
            LocalDate appointmentDate
    );

    /**
     * Check if doctor already has appointment at this slot
     */
    boolean existsByDoctorDoctorIdAndAppointmentDateAndAppointmentTimeAndStatusNot(
            Long doctorId,
            LocalDate appointmentDate,
            LocalTime appointmentTime,
            AppointmentStatus status
    );

    /**
     * Check duplicate patient appointment
     */
    boolean existsByPatientPatientIdAndDoctorDoctorIdAndAppointmentDateAndAppointmentTimeAndStatusNot(
            Long patientId,
            Long doctorId,
            LocalDate appointmentDate,
            LocalTime appointmentTime,
            AppointmentStatus status
    );

    /**
     * UC007 – Cancel Appointment
     */
    @Transactional
    @Modifying
    @Query("""
        UPDATE Appointment a
        SET a.status = 'CANCELLED'
        WHERE a.appointmentId = :appointmentId
    """)
    void cancelAppointment(@Param("appointmentId") Long appointmentId);

    /**
     * Get active appointments for patient
     */
    @Query("""
        SELECT a FROM Appointment a
        WHERE a.patient.patientId = :patientId
        AND a.status != 'CANCELLED'
        ORDER BY a.appointmentDate DESC, a.appointmentTime DESC
    """)
    List<Appointment> getActiveAppointmentsForPatient(
            @Param("patientId") Long patientId
    );

    /**
     * Get active appointments for doctor
     */
    @Query("""
        SELECT a FROM Appointment a
        WHERE a.doctor.doctorId = :doctorId
        AND a.status != 'CANCELLED'
        ORDER BY a.appointmentDate DESC, a.appointmentTime DESC
    """)
    List<Appointment> getActiveAppointmentsForDoctor(
            @Param("doctorId") Long doctorId
    );
}