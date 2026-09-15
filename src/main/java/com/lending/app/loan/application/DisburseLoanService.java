package com.lending.app.loan.application;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.lending.app.customer.domain.Customer;
import com.lending.app.customer.domain.CustomerStatus;
import com.lending.app.loan.domain.Loan;
import com.lending.app.loan.domain.LoanFee;
import com.lending.app.loan.domain.LoanStatus;
import com.lending.app.loan.domain.LoanTermFee;
import com.lending.app.loan.domain.LoanTerms;
import com.lending.app.loan.repository.LoanFeeRepository;
import com.lending.app.loan.repository.LoanRepository;
import com.lending.app.loan.repository.LoanTermFeeRepository;
import com.lending.app.loan.repository.LoanTermsRepository;
import com.lending.app.product.domain.FeeApplicationTiming;
import com.lending.app.product.domain.LoanProduct;
import com.lending.app.product.domain.ProductFee;
import com.lending.app.product.domain.ProductStatus;
import com.lending.app.product.repository.ProductFeeRepository;
import com.lending.app.repayment_schedule.application.CreateRepaymentScheduleService;
import com.lending.app.shared.exception.ResourceNotFoundException;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class DisburseLoanService {

    private final LoanRepository loanRepository;
    private final ProductFeeRepository productFeeRepository;
    private final LoanTermsRepository loanTermsRepository;
    private final LoanTermFeeRepository loanTermFeeRepository;
    private final LoanFeeRepository loanFeeRepository;
    private final LoanMaturityCalculator maturityCalculator;
    private final LoanFeeCalculator feeCalculator;
    private final CreateRepaymentScheduleService createRepaymentScheduleService;

    public DisburseLoanService(
            LoanRepository loanRepository,
            ProductFeeRepository productFeeRepository,
            LoanTermsRepository loanTermsRepository,
            LoanTermFeeRepository loanTermFeeRepository,
            LoanFeeRepository loanFeeRepository,
            LoanMaturityCalculator maturityCalculator,
            LoanFeeCalculator feeCalculator,
            CreateRepaymentScheduleService createRepaymentScheduleService) {
        this.loanRepository = loanRepository;
        this.productFeeRepository = productFeeRepository;
        this.loanTermsRepository = loanTermsRepository;
        this.loanTermFeeRepository = loanTermFeeRepository;
        this.loanFeeRepository = loanFeeRepository;
        this.maturityCalculator = maturityCalculator;
        this.feeCalculator = feeCalculator;
        this.createRepaymentScheduleService = createRepaymentScheduleService;
    }

    public Loan execute(UUID loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found: " + loanId));

        if (loan.getStatus() != LoanStatus.CREATED) {
            throw new IllegalStateException("Only CREATED loans can be disbursed");
        }

        Customer customer = loan.getCustomer();

        if (customer.getStatus() != CustomerStatus.ACTIVE) {
            throw new IllegalStateException("Customer is not active");
        }

        LoanProduct product = loan.getProduct();

        if (product.getStatus() != ProductStatus.ACTIVE) {
            throw new IllegalStateException("Loan product is not active");
        }

        LocalDate disbursementDate = LocalDate.now();

        LoanTerms terms = new LoanTerms(loan, product.getTenureValue(), product.getTenureUnit(), product.getStructure(),
                product.getBillingMode(), product.getBillingDay(), product.getGracePeriodDays());

        loanTermsRepository.save(terms);

        List<ProductFee> productFees = productFeeRepository.findByProductId(product.getId());

        for (ProductFee productFee : productFees) {
            LoanTermFee termFee = new LoanTermFee(terms, productFee.getFeeType(), productFee.getCalculationType(),
                    productFee.getValue(), productFee.getApplicationTiming(), productFee.getTriggerDays());

            loanTermFeeRepository.save(termFee);
        }

        LocalDate maturityDate = maturityCalculator.calculate(disbursementDate, terms.getTenureValue(),
                terms.getTenureUnit());

        loan.disburse(disbursementDate, maturityDate);

        createRepaymentScheduleService.execute(loan, terms);

        for (LoanTermFee termFee : getTermFees(terms.getId())) {

            if (termFee.getApplicationTiming() == FeeApplicationTiming.ORIGINAL) {
                BigDecimal amount = feeCalculator.calculate(termFee, loan.getPrincipal());

                LoanFee loanFee = new LoanFee(loan, termFee.getFeeType(), amount, disbursementDate, maturityDate, null,
                        "Original Fee");

                loanFeeRepository.save(loanFee);
            }
        }

        return loanRepository.save(loan);
    }

    private List<LoanTermFee> getTermFees(UUID loanTermsId) {
        return loanTermFeeRepository.findByLoanTermsId(loanTermsId);
    }
}
