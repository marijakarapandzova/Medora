package medora.controller;

import medora.dto.ReferralDTO;
import medora.dto.CreateReferralRequest;
import medora.models.domain.Referrals;
import medora.service.ReferralService;
import medora.util.SecurityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/referrals")
public class ReferralController {

    private static final Logger logger = LoggerFactory.getLogger(ReferralController.class);

    private final ReferralService referralService;
    private final SecurityUtil securityUtil;

    public ReferralController(ReferralService referralService, SecurityUtil securityUtil) {
        this.referralService = referralService;
        this.securityUtil = securityUtil;
    }

    @PostMapping
    public ResponseEntity<?> createReferral(@RequestBody CreateReferralRequest request, HttpServletRequest httpRequest) {
        try {
            String role = securityUtil.getRoleFromRequest(httpRequest);
            if (role == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Unauthorized"));
            }

            // Patients cannot create referrals
            if (role.equals("PATIENT")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Patients cannot create referrals"));
            }

            if (request.getMedicalRecordId() == null || request.getMedicalRecordId() <= 0) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Valid medical record ID is required"));
            }
            if (request.getFromDoctorId() == null || request.getFromDoctorId() <= 0) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Valid from doctor ID is required"));
            }
            if (request.getToDoctorId() == null || request.getToDoctorId() <= 0) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Valid to doctor ID is required"));
            }
            if (request.getReason() == null || request.getReason().isBlank()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Referral reason is required"));
            }
            if (request.getAppointmentDate() == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Appointment date is required"));
            }
            if (request.getAppointmentTime() == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Appointment time is required"));
            }

            Referrals referral = referralService.createReferral(
                    request.getMedicalRecordId(),
                    request.getFromDoctorId(),
                    request.getToDoctorId(),
                    request.getReason(),
                    request.getReferralDate(),
                    request.getAppointmentDate(),
                    request.getAppointmentTime()
            );

            ReferralDTO dto = convertToDTO(referral);
            return ResponseEntity.status(HttpStatus.CREATED).body(dto);
        } catch (RuntimeException e) {
            logger.error("Error creating referral: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error creating referral: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create referral: " + e.getMessage()));
        }
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<?> getReferralsByPatient(@PathVariable Long patientId) {
        try {
            logger.info("Fetching referrals for patient ID: {}", patientId);
            List<Referrals> referrals = referralService.getReferralsForPatient(patientId);
            List<ReferralDTO> dtos = referrals.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        } catch (RuntimeException e) {
            logger.error("Error fetching referrals: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error fetching referrals: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch referrals: " + e.getMessage()));
        }
    }

    @GetMapping("/from-doctor/{doctorId}")
    public ResponseEntity<?> getReferralsByFromDoctor(@PathVariable Long doctorId) {
        try {
            logger.info("Fetching referrals from doctor ID: {}", doctorId);
            List<Referrals> referrals = referralService.getReferralsByFromDoctor(doctorId);
            List<ReferralDTO> dtos = referrals.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        } catch (RuntimeException e) {
            logger.error("Error fetching referrals: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error fetching referrals: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch referrals: " + e.getMessage()));
        }
    }

    @GetMapping("/to-doctor/{doctorId}")
    public ResponseEntity<?> getReferralsByToDoctor(@PathVariable Long doctorId) {
        try {
            logger.info("Fetching referrals to doctor ID: {}", doctorId);
            List<Referrals> referrals = referralService.getReferralsToDoctor(doctorId);
            List<ReferralDTO> dtos = referrals.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        } catch (RuntimeException e) {
            logger.error("Error fetching referrals: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error fetching referrals: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch referrals: " + e.getMessage()));
        }
    }

    @GetMapping("/{referralId}")
    public ResponseEntity<?> getReferralById(@PathVariable Long referralId) {
        try {
            logger.info("Fetching referral with ID: {}", referralId);
            return referralService.getReferralById(referralId)
                    .map(r -> ResponseEntity.ok(convertToDTO(r)))
                    .orElse(ResponseEntity.notFound().build());
        } catch (RuntimeException e) {
            logger.error("Error fetching referral: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error fetching referral: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch referral: " + e.getMessage()));
        }
    }

    @GetMapping("/doctor/{doctorId}/sent")
    public ResponseEntity<?> getReferralsSentByDoctor(@PathVariable Long doctorId) {
        try {
            logger.info("Fetching referrals sent by doctor ID: {}", doctorId);
            List<Referrals> referrals = referralService.getReferralsByFromDoctor(doctorId);
            List<ReferralDTO> dtos = referrals.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        } catch (RuntimeException e) {
            logger.error("Error fetching referrals: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error fetching referrals: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch referrals: " + e.getMessage()));
        }
    }

    @GetMapping("/doctor/{doctorId}/received")
    public ResponseEntity<?> getReferralsReceivedByDoctor(@PathVariable Long doctorId) {
        try {
            logger.info("Fetching referrals received by doctor ID: {}", doctorId);
            List<Referrals> referrals = referralService.getReferralsToDoctor(doctorId);
            List<ReferralDTO> dtos = referrals.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        } catch (RuntimeException e) {
            logger.error("Error fetching referrals: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error fetching referrals: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch referrals: " + e.getMessage()));
        }
    }

    private ReferralDTO convertToDTO(Referrals referral) {
        String fromDoctorName = "";
        Long fromDoctorId = null;
        if (referral.getFromDoctor() != null) {
            fromDoctorName = referral.getFromDoctor().getFirstName() + " " + referral.getFromDoctor().getLastName();
            fromDoctorId = referral.getFromDoctor().getDoctorId();
        }

        String toDoctorName = "";
        Long toDoctorId = null;
        if (referral.getToDoctor() != null) {
            toDoctorName = referral.getToDoctor().getFirstName() + " " + referral.getToDoctor().getLastName();
            toDoctorId = referral.getToDoctor().getDoctorId();
        }

        Long medicalRecordId = null;
        Long patientId = null;
        String patientName = "";
        if (referral.getMedicalRecord() != null) {
            medicalRecordId = referral.getMedicalRecord().getRecordId();
            if (referral.getMedicalRecord().getPatient() != null) {
                patientId = referral.getMedicalRecord().getPatient().getPatientId();
                patientName = referral.getMedicalRecord().getPatient().getFirstName() + " "
                        + referral.getMedicalRecord().getPatient().getLastName();
            }
        }

        return new ReferralDTO(
                referral.getReferralId(),
                medicalRecordId,
                patientId,
                patientName,
                fromDoctorId,
                fromDoctorName,
                toDoctorId,
                toDoctorName,
                referral.getReason(),
                referral.getReferralDate(),
                referral.getAppointmentDate(),
                referral.getAppointmentTime()
        );
    }
}
