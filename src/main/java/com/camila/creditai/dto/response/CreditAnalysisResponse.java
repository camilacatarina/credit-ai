package com.camila.creditai.dto.response;

import com.camila.creditai.enums.AnalysisStatus;
import com.camila.creditai.enums.RiskLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditAnalysisResponse {
    private Long id;
    private String cpf;
    private Integer score;
    private RiskLevel riskLevel;
    private AnalysisStatus status;
    private Double monthlyIncome;
    private Double monthlyDebt;
    private Double debtIncomeRatio;
    private String message;
    private LocalDateTime analyzedAt;
}