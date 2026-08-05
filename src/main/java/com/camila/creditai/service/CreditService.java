package com.camila.creditai.service;

import com.camila.creditai.dto.request.CreditAnalysisRequest;
import com.camila.creditai.dto.response.CreditAnalysisResponse;
import com.camila.creditai.entity.CreditAnalysis;
import com.camila.creditai.enums.AnalysisStatus;
import com.camila.creditai.enums.IncomeType;
import com.camila.creditai.enums.RiskLevel;
import com.camila.creditai.repository.CreditAnalysisRepository;
import com.camila.creditai.util.CpfUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import lombok.extern.slf4j.Slf4j;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreditService {

    private final CreditAnalysisRepository repository;
    @Cacheable(value = "creditAnalysis", key = "#request.cpf", unless = "#result == null")
    @CacheEvict(value = "creditAnalysis", key = "#request.cpf", beforeInvocation = true)

    public CreditAnalysisResponse analyzeCredit(CreditAnalysisRequest request) {
        log.info("⚙️ Calculando score do CPF " + CpfUtils.mask(request.getCpf()) + " (CACHE NÃO ENCONTRADO)");

        // Cálculo do índice de endividamento
        double debtIncomeRatio = calculateDebtIncomeRatio(
                request.getMonthlyIncome(),
                request.getMonthlyDebt()
        );

        IncomeType incomeType = request.getIncomeType();

        // Score
        int score = calculateScore(
                request.getMonthlyIncome(),
                debtIncomeRatio,
                request.getEmploymentMonths(),
                request.getAge(),
                incomeType
        );

        RiskLevel riskLevel = determineRisk(score);
        AnalysisStatus status = determineStatus(score, debtIncomeRatio);
        String message = buildDecisionMessage(status, score);

        // Motivos e recomendações
        List<String> reasons = buildReasons(
                request.getMonthlyIncome(),
                debtIncomeRatio,
                request.getEmploymentMonths(),
                request.getAge(),
                status,
                incomeType
        );

        List<String> recommendations = generateRecommendations(
                status,
                debtIncomeRatio,
                score,
                request.getEmploymentMonths(),
                request.getAge(),
                request.getMonthlyIncome(),
                incomeType
        );

        // Cria e salva a entidade
        CreditAnalysis analysis = CreditAnalysis.builder()
                .cpf(request.getCpf())
                .monthlyIncome(request.getMonthlyIncome())
                .monthlyDebt(request.getMonthlyDebt())
                .employmentMonths(request.getEmploymentMonths())
                .age(request.getAge())
                .incomeType(incomeType)
                .score(score)
                .riskLevel(riskLevel)
                .status(status)
                .debtIncomeRatio(debtIncomeRatio)
                .build();

        CreditAnalysis saved = repository.save(analysis);

        // Constrói a resposta
        return CreditAnalysisResponse.builder()
                .id(saved.getId())
                .cpf(CpfUtils.mask(saved.getCpf()))
                .score(saved.getScore())
                .riskLevel(saved.getRiskLevel())
                .status(saved.getStatus())
                .monthlyIncome(saved.getMonthlyIncome())
                .monthlyDebt(saved.getMonthlyDebt())
                .debtIncomeRatio(saved.getDebtIncomeRatio())
                .message(message)
                .analyzedAt(saved.getAnalyzedAt())
                .reasons(reasons)
                .recommendations(recommendations)
                .build();
    }

    // ─── Motivos ──────────────────────────────
    private List<String> buildReasons(Double monthlyIncome,
                                      double debtIncomeRatio,
                                      Integer employmentMonths,
                                      Integer age,
                                      AnalysisStatus status,
                                      IncomeType incomeType) {
        List<String> reasons = new ArrayList<>();

        // Renda
        if (monthlyIncome >= 10000) {
            reasons.add("Renda mensal elevada, contribuindo positivamente para a capacidade de pagamento.");
        } else if (monthlyIncome >= 5000) {
            reasons.add("Renda mensal compatível com um perfil de risco moderado.");
        } else {
            reasons.add("Renda mensal abaixo das faixas mais favoráveis da análise.");
        }

        // Endividamento
        if (debtIncomeRatio <= 0.20) {
            reasons.add("Baixo comprometimento da renda com dívidas.");
        } else if (debtIncomeRatio <= 0.35) {
            reasons.add("Comprometimento da renda dentro de uma faixa aceitável.");
        } else if (debtIncomeRatio <= 0.50) {
            reasons.add("Comprometimento da renda considerado elevado.");
        } else {
            reasons.add("Comprometimento da renda considerado crítico.");
        }

        // Tipo de renda e estabilidade
        if (incomeType != null) {
            switch (incomeType) {
                case APOSENTADO:
                    reasons.add("Renda de aposentadoria garante estabilidade financeira comprovada.");
                    break;
                case RENDA_PASSIVA:
                    reasons.add("Renda passiva demonstra solidez financeira independente de vínculo empregatício.");
                    break;
                case EMPRESARIO:
                    reasons.add("Perfil empresarial considerado com estabilidade moderada.");
                    break;
                case CLT:
                case AUTONOMO:
                    if (employmentMonths != null) {
                        if (employmentMonths >= 60) {
                            reasons.add("Histórico de estabilidade financeira de longo prazo.");
                        } else if (employmentMonths >= 24) {
                            reasons.add("Histórico de estabilidade financeira adequado.");
                        } else {
                            reasons.add("Histórico de estabilidade financeira ainda limitado.");
                        }
                    } else {
                        reasons.add("Tempo de emprego não informado.");
                    }
                    break;
                default:
                    break;
            }
        }

        // Idade
        if (age != null && age >= 25 && age <= 55) {
            reasons.add("Faixa etária compatível com o perfil estatisticamente mais estável.");
        }

        // Status
        switch (status) {
            case APROVADO:
                reasons.add("Os critérios mínimos para aprovação automática foram atendidos.");
                break;
            case ANALISE_MANUAL:
                reasons.add("O perfil exige validações adicionais antes da decisão final.");
                break;
            case REPROVADO:
                reasons.add("Os critérios mínimos de aprovação não foram atingidos.");
                break;
        }

        return reasons;
    }

    // ─── Recomendações ────────────────────────
    private List<String> generateRecommendations(AnalysisStatus status,
                                                 double debtIncomeRatio,
                                                 int score,
                                                 Integer employmentMonths,
                                                 Integer age,
                                                 double monthlyIncome,
                                                 IncomeType incomeType) {
        List<String> recommendations = new ArrayList<>();

        if (status == AnalysisStatus.APROVADO) {
            recommendations.add("Cliente elegível para oferta de crédito pessoal.");
            if (score >= 800) {
                recommendations.add("Score excelente — pode ser elegível para crédito premium.");
            } else {
                recommendations.add("Possível aumento de limite após 6 meses de histórico positivo.");
            }
            if (debtIncomeRatio < 0.20) {
                recommendations.add("Baixo comprometimento de renda — perfil ideal para investimentos.");
            }
        }

        if (status == AnalysisStatus.ANALISE_MANUAL) {
            recommendations.add("Análise manual necessária — perfil com fatores mistos.");
            if (debtIncomeRatio >= 0.30) {
                recommendations.add("Comprometimento de renda de "
                        + String.format("%.0f%%", debtIncomeRatio * 100)
                        + " — recomenda-se reduzir dívidas antes de nova solicitação.");
            }
            if (employmentMonths != null && employmentMonths < 12) {
                recommendations.add("Tempo de emprego inferior a 12 meses reduz a confiabilidade da análise.");
            }
            if (score < 500) {
                recommendations.add("Score abaixo de 500 — apresentar comprovantes adicionais de renda.");
            }
            recommendations.add("A apresentação de comprovantes complementares pode agilizar a análise.");
        }

        if (status == AnalysisStatus.REPROVADO) {
            if (debtIncomeRatio > 0.40) {
                recommendations.add("Comprometimento de renda de "
                        + String.format("%.0f%%", debtIncomeRatio * 100)
                        + " acima do limite de 40% — priorize quitar dívidas existentes.");
            }
            if (score < 300) {
                recommendations.add("Score muito baixo (" + score + ") — histórico de inadimplência pode estar impactando.");
            }
            if (employmentMonths != null && employmentMonths < 6) {
                recommendations.add("Menos de 6 meses de emprego — aguarde estabilidade profissional mínima.");
            }
            if (age != null && age < 21) {
                recommendations.add("Perfil jovem sem histórico de crédito — considere começar com crédito consignado.");
            }
            if (monthlyIncome < 3000) {
                recommendations.add("Aumentar a capacidade de renda pode melhorar futuras avaliações.");
            }
            recommendations.add("Nova análise pode ser solicitada após 90 dias com melhora no perfil.");
        }

        return recommendations;
    }

    // ─── Cálculos ──────────────────────────────
    private double calculateDebtIncomeRatio(Double monthlyIncome, Double monthlyDebt) {
        if (monthlyIncome == null || monthlyIncome <= 0) return 1.0;
        double debt = (monthlyDebt != null) ? monthlyDebt : 0.0;
        return debt / monthlyIncome;
    }

    private int calculateScore(Double monthlyIncome,
                               double debtIncomeRatio,
                               Integer employmentMonths,
                               Integer age,
                               IncomeType incomeType) {
        int score = 0;

        // Renda (máx 400)
        if (monthlyIncome != null) {
            if (monthlyIncome >= 10000) score += 400;
            else if (monthlyIncome >= 7000) score += 320;
            else if (monthlyIncome >= 5000) score += 250;
            else if (monthlyIncome >= 3000) score += 150;
            else score += 50;
        }

        // Endividamento (máx 300)
        if (debtIncomeRatio <= 0.20) score += 300;
        else if (debtIncomeRatio <= 0.35) score += 220;
        else if (debtIncomeRatio <= 0.50) score += 120;

        // Estabilidade (máx 200)
        score += calculateStabilityScore(incomeType, employmentMonths);

        // Idade (máx 100)
        if (age != null) {
            if (age >= 25 && age <= 55) score += 100;
            else if ((age >= 21 && age <= 24) || (age >= 56 && age <= 65)) score += 60;
            else score += 20;
        }

        return Math.min(score, 1000);
    }

    private int calculateStabilityScore(IncomeType incomeType, Integer employmentMonths) {
        if (incomeType == null) return 0;
        return switch (incomeType) {
            case APOSENTADO, RENDA_PASSIVA -> 200;
            case EMPRESARIO -> 150;
            case CLT, AUTONOMO -> {
                if (employmentMonths == null) yield 0;
                if (employmentMonths >= 60) yield 200;
                if (employmentMonths >= 36) yield 150;
                if (employmentMonths >= 24) yield 100;
                if (employmentMonths >= 12) yield 50;
                yield 0;
            }
        };
    }

    private RiskLevel determineRisk(int score) {
        if (score >= 800) return RiskLevel.BAIXO;
        if (score >= 650) return RiskLevel.MEDIO;
        if (score >= 500) return RiskLevel.ALTO;
        return RiskLevel.MUITO_ALTO;
    }

    private AnalysisStatus determineStatus(int score, double debtIncomeRatio) {
        if (debtIncomeRatio > 0.60) return AnalysisStatus.REPROVADO;
        if (debtIncomeRatio > 0.50) return AnalysisStatus.ANALISE_MANUAL;
        if (score >= 750) return AnalysisStatus.APROVADO;
        if (score >= 550) return AnalysisStatus.ANALISE_MANUAL;
        return AnalysisStatus.REPROVADO;
    }

    private String buildDecisionMessage(AnalysisStatus status, int score) {
        return switch (status) {
            case APROVADO -> "Crédito aprovado. Score calculado: " + score;
            case ANALISE_MANUAL -> "Solicitação encaminhada para análise manual. Score calculado: " + score;
            case REPROVADO -> "Crédito reprovado. Score calculado: " + score;
        };
    }
}