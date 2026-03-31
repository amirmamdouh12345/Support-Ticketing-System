package com.supportapp.dtos;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class UserWithFullNameDto {

    private String id;

    private String fullName;



}
