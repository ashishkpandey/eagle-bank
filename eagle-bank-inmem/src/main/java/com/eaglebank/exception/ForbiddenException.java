package com.eaglebank.exception;

public class ForbiddenException extends RuntimeException {
    public ForbiddenException() {
        super("Access is forbidden");
    }
}
