package com.camila.creditai.repository;

import com.camila.creditai.entity.CreditAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CreditAnalysisRepository extends JpaRepository<CreditAnalysis, Long> {

    List<CreditAnalysis> findByCpfOrderByAnalyzedAtDesc(String cpf);

    Optional<CreditAnalysis> findFirstByCpfOrderByAnalyzedAtDesc(String cpf);
}