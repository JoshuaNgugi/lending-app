package com.lending.app.repayment_schedule.application;

import java.util.List;

import org.springframework.stereotype.Service;

import com.lending.app.loan.domain.Loan;
import com.lending.app.loan.domain.LoanTerms;
import com.lending.app.repayment_schedule.domain.Installment;
import com.lending.app.repayment_schedule.domain.InstallmentData;
import com.lending.app.repayment_schedule.domain.RepaymentSchedule;
import com.lending.app.repayment_schedule.persistence.InstallmentRepository;
import com.lending.app.repayment_schedule.persistence.RepaymentScheduleRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class CreateRepaymentScheduleService {

    private final RepaymentScheduleRepository scheduleRepository;
    private final InstallmentRepository installmentRepository;
    private final RepaymentScheduleGenerator generator;

    public CreateRepaymentScheduleService(
            RepaymentScheduleRepository scheduleRepository,
            InstallmentRepository installmentRepository,
            RepaymentScheduleGenerator generator) {
        this.scheduleRepository = scheduleRepository;
        this.installmentRepository = installmentRepository;
        this.generator = generator;
    }

    public RepaymentSchedule execute(Loan loan, LoanTerms terms) {
        if (scheduleRepository.existsByLoanId(loan.getId())) {
            throw new IllegalStateException("Repayment schedule already exists for loan: " + loan.getId());
        }

        RepaymentSchedule schedule = new RepaymentSchedule(loan);

        schedule = scheduleRepository.save(schedule);

        List<InstallmentData> installmentData = generator.generate(loan, terms);

        for (InstallmentData data : installmentData) {
            Installment installment = new Installment(schedule, data.installmentNumber(), data.duDate(),
                    data.principalDue());

            installmentRepository.save(installment);
        }

        return schedule;
    }
}
