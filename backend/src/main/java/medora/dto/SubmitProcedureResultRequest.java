package medora.dto;

import java.time.LocalDate;

public class SubmitProcedureResultRequest {
    private Long medicalRecordId;
    private Long procedureId;
    private String resultDescription;
    private LocalDate resultDate;

    public SubmitProcedureResultRequest() {}

    public SubmitProcedureResultRequest(Long medicalRecordId, Long procedureId, String resultDescription, LocalDate resultDate) {
        this.medicalRecordId = medicalRecordId;
        this.procedureId = procedureId;
        this.resultDescription = resultDescription;
        this.resultDate = resultDate;
    }

    public Long getMedicalRecordId() {
        return medicalRecordId;
    }

    public void setMedicalRecordId(Long medicalRecordId) {
        this.medicalRecordId = medicalRecordId;
    }

    public Long getProcedureId() {
        return procedureId;
    }

    public void setProcedureId(Long procedureId) {
        this.procedureId = procedureId;
    }

    public String getResultDescription() {
        return resultDescription;
    }

    public void setResultDescription(String resultDescription) {
        this.resultDescription = resultDescription;
    }

    public LocalDate getResultDate() {
        return resultDate;
    }

    public void setResultDate(LocalDate resultDate) {
        this.resultDate = resultDate;
    }
}
