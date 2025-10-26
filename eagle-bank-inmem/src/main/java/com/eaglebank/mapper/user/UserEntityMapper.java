package com.eaglebank.mapper.user;

import com.eaglebank.bo.UserBO;
import com.eaglebank.domain.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class UserEntityMapper {

    public UserBO toBO(UserEntity entity) {
        if (entity == null) return null;

        var addressEntity = entity.getAddress();
        var addressBO = addressEntity == null ? null :
                UserBO.AddressBO.builder()
                        .line1(addressEntity.getLine1())
                        .line2(addressEntity.getLine2())
                        .line3(addressEntity.getLine3())
                        .town(addressEntity.getTown())
                        .county(addressEntity.getCounty())
                        .postcode(addressEntity.getPostcode())
                        .build();

        return UserBO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .phoneNumber(entity.getPhoneNumber())
                .email(entity.getEmail())
                .address(addressBO)
                .created(entity.getCreated())
                .updated(entity.getUpdated())
                .build();
    }

    public UserEntity toEntity(UserBO bo) {
        if (bo == null) return null;

        var addressBO = bo.getAddress();
        var addressEntity = addressBO == null ? null :
                UserEntity.Address.builder()
                        .line1(addressBO.getLine1())
                        .line2(addressBO.getLine2())
                        .line3(addressBO.getLine3())
                        .town(addressBO.getTown())
                        .county(addressBO.getCounty())
                        .postcode(addressBO.getPostcode())
                        .build();

        return UserEntity.builder()
                .id(bo.getId())
                .name(bo.getName())
                .phoneNumber(bo.getPhoneNumber())
                .email(bo.getEmail())
                .passwordHash(bo.getPasswordPlain())
                .address(addressEntity)
                .created(bo.getCreated())
                .updated(bo.getUpdated())
                .build();
    }
}
