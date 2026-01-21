package com.example.UnityTrustBank.Entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "upi_usage", 
       uniqueConstraints = {@UniqueConstraint(columnNames = {"user_id", "date"})},
       indexes = {@Index(name = "idx_upi_usage_user_date", columnList = "user_id, date")})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpiUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private BigDecimal totalUsed = BigDecimal.ZERO;
}
