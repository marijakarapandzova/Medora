package medora.repository;

import medora.models.domain.LabTechnician;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LabTechnicianRepository extends JpaRepository<LabTechnician, Long> {
}
