package medora.service;

import medora.models.domain.Doctors;
import medora.models.domain.Referrals;
import medora.repository.DoctorRepository;
import medora.repository.MedicalRecordRepository;
import medora.repository.PatientRepository;
import medora.repository.ReferralRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * RefferalService handles referral operations.
 * UC019 – Create Referral Record
 * OPTIONAL - Use only if needed
 */
@Service
public class ReferralService {

    private static final Logger logger = LoggerFactory.getLogger(ReferralService.class);

    private final ReferralRepository referralRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final MedicalRecordRepository medicalRecordRepository;

    public ReferralService(ReferralRepository referralRepository,
                           DoctorRepository doctorRepository,
                           PatientRepository patientRepository,
                           MedicalRecordRepository medicalRecordRepository) {
        this.referralRepository = referralRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.medicalRecordRepository = medicalRecordRepository;
    }

    /**
     * UC019 – Create Referral Record
     * Create a referral from one doctor to another
     */
    @Transactional
    public Referrals createReferral(Long medicalRecordId, Long fromDoctorId, Long toDoctorId,
                                    String reason, LocalDate referralDate) {
        if (medicalRecordId == null || medicalRecordId <= 0) {
            throw new IllegalArgumentException("Medical record ID must be valid");
        }
        if (fromDoctorId == null || fromDoctorId <= 0) {
            throw new IllegalArgumentException("From doctor ID must be valid");
        }
        if (toDoctorId == null || toDoctorId <= 0) {
            throw new IllegalArgumentException("To doctor ID must be valid");
        }
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Referral reason is required");
        }
        if (referralDate == null) {
            throw new IllegalArgumentException("Referral date is required");
        }

        var medicalRecord = medicalRecordRepository.findById(medicalRecordId)
                .orElseThrow(() -> new RuntimeException("Medical record not found with ID: " + medicalRecordId));

        Doctors fromDoctor = doctorRepository.findById(fromDoctorId)
                .orElseThrow(() -> new RuntimeException("From doctor not found with ID: " + fromDoctorId));

        Doctors toDoctor = doctorRepository.findById(toDoctorId)
                .orElseThrow(() -> new RuntimeException("To doctor not found with ID: " + toDoctorId));

        if (fromDoctorId.equals(toDoctorId)) {
            throw new RuntimeException("A doctor cannot refer to themselves");
        }

        Referrals referral = new Referrals();
        referral.setMedicalRecord(medicalRecord);
        referral.setFromDoctor(fromDoctor);
        referral.setToDoctor(toDoctor);
        referral.setReason(reason);
        referral.setReferralDate(referralDate);

        logger.info("Creating referral for medical record ID: {} from doctor ID: {} to doctor ID: {}",
                medicalRecordId, fromDoctorId, toDoctorId);
        return referralRepository.save(referral);
    }


    @Transactional(readOnly = true)
    public Optional<Referrals> getReferralById(Long referralId) {
        if (referralId == null || referralId <= 0) {
            throw new IllegalArgumentException("Referral ID must be valid");
        }
        logger.info("Fetching referral with ID: {}", referralId);
        return referralRepository.findById(referralId);
    }


    @Transactional(readOnly = true)
    public List<Referrals> getReferralsForPatient(Long patientId) {
        if (patientId == null || patientId <= 0) {
            throw new IllegalArgumentException("Patient ID must be valid");
        }

        if (!patientRepository.existsById(patientId)) {
            throw new RuntimeException("Patient not found with ID: " + patientId);
        }

        logger.info("Fetching referrals for patient ID: {}", patientId);
        return referralRepository.findReferralsForPatient(patientId);
    }


    @Transactional(readOnly = true)
    public List<Referrals> getReferralsByFromDoctor(Long doctorId) {
        if (doctorId == null || doctorId <= 0) {
            throw new IllegalArgumentException("Doctor ID must be valid");
        }

        if (!doctorRepository.existsById(doctorId)) {
            throw new RuntimeException("Doctor not found with ID: " + doctorId);
        }

        logger.info("Fetching referrals made by doctor ID: {}", doctorId);
        return referralRepository.findByFromDoctorDoctorId(doctorId);
    }


    @Transactional(readOnly = true)
    public List<Referrals> getReferralsToDoctor(Long doctorId) {
        if (doctorId == null || doctorId <= 0) {
            throw new IllegalArgumentException("Doctor ID must be valid");
        }

        if (!doctorRepository.existsById(doctorId)) {
            throw new RuntimeException("Doctor not found with ID: " + doctorId);
        }

        logger.info("Fetching referrals to doctor ID: {}", doctorId);
        return referralRepository.findIncomingReferralsForDoctor(doctorId);
    }
}
