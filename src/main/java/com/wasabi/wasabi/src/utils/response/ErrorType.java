package com.wasabi.wasabi.src.utils.response;

public enum ErrorType {
    VALIDATION_ERROR("VALIDATION_ERROR"),
    USER_ALREADY_EXISTS("USER_ALREADY_EXISTS"),
    USER_NOT_FOUND("USER_NOT_FOUND"),
    INVALID_CREDENTIALS("INVALID_CREDENTIALS"),
    AUTHENTICATION_ERROR("AUTHENTICATION_ERROR"),
    NOT_FOUND("NOT_FOUND"),
    INTERNAL_ERROR("INTERNAL_ERROR");

    private final String type;

    ErrorType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return type;
    }
}
