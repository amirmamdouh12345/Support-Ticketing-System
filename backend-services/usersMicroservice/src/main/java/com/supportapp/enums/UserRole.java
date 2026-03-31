package com.supportapp.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserRole {
    ADMIN("ADMIN"),
    TECHNICAL_SUPPORT("TECHNICAL_SUPPORT"),
    USER("USER");

    final String role;



}
