package com.lending.app.repayment_schedule.application;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.lending.app.loan.domain.Loan;
import com.lending.app.loan.domain.LoanTerms;
import com.lending.app.product.domain.BillingMode;
import com.lending.app.product.domain.LoanStructure;
import com.lending.app.product.domain.TenureUnit;
import com.lending.app.repayment_schedule.domain.InstallmentData;

@Component
public class RepaymentScheduleGenerator {

    public List<InstallmentData> generate(Loan loan, LoanTerms terms) {

        if (terms.getStructure() == LoanStructure.LUMP_SUM) {
            return generateLumpSum(loan);
        }

        return generateInstallments(loan, terms);
    }

    private List<InstallmentData> generateLumpSum(Loan loan) {
        return List.of(
                new InstallmentData(
                        1, loan.getMaturityDate(), loan.getPrincipal()));
    }

    private List<InstallmentData> generateInstallments(Loan loan, LoanTerms terms) {
        Integer installmentCount = terms.getInstallmentCount();

        if (terms.getStructure() == LoanStructure.INSTALLMENT && installmentCount == null) {
            throw new IllegalStateException("Installment count is required for installment loans");
        }

        int numberOfInstallments = terms.getStructure() == LoanStructure.LUMP_SUM ? 1 : installmentCount;

        BigDecimal principal = loan.getPrincipal();

        BigDecimal baseAmount = principal.divide(BigDecimal.valueOf(numberOfInstallments), 2, RoundingMode.DOWN);

        BigDecimal totalBase = baseAmount.multiply(BigDecimal.valueOf(numberOfInstallments));

        BigDecimal finalAmount = baseAmount.add(principal.subtract(totalBase));

        List<InstallmentData> installments = new ArrayList<>();

        for (int i = 1; i <= numberOfInstallments; i++) {
            BigDecimal amount = i == numberOfInstallments ? finalAmount : baseAmount;

            LocalDate duDate = calculateDueDate(loan, terms, i, numberOfInstallments);

            installments.add(new InstallmentData(i, duDate, amount));
        }
        return installments;
    }

    private LocalDate calculateDueDate(Loan loan, LoanTerms loanTerms, int installmentNumber, int totalInstallments) {
        if (loanTerms.getBillingMode() == BillingMode.CONSOLIDATED) {
            return calculateConsolidatedDueDate(loan, loanTerms, installmentNumber);
        }

        if (installmentNumber == totalInstallments) {
            return loan.getMaturityDate();
        }

        if (loanTerms.getTenureUnit() == TenureUnit.MONTHS) {
            long monthsBetween = loanTerms.getTenureValue() / totalInstallments;

            return loan.getDisbursementDate().plusMonths(monthsBetween * installmentNumber);
        }

        long daysBetween = loanTerms.getTenureValue() / totalInstallments;

        return loan.getDisbursementDate().plusDays(daysBetween * installmentNumber);
    }

    private LocalDate calculateConsolidatedDueDate(Loan loan, LoanTerms terms, int installmentNumber) {
        Integer billingDay = terms.getBillingDay();

        if (billingDay == null) {
            throw new IllegalStateException("Billing day is required for consolidated billing");
        }

        LocalDate disbursementDate = loan.getDisbursementDate();
        YearMonth billingMonth = YearMonth.from(disbursementDate);
        LocalDate firstBillingDate = billingMonth.atDay(billingDay);

        if (!firstBillingDate.isAfter(disbursementDate)) {
            firstBillingDate = billingMonth.plusMonths(1).atDay(billingDay);
        }

        LocalDate dueDate = firstBillingDate.plusMonths(installmentNumber - 1L);

        return dueDate.isAfter(loan.getMaturityDate())
                ? loan.getMaturityDate()
                : dueDate;
    }
}
