package com.lending.app.repayment.repayment;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lending.app.product.domain.ProcessRepaymentService;
import com.lending.app.repayment.api.CreateRepaymentRequest;
import com.lending.app.repayment.api.RepaymentAllocationResponse;
import com.lending.app.repayment.api.RepaymentResponse;
import com.lending.app.repayment.domain.Repayment;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/loans/{loanId}/repayments")
public class RepaymentController {

    private final ProcessRepaymentService processRepaymentService;
    private final RepaymentAllocationRepository repaymentAllocationRepository;

    public RepaymentController(
            ProcessRepaymentService processRepaymentService,
            RepaymentAllocationRepository repaymentAllocationRepository) {
        this.processRepaymentService = processRepaymentService;
        this.repaymentAllocationRepository = repaymentAllocationRepository;
    }

    @PostMapping
    public ResponseEntity<RepaymentResponse> create(
            @PathVariable("loanId") UUID loanId,
            @Valid @RequestBody CreateRepaymentRequest request) {
        Repayment repayment = processRepaymentService.execute(
                loanId,
                request.amount(),
                request.reference(),
                request.channel());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(toResponse(repayment));
    }

    private RepaymentResponse toResponse(Repayment repayment) {

        List<RepaymentAllocationResponse> allocations = repaymentAllocationRepository
                .findByRepaymentId(repayment.getId())
                .stream()
                .map(allocation -> new RepaymentAllocationResponse(
                        allocation.getId(),
                        allocation.getLoanFee() != null
                                ? allocation.getLoanFee().getId()
                                : null,
                        allocation.getInstallment() != null
                                ? allocation.getInstallment().getId()
                                : null,
                        allocation.getAmount()))
                .toList();

        return new RepaymentResponse(
                repayment.getId(),
                repayment.getLoan().getId(),
                repayment.getAmount(),
                repayment.getPaymentDate(),
                repayment.getReference(),
                repayment.getChannel(),
                allocations);
    }
}
