package medora.service;

import medora.models.domain.Appointment;
import medora.models.domain.Patient;
import medora.models.domain.Doctors;
import medora.models.enums.AppointmentStatus;
import medora.repository.AppointmentRepository;
import medora.repository.PatientRepository;
import medora.repository.DoctorRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * AppointmentService handles appointment operations.
 * UC006 – Create Appointment Record
 * UC007 – Cancel Appointment Record
 */
@Service
public class AppointmentService {

    private static final Logger logger =
            LoggerFactory.getLogger(AppointmentService.class);

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              PatientRepository patientRepository,
                              DoctorRepository doctorRepository) {

        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
    }

    /**
     * UC006 – Create Appointment Record
     */
    @Transactional
    public Appointment createAppointment(Long patientId,
                                         Long doctorId,
                                         LocalDate appointmentDate,
                                         LocalTime appointmentTime) {

        if (patientId == null || patientId <= 0) {
            throw new IllegalArgumentException("Patient ID must be valid");
        }

        if (doctorId == null || doctorId <= 0) {
            throw new IllegalArgumentException("Doctor ID must be valid");
        }

        if (appointmentDate == null) {
            throw new IllegalArgumentException("Appointment date is required");
        }

        if (appointmentTime == null) {
            throw new IllegalArgumentException("Appointment time is required");
        }

        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() ->
                        new RuntimeException("Patient not found with ID: " + patientId));

        Doctors doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() ->
                        new RuntimeException("Doctor not found with ID: " + doctorId));

        LocalDateTime appointmentDateTime =
                LocalDateTime.of(appointmentDate, appointmentTime);

        // Future validation
        if (!appointmentDateTime.isAfter(LocalDateTime.now())) {
            throw new RuntimeException(
                    "Appointment must be scheduled for a future date and time"
            );
        }

        // Doctor slot validation
        boolean doctorBusy =
                appointmentRepository
                        .existsByDoctorDoctorIdAndAppointmentDateAndAppointmentTimeAndStatusNot(
                                doctorId,
                                appointmentDate,
                                appointmentTime,
                                AppointmentStatus.CANCELLED
                        );

        if (doctorBusy) {
            throw new RuntimeException("This appointment slot is already booked");
        }

        // Duplicate patient validation
        boolean duplicateAppointment =
                appointmentRepository
                        .existsByPatientPatientIdAndDoctorDoctorIdAndAppointmentDateAndAppointmentTimeAndStatusNot(
                                patientId,
                                doctorId,
                                appointmentDate,
                                appointmentTime,
                                AppointmentStatus.CANCELLED
                        );

        if (duplicateAppointment) {
            throw new RuntimeException(
                    "Patient already has this appointment scheduled"
            );
        }

        Appointment appointment = new Appointment();

        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setAppointmentDate(appointmentDate);
        appointment.setAppointmentTime(appointmentTime);
        appointment.setStatus(AppointmentStatus.SCHEDULED);

        logger.info(
                "Creating appointment for patient ID: {} with doctor ID: {}",
                patientId,
                doctorId
        );

        return appointmentRepository.save(appointment);
    }

    /**
     * UC007 – Cancel Appointment Record
     */
    @Transactional
    public Appointment cancelAppointment(Long appointmentId) {

        if (appointmentId == null || appointmentId <= 0) {
            throw new IllegalArgumentException("Appointment ID must be valid");
        }

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Appointment not found with ID: " + appointmentId
                        ));

        LocalDateTime appointmentDateTime =
                LocalDateTime.of(
                        appointment.getAppointmentDate(),
                        appointment.getAppointmentTime()
                );

        if (!appointmentDateTime.isAfter(LocalDateTime.now())) {
            throw new RuntimeException(
                    "Only future appointments can be cancelled"
            );
        }

        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new RuntimeException("Appointment is already cancelled");
        }

        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new RuntimeException(
                    "Completed appointments cannot be cancelled"
            );
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);

        logger.info("Cancelling appointment with ID: {}", appointmentId);

        return appointmentRepository.save(appointment);
    }


    @Transactional
    public Appointment completeAppointment(Long appointmentId) {

        if (appointmentId == null || appointmentId <= 0) {
            throw new IllegalArgumentException("Appointment ID must be valid");
        }

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Appointment not found with ID: " + appointmentId
                        ));

        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new RuntimeException(
                    "Cancelled appointments cannot be completed"
            );
        }

        appointment.setStatus(AppointmentStatus.COMPLETED);

        logger.info("Completing appointment with ID: {}", appointmentId);

        return appointmentRepository.save(appointment);
    }

    @Transactional(readOnly = true)
    public Optional<Appointment> getAppointmentById(Long appointmentId) {

        if (appointmentId == null || appointmentId <= 0) {
            throw new IllegalArgumentException("Appointment ID must be valid");
        }

        logger.info("Fetching appointment with ID: {}", appointmentId);

        return appointmentRepository.findById(appointmentId);
    }


    @Transactional(readOnly = true)
    public List<Appointment> getAppointmentsForPatient(Long patientId) {

        if (patientId == null || patientId <= 0) {
            throw new IllegalArgumentException("Patient ID must be valid");
        }

        if (!patientRepository.existsById(patientId)) {
            throw new RuntimeException(
                    "Patient not found with ID: " + patientId
            );
        }

        logger.info("Fetching appointments for patient ID: {}", patientId);

        return appointmentRepository
                .findByPatientPatientIdOrderByAppointmentDateAscAppointmentTimeAsc(
                        patientId
                );
    }


    @Transactional(readOnly = true)
    public List<Appointment> getAppointmentsForDoctor(Long doctorId) {

        if (doctorId == null || doctorId <= 0) {
            throw new IllegalArgumentException("Doctor ID must be valid");
        }

        if (!doctorRepository.existsById(doctorId)) {
            throw new RuntimeException(
                    "Doctor not found with ID: " + doctorId
            );
        }

        logger.info("Fetching appointments for doctor ID: {}", doctorId);

        return appointmentRepository
                .findByDoctorDoctorIdOrderByAppointmentDateAscAppointmentTimeAsc(
                        doctorId
                );
    }


    @Transactional(readOnly = true)
    public List<Appointment> getDoctorSchedule(Long doctorId,
                                               LocalDate appointmentDate) {

        if (doctorId == null || doctorId <= 0) {
            throw new IllegalArgumentException("Doctor ID must be valid");
        }

        if (appointmentDate == null) {
            throw new IllegalArgumentException("Appointment date is required");
        }

        logger.info(
                "Fetching doctor schedule for doctor ID: {} on {}",
                doctorId,
                appointmentDate
        );

        return appointmentRepository
                .findByDoctorDoctorIdAndAppointmentDateOrderByAppointmentTimeAsc(
                        doctorId,
                        appointmentDate
                );
    }


    @Transactional(readOnly = true)
    public List<Appointment> getAllAppointments() {

        logger.info("Fetching all appointments");

        return appointmentRepository.findAll();
    }


    public LocalTime findNextAvailableSlot(Long doctorId, LocalDate appointmentDate) {
        LocalTime[] timeSlots = {
                LocalTime.of(9, 0),
                LocalTime.of(10, 0),
                LocalTime.of(11, 0),
                LocalTime.of(13, 0),
                LocalTime.of(14, 0),
                LocalTime.of(15, 0),
                LocalTime.of(16, 0)
        };

        for (LocalTime timeSlot : timeSlots) {
            boolean isBooked = appointmentRepository
                    .existsByDoctorDoctorIdAndAppointmentDateAndAppointmentTimeAndStatusNot(
                            doctorId,
                            appointmentDate,
                            timeSlot,
                            AppointmentStatus.CANCELLED
                    );
            if (!isBooked) {
                return timeSlot;
            }
        }
        return null;
    }
}