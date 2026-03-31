package com.supportapp.mappers;

import com.supportapp.dtos.UserRequestDto;
import com.supportapp.dtos.UserResponseDto;
import com.supportapp.entities.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.time.LocalDateTime;
import java.util.List;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserMapper {

    @Mapping(target = "createdAt",ignore = true)
    @Mapping(target = "lastUpdatedAt",ignore = true)
    @Mapping(target = "age",ignore = true)
    @Mapping(source = "id", target = "id")
    User toEntity(UserRequestDto userRequestDto,String id);

    UserResponseDto toResponseDto(User user);

    @Mapping(target = "user.createdAt",ignore = true)
    @Mapping(target = "user.lastUpdatedAt",ignore = true)
    @Mapping(target = "user.uuid",ignore = true)
    List<UserResponseDto> toResponseDto(List<User> user);

    @Mapping(target = "user.createdAt",ignore = true)
    @Mapping(source = "lastUpdatedAt", target = "user.lastUpdatedAt")
    @Mapping(target = "id" , ignore = true)
    @Mapping(target = "age",ignore = true)
    void updateUser(UserRequestDto userRequestDto, LocalDateTime lastUpdatedAt, @MappingTarget User user);

}
