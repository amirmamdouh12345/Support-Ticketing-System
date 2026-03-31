package com.supportapp.dtos;

import com.supportapp.enums.UserRole;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
public class UserRequestDto {

    private String firstName;

    private String lastName;

    private String uuid;

    private String email;

    private LocalDate birthday;

    private UserRole role;


}
