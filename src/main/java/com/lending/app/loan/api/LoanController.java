package com.lending.app.loan.api;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lending.app.loan.application.CreateLoanService;
import com.lending.app.loan.application.CancelLoanService;
import com.lending.app.loan.application.DisburseLoanService;
import com.lending.app.loan.domain.Loan;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/loans")
public class LoanController {

    private final CreateLoanService createLoanService;
    private final CancelLoanService cancelLoanService;
    private final DisburseLoanService disburseLoanService;

    public LoanController(CreateLoanService createLoanService, CancelLoanService cancelLoanService,
            DisburseLoanService disburseLoanService) {
        this.createLoanService = createLoanService;
        this.cancelLoanService = cancelLoanService;
        this.disburseLoanService = disburseLoanService;
    }

    @PostMapping
    public ResponseEntity<LoanResponse> create(
            @Valid @RequestBody CreateLoanRequest request) {

        Loan loan = createLoanService.execute(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(toResponse(loan));
    }

    @PostMapping("/{loanId}/disburse")
    public LoanResponse disburse(@PathVariable("loanId") UUID loanId) {

        Loan loan = disburseLoanService.execute(loanId);

        return toResponse(loan);
    }

    @PostMapping("/{loanId}/cancel")
    public LoanResponse cancel(@PathVariable("loanId") UUID loanId) {

        Loan loan = cancelLoanService.execute(loanId);

        return toResponse(loan);
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
