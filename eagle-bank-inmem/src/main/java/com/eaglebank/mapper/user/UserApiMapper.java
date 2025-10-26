package com.eaglebank.mapper.user;

import com.eaglebank.bo.UserBO;
import com.eaglebank.gen.model.CreateUserRequest;
import com.eaglebank.gen.model.CreateUserRequestAddress;
import com.eaglebank.gen.model.UserResponse;
import com.eaglebank.util.IdGenerator;
import lombok.experimental.UtilityClass;

import java.time.OffsetDateTime;

@UtilityClass
public class UserApiMapper {

    public UserBO fromCreateRequest(CreateUserRequest r) {
        return UserBO.builder()
                .name(r.getName())
                .id(IdGenerator.userId())
                .phoneNumber(r.getPhoneNumber())
                .email(r.getEmail())
                .passwordPlain(r.getPassword())
                .address(toBO(r.getAddress()))
                .created( OffsetDateTime.now())
                .updated( OffsetDateTime.now())
                .build();
    }



    public UserResponse toResponse(UserBO bo) {
        return new UserResponse()
                .id(bo.getId())
                .name(bo.getName())
                .phoneNumber(bo.getPhoneNumber())
                .email(bo.getEmail())
                .address(toResponseAddress(bo.getAddress()))
                .createdTimestamp(bo.getCreated())
                .updatedTimestamp(bo.getUpdated());
    }

    private UserBO.AddressBO toBO(CreateUserRequestAddress a) {
        if (a == null) return null;
        return UserBO.AddressBO.builder()
                .line1(a.getLine1())
                .line2(a.getLine2())
                .line3(a.getLine3())
                .town(a.getTown())
                .county(a.getCounty())
                .postcode(a.getPostcode())
                .build();
    }


    private CreateUserRequestAddress toResponseAddress(UserBO.AddressBO a) {
        if (a == null) return null;
        return new CreateUserRequestAddress()
                .line1(a.getLine1())
                .line2(a.getLine2())
                .line3(a.getLine3())
                .town(a.getTown())
                .county(a.getCounty())
                .postcode(a.getPostcode());
    }




}
