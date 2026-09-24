package medora.controller;

import medora.dto.LabTechnicianDTO;
import medora.models.domain.LabTechnician;
import medora.repository.LabTechnicianRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/lab-technicians")
public class LabTechnicianController {

    private static final Logger logger = LoggerFactory.getLogger(LabTechnicianController.class);

    private final LabTechnicianRepository labTechnicianRepository;

    public LabTechnicianController(LabTechnicianRepository labTechnicianRepository) {
        this.labTechnicianRepository = labTechnicianRepository;
    }

    @GetMapping
    public ResponseEntity<?> getAllLabTechnicians() {
        try {
            List<LabTechnicianDTO> technicians = labTechnicianRepository.findAll().stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(technicians);
        } catch (Exception e) {
            logger.error("Error fetching lab technicians: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to fetch lab technicians: " + e.getMessage()));
        }
    }

    private LabTechnicianDTO convertToDTO(LabTechnician technician) {
        return new LabTechnicianDTO(
                technician.getTechnicianId(),
                technician.getName(),
                technician.getLastname(),
                technician.getUsername()
        );
    }
}