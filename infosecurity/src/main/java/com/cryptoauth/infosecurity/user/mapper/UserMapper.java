package com.cryptoauth.infosecurity.user.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.MappingConstants;

import com.cryptoauth.infosecurity.user.dto.UserCreate;
import com.cryptoauth.infosecurity.user.dto.UserGet;
import com.cryptoauth.infosecurity.user.dto.UserUpdate;
import com.cryptoauth.infosecurity.user.entity.User;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {
    @Mapping(target = "passwordRestrictions", source = "passwordRestrictions") // исправление имени
    @Mapping(target = "firstLogin", source = "firstLogin")
    @Mapping(target = "accountLocked", source = "accountLocked")
    @Mapping(target = "minPasswordLength", source = "minPasswordLength")
    @Mapping(target = "passwordExpirationMonths", source = "passwordExpirationMonths")
    @Mapping(target = "passwordLastChanged", source = "passwordLastChanged")
    @Mapping(target = "role", source = "role")
    @Mapping(target = "id", source = "id")
    @Mapping(target = "email", source = "email")
    UserGet toDto(User user);

    List<UserGet> toDtoList(List<User> users);

    @Mapping(target = "passwordRestrictions", ignore = true)
    @Mapping(target = "firstLogin", ignore = true)
    @Mapping(target = "accountLocked", ignore = true)
    @Mapping(target = "minPasswordLength", ignore = true)
    @Mapping(target = "passwordExpirationMonths", ignore = true)
    @Mapping(target = "passwordLastChanged", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password_hash", ignore = true)
    @Mapping(target = "salt", ignore = true)
    @Mapping(target = "failedAttempts", ignore = true)
    @Mapping(target = "emailConfirmed", ignore = true)
    User toEntity(UserCreate userCreate);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordRestrictions", ignore = true)
    void updateEntity(@MappingTarget User user, UserUpdate dto);
}
