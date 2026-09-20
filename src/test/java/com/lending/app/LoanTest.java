package com.lending.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import com.lending.app.customer.domain.Customer;
import com.lending.app.customer.domain.CustomerSegment;
import com.lending.app.customer.persistence.CustomerRepository;
import com.lending.app.loan.api.CreateLoanRequest;
import com.lending.app.loan.application.CreateLoanService;
import com.lending.app.loan.domain.Loan;
import com.lending.app.loan.domain.LoanStatus;
import com.lending.app.loan.repository.LoanRepository;
import com.lending.app.notification.event.NotificationEvent;
import com.lending.app.notification.event.NotificationEventType;
import com.lending.app.product.domain.BillingMode;
import com.lending.app.product.domain.LoanProduct;
import com.lending.app.product.domain.LoanStructure;
import com.lending.app.product.domain.TenureUnit;
import com.lending.app.product.repository.LoanProductRepository;

@ExtendWith(MockitoExtension.class)
public class LoanTest {
    private Loan loan;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private LoanProductRepository loanProductRepository;

    @Mock
    private LoanRepository loanRepository;

    @InjectMocks
    private CreateLoanService service;

    @BeforeEach
    void setUp() {
        loan = new Loan(null, null, new BigDecimal("1000.00"));
    }

    @Test
    void shouldCreateLoanWithCreatedStatus() {
        assertEquals(LoanStatus.CREATED, loan.getStatus());
        assertEquals(new BigDecimal("1000.00"), loan.getPrincipal());
        assertNotNull(loan.getCreatedAt());
        assertNotNull(loan.getUpdatedAt());
    }

    @Test
    void shouldDisburseCreatedLoan() {
        LocalDate disbursementDate = LocalDate.of(2026, 9, 17);
        LocalDate maturityDate = LocalDate.of(2027, 12, 17);

        loan.disburse(disbursementDate, maturityDate);

        assertEquals(LoanStatus.OPEN, loan.getStatus());
        assertEquals(disbursementDate, loan.getDisbursementDate());
        assertEquals(disbursementDate, loan.getOriginationDate());
        assertEquals(maturityDate, loan.getMaturityDate());
    }

    @Test
    void shouldNotDisburseLoanMoreThanOnce() {
        LocalDate disbursementDate = LocalDate.of(2026, 9, 17);
        LocalDate maturityDate = LocalDate.of(2026, 12, 17);

        loan.disburse(disbursementDate, maturityDate);

        assertThrows(IllegalStateException.class, () -> loan.disburse(disbursementDate, maturityDate));
    }

    @Test
    void shouldCancelCreatedLoan() {
        loan.cancel();

        assertEquals(LoanStatus.CANCELLED, loan.getStatus());
    }

    @Test
    void shouldNotCancelDisbursedLoan() {
        loan.disburse(
                LocalDate.of(2026, 9, 17),
                LocalDate.of(2026, 12, 17));

        assertThrows(IllegalStateException.class, () -> loan.cancel());
    }

    @Test
    void shouldCloseOpenLoan() {
        loan.disburse(
                LocalDate.of(2026, 9, 17),
                LocalDate.of(2026, 12, 17));

        loan.close();

        assertEquals(LoanStatus.CLOSED, loan.getStatus());
    }

    @Test
    void shouldNotCloseCreatedLoan() {
        assertThrows(IllegalStateException.class, () -> loan.close());
    }

    @Test
    void shouldPublishLoanCreatedEvent() {
        Customer customer = new Customer("Maimuna", "Maksuudi", "maimuna@email.com", "254717000002",
                CustomerSegment.BUSINESS);

        LoanProduct product = new LoanProduct("SALARY-3M",
                "Salary Loan 3 Months",
                "Three month salary loan",
                3,
                TenureUnit.MONTHS,
                LoanStructure.INSTALLMENT,
                BillingMode.CONSOLIDATED,
                null,
                3,
                3);

        CreateLoanRequest request = new CreateLoanRequest(
                customer.getId(),
                product.getId(),
                new BigDecimal("10000.00"));

        Loan loan = new Loan(customer, product, request.principal());

        when(customerRepository.findByIdForUpdate(request.customerId())).thenReturn(Optional.of(customer));

        when(loanProductRepository.findById(request.productId())).thenReturn(Optional.of(product));

        when(loanRepository.save(any(Loan.class))).thenReturn(loan);

        service.execute(request);

        verify(eventPublisher).publishEvent(
                argThat((Object event) -> event instanceof NotificationEvent notificationEvent
                        && notificationEvent.eventType() == NotificationEventType.LOAN_CREATED
                        && Objects.equals(notificationEvent.loanId(), loan.getId())
                        && Objects.equals(notificationEvent.customerId(), customer.getId())
                        && notificationEvent.variables()
                                .get("amount")
                                .equals(new BigDecimal("10000.00"))));
    }
}
