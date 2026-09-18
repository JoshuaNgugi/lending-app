package com.lending.app.loan.application;

import java.time.LocalDate;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * This class is responsible for scheduling the overdue loan sweep process.
 * It uses the OverdueLoanSweepService to execute the sweep at a specified interval.
 * 
 * @param loan.overdue-sweep.cron The cron expression for scheduling the sweep process.
 * It is configured in the application properties file.
 * 
 */
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
