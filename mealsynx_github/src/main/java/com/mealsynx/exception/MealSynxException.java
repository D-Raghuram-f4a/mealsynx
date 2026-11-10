package com.mealsynx.exception;

public class MealSynxException extends RuntimeException {
    private final String errorCode;

    public MealSynxException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() { return errorCode; }
}
