package medora.repository;

import medora.models.domain.Procedure;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcedureRepository extends JpaRepository<Procedure, Long> {
}

