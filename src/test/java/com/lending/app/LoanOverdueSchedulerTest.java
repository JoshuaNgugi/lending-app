package com.lending.app;

import static org.mockito.Mockito.verify;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.lending.app.loan.application.LoanOverdueScheduler;
import com.lending.app.loan.application.OverdueLoanSweepService;

@ExtendWith(MockitoExtension.class)
class LoanOverdueSchedulerTest {

    @Mock
    private OverdueLoanSweepService overdueLoanSweepService;

    @InjectMocks
    private LoanOverdueScheduler scheduler;

    @Test
    void shouldExecuteOverdueLoanSweep() {

        scheduler.sweep();

        verify(overdueLoanSweepService).execute(LocalDate.now());
    }
}
