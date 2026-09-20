package com.lending.app.customer.domain;

/*
*
* Enum to represent the limit status of a customer
* @param SUSPENDED is useful if a customer's borrowing ability needs to be temporarily disabled
* without deleting the limit
*
 */
public enum CustomerLoanLimitStatus {
    ACTIVE,
    EXPIRED,
    SUSPENDED
}
