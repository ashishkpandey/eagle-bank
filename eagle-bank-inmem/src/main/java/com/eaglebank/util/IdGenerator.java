package com.eaglebank.util;

import java.util.UUID;

public final class IdGenerator {

    public IdGenerator() {}

    private static String shortId() {
        // Take first 8 chars of UUID for compact but unique IDs
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }

    public static String userId() {
        return "usr-" + shortId();
    }

    public static String accountId() {
              int random = (int) (Math.random() * 1_000_000);
            return String.format("01%06d", random);
        }


    public static String transactionId() {
        return "tan-" + shortId();
    }
}
