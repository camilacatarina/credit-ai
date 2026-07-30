package com.camila.creditai.entity;

import com.camila.creditai.enums.AnalysisStatus;
import com.camila.creditai.enums.RiskLevel;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "credit_analysis")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreditAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String cpf;

    @Column(nullable = false)
    private Double monthlyIncome;

    @Column(nullable = false)
    private Double monthlyDebt;

    @Column(nullable = false)
    private Double debtIncomeRatio;

    @Column(nullable = false)
    private Integer employmentMonths;

    @Column(nullable = false)
    private Integer age;

    @Column(nullable = false)
    private Integer score;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RiskLevel riskLevel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AnalysisStatus status;

    @Column(nullable = false)
    private LocalDateTime analyzedAt;

    @PrePersist
    public void prePersist() {
        this.analyzedAt = LocalDateTime.now();
    }
}