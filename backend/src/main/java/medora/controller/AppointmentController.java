package medora.controller;

import medora.dto.AppointmentDTO;
import medora.dto.CreateAppointmentRequest;
import medora.dto.PatientDTO;
import medora.dto.DoctorDTO;
import medora.models.domain.Appointment;
import medora.models.domain.Patient;
import medora.models.domain.Doctors;
import medora.service.AppointmentService;
import medora.util.SecurityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private static final Logger logger = LoggerFactory.getLogger(AppointmentController.class);

    private final AppointmentService appointmentService;
    private final SecurityUtil securityUtil;

    public AppointmentController(AppointmentService appointmentService, SecurityUtil securityUtil) {
        this.appointmentService = appointmentService;
        this.securityUtil = securityUtil;
    }

    @PostMapping
    public ResponseEntity<?> createAppointment(@RequestBody CreateAppointmentRequest request, HttpServletRequest httpRequest) {
        try {
            String role = securityUtil.getRoleFromRequest(httpRequest);
            if (role == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Unauthorized"));
            }

            if (request.getPatientId() == null || request.getPatientId() <= 0) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Valid patient ID is required"));
            }
            if (request.getDoctorId() == null || request.getDoctorId() <= 0) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Valid doctor ID is required"));
            }
            if (request.getAppointmentDate() == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Appointment date is required"));
            }
            if (request.getAppointmentTime() == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Appointment time is required"));
            }

            // Patients can only create appointments for themselves
            if (role.equals("PATIENT")) {
                Long patientIdFromToken = securityUtil.getPatientIdFromRequest(httpRequest);
                if (patientIdFromToken == null || !patientIdFromToken.equals(request.getPatientId())) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(Map.of("error", "You can only create appointments for yourself"));
                }
            }

            Appointment appointment = appointmentService.createAppointment(
                    request.getPatientId(),
                    request.getDoctorId(),
                    request.getAppointmentDate(),
                    request.getAppointmentTime()
            );
            AppointmentDTO dto = convertToDTO(appointment);

            return ResponseEntity.status(HttpStatus.CREATED).body(dto);
        } catch (RuntimeException e) {
            logger.error("Error creating appointment: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error creating appointment: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create appointment: " + e.getMessage()));
        }
    }

    @GetMapping("/{appointmentId}")
    public ResponseEntity<?> getAppointmentById(@PathVariable Long appointmentId) {
        try {
            logger.info("Fetching appointment with ID: {}", appointmentId);
            return appointmentService.getAppointmentById(appointmentId)
                    .map(a -> ResponseEntity.ok(convertToDTO(a)))
                    .orElse(ResponseEntity.notFound().build());
        } catch (RuntimeException e) {
            logger.error("Error fetching appointment: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error fetching appointment: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch appointment: " + e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllAppointments(HttpServletRequest httpRequest) {
        try {
            String role = securityUtil.getRoleFromRequest(httpRequest);
            if (role == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Unauthorized"));
            }

            // Patients cannot view all appointments
            if (role.equals("PATIENT")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Patients cannot view all appointments"));
            }

            List<Appointment> appointments;

            // Doctors can only view their own appointments
            if (role.equals("DOCTOR")) {
                Long doctorIdFromToken = securityUtil.getDoctorIdFromRequest(httpRequest);
                if (doctorIdFromToken == null || doctorIdFromToken <= 0) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(Map.of("error", "Doctor ID not found in token"));
                }
                logger.info("Fetching appointments for doctor ID: {}", doctorIdFromToken);
                appointments = appointmentService.getAppointmentsForDoctor(doctorIdFromToken);
            } else {
                // ADMIN and other roles can view all appointments
                logger.info("Fetching all appointments");
                appointments = appointmentService.getAllAppointments();
            }

            List<AppointmentDTO> dtos = appointments.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        } catch (RuntimeException e) {
            logger.error("Error fetching appointments: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error fetching appointments: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch appointments: " + e.getMessage()));
        }
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<?> getAppointmentsForPatient(@PathVariable Long patientId, HttpServletRequest httpRequest) {
        try {
            String role = securityUtil.getRoleFromRequest(httpRequest);
            if (role == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Unauthorized"));
            }

            // Patients can only view their own appointments
            if (role.equals("PATIENT")) {
                Long patientIdFromToken = securityUtil.getPatientIdFromRequest(httpRequest);
                if (patientIdFromToken == null || !patientIdFromToken.equals(patientId)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(Map.of("error", "You can only view your own appointments"));
                }
            }

            logger.info("Fetching appointments for patient ID: {}", patientId);
            List<Appointment> appointments = appointmentService.getAppointmentsForPatient(patientId);
            List<AppointmentDTO> dtos = appointments.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        } catch (RuntimeException e) {
            logger.error("Error fetching patient appointments: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error fetching patient appointments: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch appointments: " + e.getMessage()));
        }
    }

    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<?> getAppointmentsForDoctor(@PathVariable Long doctorId) {
        try {
            logger.info("Fetching appointments for doctor ID: {}", doctorId);
            List<Appointment> appointments = appointmentService.getAppointmentsForDoctor(doctorId);
            List<AppointmentDTO> dtos = appointments.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        } catch (RuntimeException e) {
            logger.error("Error fetching doctor appointments: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error fetching doctor appointments: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch appointments: " + e.getMessage()));
        }
    }

    @GetMapping("/doctor/{doctorId}/schedule")
    public ResponseEntity<?> getDoctorSchedule(
            @PathVariable Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            logger.info("Fetching doctor schedule for doctor ID: {} on {}", doctorId, date);
            List<Appointment> appointments = appointmentService.getDoctorSchedule(doctorId, date);
            List<AppointmentDTO> dtos = appointments.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        } catch (RuntimeException e) {
            logger.error("Error fetching doctor schedule: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error fetching doctor schedule: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch schedule: " + e.getMessage()));
        }
    }

    @PatchMapping("/{appointmentId}/cancel")
    public ResponseEntity<?> cancelAppointment(@PathVariable Long appointmentId, HttpServletRequest httpRequest) {
        try {
            String role = securityUtil.getRoleFromRequest(httpRequest);
            if (role == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Unauthorized"));
            }

            // Verify appointment exists and check permissions for patients
            Appointment appointment = appointmentService.getAppointmentById(appointmentId)
                    .orElseThrow(() -> new RuntimeException("Appointment not found"));

            if (role.equals("PATIENT")) {
                Long patientIdFromToken = securityUtil.getPatientIdFromRequest(httpRequest);
                Long appointmentPatientId = appointment.getPatient() != null ? appointment.getPatient().getPatientId() : null;
                if (patientIdFromToken == null || !patientIdFromToken.equals(appointmentPatientId)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(Map.of("error", "You can only cancel your own appointments"));
                }
            }

            logger.info("Cancelling appointment with ID: {}", appointmentId);
            appointment = appointmentService.cancelAppointment(appointmentId);
            AppointmentDTO dto = convertToDTO(appointment);
            return ResponseEntity.ok(dto);
        } catch (RuntimeException e) {
            logger.error("Error cancelling appointment: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error cancelling appointment: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to cancel appointment: " + e.getMessage()));
        }
    }

    @PatchMapping("/{appointmentId}/complete")
    public ResponseEntity<?> completeAppointment(@PathVariable Long appointmentId, HttpServletRequest httpRequest) {
        try {
            String role = securityUtil.getRoleFromRequest(httpRequest);
            if (role == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Unauthorized"));
            }

            // Only ADMIN and DOCTOR can complete appointments
            if (role.equals("PATIENT")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Patients cannot complete appointments"));
            }

            logger.info("Completing appointment with ID: {}", appointmentId);
            Appointment appointment = appointmentService.completeAppointment(appointmentId);
            AppointmentDTO dto = convertToDTO(appointment);
            return ResponseEntity.ok(dto);
        } catch (RuntimeException e) {
            logger.error("Error completing appointment: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error completing appointment: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to complete appointment: " + e.getMessage()));
        }
    }

    private AppointmentDTO convertToDTO(Appointment appointment) {
        PatientDTO patientDTO = null;
        DoctorDTO doctorDTO = null;

        if (appointment.getPatient() != null) {
            patientDTO = new PatientDTO();
            patientDTO.setPatientId(appointment.getPatient().getPatientId());
            patientDTO.setFirstName(appointment.getPatient().getFirstName());
            patientDTO.setLastName(appointment.getPatient().getLastName());
            patientDTO.setEmailAddress(appointment.getPatient().getEmailAddress());
            patientDTO.setEmbg(appointment.getPatient().getEmbg());
        }

        if (appointment.getDoctor() != null) {
            doctorDTO = new DoctorDTO();
            doctorDTO.setDoctorId(appointment.getDoctor().getDoctorId());
            doctorDTO.setFirstName(appointment.getDoctor().getFirstName());
            doctorDTO.setLastName(appointment.getDoctor().getLastName());
            doctorDTO.setEmailAddress(appointment.getDoctor().getEmailAddress());
        }

        return new AppointmentDTO(
                appointment.getAppointmentId(),
                appointment.getAppointmentDate(),
                appointment.getAppointmentTime(),
                appointment.getStatus(),
                patientDTO,
                doctorDTO
        );
    }
}
