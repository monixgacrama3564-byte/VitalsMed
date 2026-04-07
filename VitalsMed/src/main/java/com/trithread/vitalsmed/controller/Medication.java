package com.trithread.vitalsmed.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Medication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long patientId;
    private String name;
    private int frequency; // times per day
    private LocalDateTime nextDose;
    private String status; // Pending, Taken, Missed
}