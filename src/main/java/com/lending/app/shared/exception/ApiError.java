package com.lending.app.shared.exception;

public record ApiError(
        String code,
        String message) {
}
