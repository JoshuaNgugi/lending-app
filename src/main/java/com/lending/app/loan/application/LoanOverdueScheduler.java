package com.lending.app.loan.application;

import java.time.LocalDate;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class LoanOverdueScheduler {

    private final OverdueLoanSweepService overdueLoanSweepService;

    public LoanOverdueScheduler(OverdueLoanSweepService overdueLoanSweepService) {
        this.overdueLoanSweepService = overdueLoanSweepService;
    }

    @Scheduled(cron = "${loan.overdue-sweep.cron}")
    public void sweep() {
        overdueLoanSweepService.execute(LocalDate.now());
    }
}
