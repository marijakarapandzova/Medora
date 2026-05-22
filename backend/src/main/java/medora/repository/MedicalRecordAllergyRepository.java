package medora.repository;

import medora.models.domain.MedicalRecordAllergies;
import medora.models.domain.id.MedicalRecordAllergyId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicalRecordAllergyRepository extends JpaRepository<MedicalRecordAllergies, MedicalRecordAllergyId> {

    List<MedicalRecordAllergies> findByMedicalRecordRecordId(Long medicalRecordId);

    boolean existsByMedicalRecordRecordIdAndAllergyAllergyId(Long medicalRecordId, Long allergyId);

    List<MedicalRecordAllergies> findByMedicalRecordRecordIdAndAllergyAllergyId(Long medicalRecordId, Long allergyId);
}

