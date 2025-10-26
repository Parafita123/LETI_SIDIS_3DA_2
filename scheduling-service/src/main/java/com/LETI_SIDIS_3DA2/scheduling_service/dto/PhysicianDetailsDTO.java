package  com.LETI_SIDIS_3DA2.scheduling_service.dto;
import java.time.LocalTime;
// Este DTO representa os dados que recebemos do Physician Service.
// Os nomes dos campos devem corresponder aos do JSON retornado pelo Physician Service.
public class PhysicianDetailsDTO {
    private Long id;
    private String fullName;
    private LocalTime workStartTime;
    private LocalTime workEndTime;

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public LocalTime getWorkStartTime() { return workStartTime; }
    public void setWorkStartTime(LocalTime workStartTime) { this.workStartTime = workStartTime; }
    public LocalTime getWorkEndTime() { return workEndTime; }
    public void setWorkEndTime(LocalTime workEndTime) { this.workEndTime = workEndTime; }
}