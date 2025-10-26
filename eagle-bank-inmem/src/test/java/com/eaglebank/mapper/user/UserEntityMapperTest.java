package com.eaglebank.mapper.user;

import com.eaglebank.bo.UserBO;
import com.eaglebank.domain.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

class UserEntityMapperTest {

    private UserEntityMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new UserEntityMapper();
    }

    @Test
    void toBO_mapsAllFieldsWithAddress() {
        OffsetDateTime created = OffsetDateTime.now().minusDays(1);
        OffsetDateTime updated = OffsetDateTime.now();

        UserEntity.Address addr = UserEntity.Address.builder()
                .line1("10 Downing St")
                .line2("Westminster")
                .line3("Door B")
                .town("London")
                .county("Greater London")
                .postcode("SW1A 2AA")
                .build();

        UserEntity entity = UserEntity.builder()
                .id("usr-1")
                .name("Ashish Pandey")
                .phoneNumber("07123456789")
                .email("ashish@example.com")
                .passwordHash("$2a$hash") // not surfaced in BO
                .address(addr)
                .created(created)
                .updated(updated)
                .build();

        UserBO bo = mapper.toBO(entity);

        assertNotNull(bo);
        assertEquals("usr-1", bo.getId());
        assertEquals("Ashish Pandey", bo.getName());
        assertEquals("07123456789", bo.getPhoneNumber());
        assertEquals("ashish@example.com", bo.getEmail());
        assertEquals(created, bo.getCreated());
        assertEquals(updated, bo.getUpdated());
        assertNotNull(bo.getAddress());
        assertEquals("10 Downing St", bo.getAddress().getLine1());
        assertEquals("Westminster", bo.getAddress().getLine2());
        assertEquals("Door B", bo.getAddress().getLine3());
        assertEquals("London", bo.getAddress().getTown());
        assertEquals("Greater London", bo.getAddress().getCounty());
        assertEquals("SW1A 2AA", bo.getAddress().getPostcode());
        // passwordPlain is intentionally not set from passwordHash by the mapper
        assertNull(bo.getPasswordPlain());
    }

    @Test
    void toBO_handlesNulls() {
        assertNull(mapper.toBO(null));
        UserEntity entityNoAddr = UserEntity.builder()
                .id("usr-x")
                .name("No Addr")
                .phoneNumber("0700")
                .email("x@x.com")
                .created(OffsetDateTime.now().minusHours(1))
                .updated(OffsetDateTime.now())
                .build();
        UserBO bo = mapper.toBO(entityNoAddr);
        assertNotNull(bo);
        assertNull(bo.getAddress());
    }

    @Test
    void toEntity_mapsAllFieldsWithAddress_andPasswordHashFromPlain() {
        OffsetDateTime created = OffsetDateTime.now().minusDays(2);
        OffsetDateTime updated = OffsetDateTime.now();

        UserBO.AddressBO a = UserBO.AddressBO.builder()
                .line1("221B Baker St")
                .line2("Marylebone")
                .line3("Flat 2")
                .town("London")
                .county("Greater London")
                .postcode("NW1 6XE")
                .build();

        UserBO bo = UserBO.builder()
                .id("usr-9")
                .name("Sherlock Holmes")
                .phoneNumber("07999999999")
                .email("sherlock@example.com")
                .passwordPlain("plain-secret")
                .address(a)
                .created(created)
                .updated(updated)
                .build();

        UserEntity entity = mapper.toEntity(bo);

        assertNotNull(entity);
        assertEquals("usr-9", entity.getId());
        assertEquals("Sherlock Holmes", entity.getName());
        assertEquals("07999999999", entity.getPhoneNumber());
        assertEquals("sherlock@example.com", entity.getEmail());
        // Mapper copies passwordPlain into passwordHash as per your code
        assertEquals("plain-secret", entity.getPasswordHash());
        assertEquals(created, entity.getCreated());
        assertEquals(updated, entity.getUpdated());

        assertNotNull(entity.getAddress());
        assertEquals("221B Baker St", entity.getAddress().getLine1());
        assertEquals("Marylebone", entity.getAddress().getLine2());
        assertEquals("Flat 2", entity.getAddress().getLine3());
        assertEquals("London", entity.getAddress().getTown());
        assertEquals("Greater London", entity.getAddress().getCounty());
        assertEquals("NW1 6XE", entity.getAddress().getPostcode());
    }

    @Test
    void toEntity_handlesNulls() {
        assertNull(mapper.toEntity(null));
        UserBO boNoAddr = UserBO.builder()
                .id("usr-y")
                .name("No Addr")
                .phoneNumber("0700")
                .email("y@y.com")
                .created(OffsetDateTime.now().minusHours(3))
                .updated(OffsetDateTime.now())
                .build();

        UserEntity e = mapper.toEntity(boNoAddr);
        assertNotNull(e);
        assertNull(e.getAddress());
    }

    @Test
    void roundTrip_commonFieldsStayEqual() {
        // Note: passwordPlain -> passwordHash one-way; not reversible by mapper (by design)
        UserBO original = UserBO.builder()
                .id("usr-rt")
                .name("Round Trip")
                .phoneNumber("0700123456")
                .email("rt@example.com")
                .passwordPlain("pw")
                .address(UserBO.AddressBO.builder()
                        .line1("L1").line2("L2").line3("L3")
                        .town("Town").county("County").postcode("PC")
                        .build())
                .created(OffsetDateTime.now().minusMinutes(10))
                .updated(OffsetDateTime.now())
                .build();

        UserEntity entity = mapper.toEntity(original);
        UserBO back = mapper.toBO(entity);

        assertEquals(original.getId(), back.getId());
        assertEquals(original.getName(), back.getName());
        assertEquals(original.getPhoneNumber(), back.getPhoneNumber());
        assertEquals(original.getEmail(), back.getEmail());
        assertEquals(original.getCreated(), back.getCreated());
        assertEquals(original.getUpdated(), back.getUpdated());
        assertNotNull(back.getAddress());
        assertEquals(original.getAddress().getLine1(), back.getAddress().getLine1());
        assertEquals(original.getAddress().getLine2(), back.getAddress().getLine2());
        assertEquals(original.getAddress().getLine3(), back.getAddress().getLine3());
        assertEquals(original.getAddress().getTown(), back.getAddress().getTown());
        assertEquals(original.getAddress().getCounty(), back.getAddress().getCounty());
        assertEquals(original.getAddress().getPostcode(), back.getAddress().getPostcode());
        // As designed, passwordPlain is not reconstructed from entity.passwordHash
        assertNull(back.getPasswordPlain());
    }
}
