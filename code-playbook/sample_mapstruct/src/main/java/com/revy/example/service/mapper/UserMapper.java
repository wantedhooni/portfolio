package com.revy.example.service.mapper;

import com.revy.example.config.MapStructConfig;
import com.revy.example.controller.UserCreateRequest;
import com.revy.example.controller.UserResponse;
import com.revy.example.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(config = MapStructConfig.class)
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    User toEntity(UserCreateRequest request);

    UserResponse toResponse(User user);
}