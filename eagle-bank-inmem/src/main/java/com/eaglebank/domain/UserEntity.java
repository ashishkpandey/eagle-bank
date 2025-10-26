package com.eaglebank.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {
  private String id;
  private String name;
  private String phoneNumber;
  private String email;
  private String passwordHash;
  private Address address;
  private OffsetDateTime created;
  private OffsetDateTime updated;

  @Data @Builder @NoArgsConstructor @AllArgsConstructor
  public static class Address {
    private String line1;
    private String line2;
    private String line3;
    private String town;
    private String county;
    private String postcode;
  }
}
