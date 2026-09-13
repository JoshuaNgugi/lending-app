package com.lending.app.product.api;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lending.app.product.application.CreateLoanProductService;
import com.lending.app.product.application.GetLoanProductService;
import com.lending.app.product.domain.LoanProduct;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/products")
public class LoanProductController {
    private final GetLoanProductService getLoanProductService;
    private final CreateLoanProductService createLoanProductService;

    public LoanProductController(GetLoanProductService getLoanProductService,
            CreateLoanProductService createLoanProductService) {
        this.getLoanProductService = getLoanProductService;
        this.createLoanProductService = createLoanProductService;

    }

    @GetMapping("/{productId}")
    public LoanProductResponse get(@PathVariable("productId") UUID id) {
        LoanProduct product = getLoanProductService.execute(id);
        return toResponse(product);
    }

    @GetMapping
    public List<LoanProductResponse> getAll() {
        return getLoanProductService.executeAll().stream().map(this::toResponse).toList();
    }

    @PostMapping
    public ResponseEntity<LoanProductResponse> create(
            @Valid @RequestBody CreateLoanProductRequest request) {

        LoanProduct product = createLoanProductService.execute(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(product));
    }

    private LoanProductResponse toResponse(LoanProduct product) {
        return new LoanProductResponse(product.getId(), product.getCode(), product.getName(),
                product.getDescription(), product.getStatus(), product.getTenureValue(), product.getTenureUnit(),
                product.getStructure(), product.getBillingMode(), product.getBillingDay(),
                product.getGracePeriodDays());
    }

}
