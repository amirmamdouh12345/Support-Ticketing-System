package com.supportapp.dtos;

import com.supportapp.enums.UserRole;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.time.LocalDate;

@Setter
@Getter
@ToString
public class UserResponseDto {

    private String id;

    private UserRole role;

    private String firstName;

    private String lastName;

    private String email;

    private LocalDate birthday;

    private Integer age;

}
