package com.lending.app;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.lending.app.loan.domain.Loan;
import com.lending.app.loan.domain.LoanTerms;
import com.lending.app.product.domain.BillingMode;
import com.lending.app.product.domain.LoanStructure;
import com.lending.app.product.domain.TenureUnit;
import com.lending.app.repayment_schedule.application.RepaymentScheduleGenerator;
import com.lending.app.repayment_schedule.domain.InstallmentData;

class RepaymentScheduleGeneratorTest {

    private final RepaymentScheduleGenerator generator = new RepaymentScheduleGenerator();

    @Test
    void consolidatedBillingUsesConfiguredBillingDayForEachInstallment() {
        Loan loan = createLoan(LocalDate.of(2026, 9, 20), LocalDate.of(2026, 12, 20));
        LoanTerms terms = createTerms(loan, BillingMode.CONSOLIDATED, 5);

        List<InstallmentData> schedule = generator.generate(loan, terms);

        assertEquals(
                List.of(
                        LocalDate.of(2026, 10, 5),
                        LocalDate.of(2026, 11, 5),
                        LocalDate.of(2026, 12, 5)),
                schedule.stream().map(InstallmentData::duDate).toList());
    }

    @Test
    void consolidatedBillingUsesCurrentMonthWhenBillingDayHasNotPassed() {
        Loan loan = createLoan(LocalDate.of(2026, 9, 1), LocalDate.of(2026, 12, 1));
        LoanTerms terms = createTerms(loan, BillingMode.CONSOLIDATED, 5);

        List<InstallmentData> schedule = generator.generate(loan, terms);

        assertEquals(LocalDate.of(2026, 9, 5), schedule.get(0).duDate());
    }

    @Test
    void individualBillingKeepsTenureBasedDueDates() {
        Loan loan = createLoan(LocalDate.of(2026, 9, 20), LocalDate.of(2026, 12, 20));
        LoanTerms terms = createTerms(loan, BillingMode.INDIVIDUAL, null);

        List<InstallmentData> schedule = generator.generate(loan, terms);

        assertEquals(LocalDate.of(2026, 10, 20), schedule.get(0).duDate());
        assertEquals(LocalDate.of(2026, 11, 20), schedule.get(1).duDate());
        assertEquals(LocalDate.of(2026, 12, 20), schedule.get(2).duDate());
    }

    private LoanTerms createTerms(Loan loan, BillingMode billingMode, Integer billingDay) {
        return new LoanTerms(
                loan,
                3,
                TenureUnit.MONTHS,
                LoanStructure.INSTALLMENT,
                billingMode,
                billingDay,
                0,
                3);
    }

    private Loan createLoan(LocalDate disbursementDate, LocalDate maturityDate) {
        Loan loan = new Loan(null, null, new BigDecimal("900.00"));
        loan.disburse(disbursementDate, maturityDate);
        return loan;
    }
}
