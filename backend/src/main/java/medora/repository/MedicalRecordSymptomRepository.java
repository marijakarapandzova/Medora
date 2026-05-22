package medora.repository;

import medora.models.domain.MedicalRecordSymptoms;
import medora.models.domain.id.MedicalRecordSymptomId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicalRecordSymptomRepository extends JpaRepository<MedicalRecordSymptoms, MedicalRecordSymptomId> {

    List<MedicalRecordSymptoms> findByMedicalRecordRecordId(Long medicalRecordId);

    boolean existsByMedicalRecordRecordIdAndSymptomSymptomId(Long medicalRecordId, Long symptomId);

    List<MedicalRecordSymptoms> findByMedicalRecordRecordIdAndSymptomSymptomId(Long medicalRecordId, Long symptomId);
}

