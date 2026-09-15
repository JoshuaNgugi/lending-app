package com.lending.app.repayment_schedule.application;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.lending.app.loan.domain.Loan;
import com.lending.app.loan.domain.LoanTerms;
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
        int numberOfInstallments = terms.getInstallmentCount();

        BigDecimal principal = loan.getPrincipal();

        BigDecimal baseAmount = principal.divide(BigDecimal.valueOf(numberOfInstallments), 2, RoundingMode.DOWN);

        BigDecimal totalBase = baseAmount.multiply(BigDecimal.valueOf(numberOfInstallments));

        BigDecimal finalAmount = principal.subtract(totalBase);

        List<InstallmentData> installments = new ArrayList<>();

        for (int i = 0; i <= numberOfInstallments; i++) {
            BigDecimal amount = i == numberOfInstallments ? finalAmount : baseAmount;

            LocalDate duDate = calculateDueDate(loan, terms, i, numberOfInstallments);

            installments.add(new InstallmentData(i, duDate, amount));
        }
        return installments;
    }

    private LocalDate calculateDueDate(Loan loan, LoanTerms loanTerms, int installmentNumber, int totalInstallments) {
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
}
