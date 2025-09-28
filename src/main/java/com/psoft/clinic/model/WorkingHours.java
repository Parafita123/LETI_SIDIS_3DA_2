package com.psoft.clinic.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalTime;

@Embeddable
@NoArgsConstructor
@Data
public class WorkingHours {

    @Getter
    @Setter
    @Column(name = "start_time", nullable = false)
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime startTime;

    @Getter
    @Setter
    @Column(name = "end_time", nullable = false)
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime endTime;
}
