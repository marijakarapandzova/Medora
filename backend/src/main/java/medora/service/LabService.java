package medora.service;

import medora.models.domain.*;
import medora.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * LabService handles lab test and lab result operations.
 * UC013 – Record Lab Test Request
 * UC014 – Store Lab Results
 * UC015 – Link Medical Data to Medical Record
 */
@Service
public class LabService {

    private static final Logger logger = LoggerFactory.getLogger(LabService.class);

    private final LabTestRepository labTestRepository;
    private final LabResultsRepository labResultsRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final MedicalRecordLabResultRepository medicalRecordLabResultRepository;
    private final PerformedLabTestRepository performedLabTestRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final LabTechnicianRepository labTechnicianRepository;

    public LabService(LabTestRepository labTestRepository,
                      LabResultsRepository labResultsRepository,
                      MedicalRecordRepository medicalRecordRepository,
                      MedicalRecordLabResultRepository medicalRecordLabResultRepository,
                      PerformedLabTestRepository performedLabTestRepository,
                      PatientRepository patientRepository,
                      DoctorRepository doctorRepository,
                      LabTechnicianRepository labTechnicianRepository) {
        this.labTestRepository = labTestRepository;
        this.labResultsRepository = labResultsRepository;
        this.medicalRecordRepository = medicalRecordRepository;
        this.medicalRecordLabResultRepository = medicalRecordLabResultRepository;
        this.performedLabTestRepository = performedLabTestRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.labTechnicianRepository = labTechnicianRepository;
    }

    // LAB TEST

    @Transactional
    public LabTests requestLabTest(String testName, String description,
                                   java.math.BigDecimal cost) {

        if (testName == null || testName.isBlank())
            throw new IllegalArgumentException("Lab test name is required");

        if (cost == null || cost.compareTo(java.math.BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("Invalid cost");

        LabTests labTest = new LabTests();
        labTest.setTestName(testName);
        labTest.setDescription(description);
        labTest.setCost(cost);

        return labTestRepository.save(labTest);
    }

    @Transactional(readOnly = true)
    public Optional<LabTests> getLabTestById(Long id) {

        if (id == null || id <= 0)
            throw new IllegalArgumentException("Invalid lab test ID");

        return labTestRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<LabTests> getAllLabTests() {
        return labTestRepository.findAll();
    }

    @Transactional
    public LabTests updateLabTest(Long id, String name,
                                  String description,
                                  java.math.BigDecimal cost) {

        LabTests test = labTestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lab test not found"));

        if (name != null && !name.isBlank())
            test.setTestName(name);

        if (description != null)
            test.setDescription(description);

        if (cost != null && cost.compareTo(java.math.BigDecimal.ZERO) >= 0)
            test.setCost(cost);

        return labTestRepository.save(test);
    }

    //  LAB TEST REQUESTS (UC013)

    @Transactional
    public PerformedLabTests requestLabTestForPatient(Long patientId,
                                                      Long doctorId,
                                                      Long testId,
                                                      LocalDate testDate,
                                                      String notes) {
        if (patientId == null || patientId <= 0)
            throw new IllegalArgumentException("Invalid patient ID");

        if (doctorId == null || doctorId <= 0)
            throw new IllegalArgumentException("Invalid doctor ID");

        if (testId == null || testId <= 0)
            throw new IllegalArgumentException("Invalid test ID");

        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        Doctors doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        LabTests test = labTestRepository.findById(testId)
                .orElseThrow(() -> new RuntimeException("Lab test not found"));

        PerformedLabTests performedTest = new PerformedLabTests();
        performedTest.setPatient(patient);
        performedTest.setDoctor(doctor);
        performedTest.setLabTest(test);
        performedTest.setTestDate(testDate != null ? testDate : LocalDate.now());
        performedTest.setNotes(notes);

        logger.info("Lab test {} requested for patient {} by doctor {}", testId, patientId, doctorId);
        return performedLabTestRepository.save(performedTest);
    }

    @Transactional(readOnly = true)
    public List<PerformedLabTests> getLabTestRequestsForPatient(Long patientId) {
        if (patientId == null || patientId <= 0)
            throw new IllegalArgumentException("Invalid patient ID");

        if (!patientRepository.existsById(patientId))
            throw new RuntimeException("Patient not found");

        return performedLabTestRepository.findByPatientPatientId(patientId);
    }

    @Transactional(readOnly = true)
    public List<PerformedLabTests> getLabTestRequestsByDoctor(Long doctorId) {
        if (doctorId == null || doctorId <= 0)
            throw new IllegalArgumentException("Invalid doctor ID");

        if (!doctorRepository.existsById(doctorId))
            throw new RuntimeException("Doctor not found");

        return performedLabTestRepository.findByDoctorDoctorId(doctorId);
    }

    //  LAB RESULTS (UC014, UC015)

    @Transactional
    public MedicalRecordLabResults storeLabResult(Long medicalRecordId,
                                                  Long testId,
                                                  String results,
                                                  LocalDate resultDate) {

        if (medicalRecordId == null || medicalRecordId <= 0)
            throw new IllegalArgumentException("Invalid medical record ID");

        if (testId == null || testId <= 0)
            throw new IllegalArgumentException("Invalid test ID");

        if (results == null || results.isBlank())
            throw new IllegalArgumentException("Results required");

        MedicalRecord medicalRecord = medicalRecordRepository.findById(medicalRecordId)
                .orElseThrow(() -> new RuntimeException("Medical record not found"));

        LabTests labTest = labTestRepository.findById(testId)
                .orElseThrow(() -> new RuntimeException("Lab test not found"));

        LabResults labResult = new LabResults();
        labResult.setResults(results);
        labResult.setResultDate(resultDate);
        labResult.setLabTest(labTest);

        LabResults saved = labResultsRepository.save(labResult);


        MedicalRecordLabResults link = new MedicalRecordLabResults();
        link.setMedicalRecord(medicalRecord);
        link.setLabResult(saved);

        logger.info("Stored lab result {} for medical record {}", saved.getResultId(), medicalRecordId);
        return medicalRecordLabResultRepository.save(link);
    }

    @Transactional(readOnly = true)
    public List<MedicalRecordLabResults> getLabResultsForMedicalRecord(Long medicalRecordId) {

        if (!medicalRecordRepository.existsById(medicalRecordId))
            throw new RuntimeException("Medical record not found");

        return medicalRecordLabResultRepository.findByMedicalRecordRecordId(medicalRecordId);
    }
}