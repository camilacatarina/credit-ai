package com.camila.creditai.service;

import com.camila.creditai.dto.request.CreditAnalysisRequest;
import com.camila.creditai.dto.response.CreditAnalysisResponse;
import com.camila.creditai.entity.CreditAnalysis;
import com.camila.creditai.enums.AnalysisStatus;
import com.camila.creditai.enums.RiskLevel;
import com.camila.creditai.repository.CreditAnalysisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreditService {

    private final CreditAnalysisRepository repository;

    public CreditAnalysisResponse analyzeCredit(CreditAnalysisRequest request) {

        double debtIncomeRatio = calculateDebtIncomeRatio(
                request.getMonthlyIncome(),
                request.getMonthlyDebt()
        );

        int score = calculateScore(
                request.getMonthlyIncome(),
                debtIncomeRatio,
                request.getEmploymentMonths(),
                request.getAge()
        );

        RiskLevel riskLevel = determineRisk(score);

        AnalysisStatus status = determineStatus(
                score,
                debtIncomeRatio
        );

        String message = buildDecisionMessage(
                status,
                score
        );

        CreditAnalysis analysis = CreditAnalysis.builder()
                .cpf(request.getCpf())
                .monthlyIncome(request.getMonthlyIncome())
                .monthlyDebt(request.getMonthlyDebt())
                .employmentMonths(request.getEmploymentMonths())
                .age(request.getAge())
                .score(score)
                .riskLevel(riskLevel)
                .status(status)
                .debtIncomeRatio(debtIncomeRatio)
                .build();

        CreditAnalysis savedAnalysis = repository.save(analysis);

        return CreditAnalysisResponse.builder()
                .id(savedAnalysis.getId())
                .cpf(savedAnalysis.getCpf())
                .score(savedAnalysis.getScore())
                .riskLevel(savedAnalysis.getRiskLevel())
                .status(savedAnalysis.getStatus())
                .monthlyIncome(savedAnalysis.getMonthlyIncome())
                .monthlyDebt(savedAnalysis.getMonthlyDebt())
                .debtIncomeRatio(savedAnalysis.getDebtIncomeRatio())
                .message(message)
                .analyzedAt(savedAnalysis.getAnalyzedAt())
                .build();
    }

    private double calculateDebtIncomeRatio(
            Double monthlyIncome,
            Double monthlyDebt) {

        if (monthlyIncome == null || monthlyIncome <= 0) {
            return 1.0;
        }

        return monthlyDebt / monthlyIncome;
    }

    private int calculateScore(
            Double monthlyIncome,
            double debtIncomeRatio,
            Integer employmentMonths,
            Integer age) {

        int score = 0;

        // Renda (até 400)

        if (monthlyIncome >= 10000) {
            score += 400;
        } else if (monthlyIncome >= 7000) {
            score += 320;
        } else if (monthlyIncome >= 5000) {
            score += 250;
        } else if (monthlyIncome >= 3000) {
            score += 150;
        } else {
            score += 50;
        }

        // Endividamento (até 300)

        if (debtIncomeRatio <= 0.20) {
            score += 300;
        } else if (debtIncomeRatio <= 0.35) {
            score += 220;
        } else if (debtIncomeRatio <= 0.50) {
            score += 120;
        }

        // Tempo de emprego (até 200)

        if (employmentMonths >= 60) {
            score += 200;
        } else if (employmentMonths >= 36) {
            score += 150;
        } else if (employmentMonths >= 24) {
            score += 100;
        } else if (employmentMonths >= 12) {
            score += 50;
        }

        // Idade (até 100)

        if (age >= 25 && age <= 55) {
            score += 100;
        } else if ((age >= 21 && age <= 24)
                || (age >= 56 && age <= 65)) {
            score += 60;
        } else {
            score += 20;
        }

        return Math.min(score, 1000);
    }

    private RiskLevel determineRisk(int score) {

        if (score >= 800) {
            return RiskLevel.BAIXO;
        }

        if (score >= 650) {
            return RiskLevel.MEDIO;
        }

        if (score >= 500) {
            return RiskLevel.ALTO;
        }

        return RiskLevel.MUITO_ALTO;
    }

    private AnalysisStatus determineStatus(
            int score,
            double debtIncomeRatio) {

        if (debtIncomeRatio > 0.60) {
            return AnalysisStatus.REPROVADO;
        }

        if (score >= 750) {
            return AnalysisStatus.APROVADO;
        }

        if (score >= 550) {
            return AnalysisStatus.ANALISE_MANUAL;
        }

        return AnalysisStatus.REPROVADO;
    }

    private String buildDecisionMessage(
            AnalysisStatus status,
            int score) {

        return switch (status) {
            case APROVADO ->
                    "Crédito aprovado. Score calculado: " + score;

            case ANALISE_MANUAL ->
                    "Solicitação encaminhada para análise manual. Score calculado: " + score;

            case REPROVADO ->
                    "Crédito reprovado. Score calculado: " + score;
        };
    }
}