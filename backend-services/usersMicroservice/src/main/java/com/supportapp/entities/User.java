package com.supportapp.entities;


import com.supportapp.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Document(collection = "users")
@Setter
@Getter
@ToString
@Builder
public class User {

    @Id
    private String id;

    @NotNull
    private UserRole role;

    @Indexed(unique = true)
    private String uuid;

    @NotNull
    private String firstName;

    @NotNull
    private String lastName;

    @Email
    @Indexed(unique = true)
    @NotNull
    private String email;

    private LocalDate birthday;

    @NotNull
    private Integer age;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime lastUpdatedAt;

}
