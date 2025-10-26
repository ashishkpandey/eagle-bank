package com.eaglebank.mapper.user;

import com.eaglebank.bo.UserBO;
import com.eaglebank.gen.model.CreateUserRequest;
import com.eaglebank.gen.model.CreateUserRequestAddress;
import com.eaglebank.gen.model.UserResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserApiMapperTest {

    @Test
    void fromCreateRequest_mapsAllFields_andGeneratesIdsAndTimestamps() {
        // given
        CreateUserRequestAddress addr = new CreateUserRequestAddress()
                .line1("10 Downing St")
                .line2("Westminster")
                .line3("Door B")
                .town("London")
                .county("Greater London")
                .postcode("SW1A 2AA");

        CreateUserRequest req = new CreateUserRequest()
                .name("Ashish Pandey")
                .phoneNumber("07123456789")
                .email("ashish@example.com")
                .password("secret")
                .address(addr);

        // when
        UserBO bo = UserApiMapper.fromCreateRequest(req);

        // then
        assertNotNull(bo);
        assertNotNull(bo.getId(), "Id must be generated");
        assertEquals("Ashish Pandey", bo.getName());
        assertEquals("07123456789", bo.getPhoneNumber());
        assertEquals("ashish@example.com", bo.getEmail());
        assertEquals("secret", bo.getPasswordPlain(), "Password should map to passwordPlain");

        assertNotNull(bo.getCreated(), "created timestamp must be set");
        assertNotNull(bo.getUpdated(), "updated timestamp must be set");

        assertNotNull(bo.getAddress(), "address must be mapped");
        assertEquals("10 Downing St", bo.getAddress().getLine1());
        assertEquals("Westminster", bo.getAddress().getLine2());
        assertEquals("Door B", bo.getAddress().getLine3());
        assertEquals("London", bo.getAddress().getTown());
        assertEquals("Greater London", bo.getAddress().getCounty());
        assertEquals("SW1A 2AA", bo.getAddress().getPostcode());
    }

    @Test
    void fromCreateRequest_handlesNullAddress() {
        CreateUserRequest req = new CreateUserRequest()
                .name("No Address")
                .phoneNumber("07000000000")
                .email("noaddr@example.com")
                .password("pw"); // no address

        UserBO bo = UserApiMapper.fromCreateRequest(req);

        assertNotNull(bo);
        assertNull(bo.getAddress(), "Null request address should remain null");
    }

    @Test
    void toResponse_mapsAllFields_andAddress() {
        UserBO.AddressBO a = UserBO.AddressBO.builder()
                .line1("221B Baker St")
                .line2("Marylebone")
                .line3("Flat 2")
                .town("London")
                .county("Greater London")
                .postcode("NW1 6XE")
                .build();

        UserBO bo = UserBO.builder()
                .id("usr-123")
                .name("Sherlock Holmes")
                .phoneNumber("07999999999")
                .email("sherlock@example.com")
                .address(a)
                .created(java.time.OffsetDateTime.now().minusDays(1))
                .updated(java.time.OffsetDateTime.now())
                .build();

        UserResponse resp = UserApiMapper.toResponse(bo);

        assertNotNull(resp);
        assertEquals("usr-123", resp.getId());
        assertEquals("Sherlock Holmes", resp.getName());
        assertEquals("07999999999", resp.getPhoneNumber());
        assertEquals("sherlock@example.com", resp.getEmail());
        assertNotNull(resp.getCreatedTimestamp());
        assertNotNull(resp.getUpdatedTimestamp());

        assertNotNull(resp.getAddress(), "Response address must be mapped");
        assertEquals("221B Baker St", resp.getAddress().getLine1());
        assertEquals("Marylebone", resp.getAddress().getLine2());
        assertEquals("Flat 2", resp.getAddress().getLine3());
        assertEquals("London", resp.getAddress().getTown());
        assertEquals("Greater London", resp.getAddress().getCounty());
        assertEquals("NW1 6XE", resp.getAddress().getPostcode());
    }

    @Test
    void toResponse_handlesNullAddress() {
        UserBO bo = UserBO.builder()
                .id("usr-null")
                .name("Null Addr")
                .phoneNumber("0700")
                .email("null@example.com")
                .created(java.time.OffsetDateTime.now().minusHours(2))
                .updated(java.time.OffsetDateTime.now())
                .build();

        UserResponse resp = UserApiMapper.toResponse(bo);

        assertNotNull(resp);
        assertNull(resp.getAddress(), "Null BO address should remain null in response");
    }
}
