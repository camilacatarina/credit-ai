package com.camila.creditai.controller;

import com.camila.creditai.dto.request.CreditAnalysisRequest;
import com.camila.creditai.dto.response.CreditAnalysisResponse;
import com.camila.creditai.service.CreditService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/credit")
@RequiredArgsConstructor
public class CreditController {

    private final CreditService creditService;

    @PostMapping("/analyze")
    public ResponseEntity<CreditAnalysisResponse> analyzeCredit(
            @Valid @RequestBody CreditAnalysisRequest request) {

        CreditAnalysisResponse response =
                creditService.analyzeCredit(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}