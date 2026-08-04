package com.camila.creditai.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import com.camila.creditai.enums.IncomeType;

@Data
public class CreditAnalysisRequest {

    @NotBlank(message = "CPF é obrigatório")
    @Pattern(regexp = "\\d{11}", message = "CPF deve conter 11 dígitos numéricos")
    private String cpf;

    @NotNull(message = "Renda mensal é obrigatória")
    @Positive(message = "Renda mensal deve ser positiva")
    @DecimalMax(value = "1000000.00")
    private Double monthlyIncome;

    @NotNull(message = "Dívida mensal é obrigatória")
    @PositiveOrZero(message = "Dívida mensal não pode ser negativa")
    @DecimalMax(value = "1000000.00")
    private Double monthlyDebt;

    @NotNull(message = "Tipo de renda é obrigatório")
    private IncomeType incomeType;

    @PositiveOrZero(message = "Meses de emprego não pode ser negativo")
    private Integer employmentMonths;

    @NotNull(message = "Idade é obrigatória")
    @Min(value = 18, message = "Idade mínima é 18 anos")
    @Max(value = 100, message = "Idade máxima é 100 anos")
    private Integer age;

    @AssertTrue(message = "Meses de emprego é obrigatório para CLT ou Autônomo")
    public boolean isEmploymentMonthsValid() {
        if (incomeType == IncomeType.CLT || incomeType == IncomeType.AUTONOMO) {
            return employmentMonths != null && employmentMonths >= 0;
        }
        return true;
    }

    @AssertTrue(message = "Dívida mensal não pode ser maior que a renda mensal")
    public boolean isDebtValid() {
        if (monthlyIncome == null || monthlyDebt == null) return true;
        return monthlyDebt <= monthlyIncome;
    }
}