package com.eaglebank.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GlobalExceptionHandlerTest {

    @Test
    void handle401_returnsUnauthorized() {
        GlobalExceptionHandler h = new GlobalExceptionHandler();
        var resp = h.handle401();
        assertEquals(401, resp.getStatusCode().value());
    }

    @Test
    void handle403_returnsForbidden() {
        GlobalExceptionHandler h = new GlobalExceptionHandler();
        var resp = h.handle403();
        assertEquals(403, resp.getStatusCode().value());
    }

    @Test
    void handle404_returnsNotFound() {
        GlobalExceptionHandler h = new GlobalExceptionHandler();
        var resp = h.handle404();
        assertEquals(404, resp.getStatusCode().value());
    }
}