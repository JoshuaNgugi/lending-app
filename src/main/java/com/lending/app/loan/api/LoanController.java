package com.lending.app.loan.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lending.app.loan.application.CreateLoanService;
import com.lending.app.loan.domain.Loan;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/loans")
public class LoanController {

    private final CreateLoanService createLoanService;

    public LoanController(CreateLoanService createLoanService) {
        this.createLoanService = createLoanService;
    }

    @PostMapping
    public ResponseEntity<LoanResponse> create(
            @Valid @RequestBody CreateLoanRequest request) {

        Loan loan = createLoanService.execute(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(toResponse(loan));
    }

    private LoanResponse toResponse(Loan loan) {

        return new LoanResponse(
                loan.getId(),
                loan.getCustomer().getId(),
                loan.getProduct().getId(),
                loan.getPrincipal(),
                loan.getStatus(),
                loan.getOriginationDate(),
                loan.getDisbursementDate(),
                loan.getMaturityDate());
    }
}
