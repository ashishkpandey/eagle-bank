package com.eaglebank.bo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class UserBO {
    private String id;
    private String name;
    private String phoneNumber;
    private String email;
    private String passwordPlain;
    private AddressBO address;
    private OffsetDateTime created;
    private OffsetDateTime updated;

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class AddressBO {
        private String line1;
        private String line2;
        private String line3;
        private String town;
        private String county;
        private String postcode;
    }
}
