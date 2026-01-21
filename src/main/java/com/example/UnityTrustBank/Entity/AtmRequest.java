package com.example.UnityTrustBank.Entity;

import java.time.LocalDateTime;

import com.example.UnityTrustBank.Enum.Request;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AtmRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private Request status;

    private LocalDateTime requestDate;
    private LocalDateTime approvedDate;
    @ManyToOne(optional = false)
    private User requestedBy;

    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account account;


    @ManyToOne
    private User approvedBy;
    private String rejectionReason;
	
}
