package com.supportapp.controllers;

import com.supportapp.constants.Constants;
import com.supportapp.dtos.UserRequestDto;
import com.supportapp.dtos.UserResponseDto;
import com.supportapp.dtos.UserWithFullNameDto;
import com.supportapp.enums.UserRole;
import com.supportapp.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "User Management", description = "Endpoints for managing application users and their profiles")
@RequiredArgsConstructor
public class UserController {


    private final UserService userService;

    @Operation(summary = "Get all users", description = "Returns a paginated list of all users in the system")
    @GetMapping()
    public ResponseEntity<Page<UserResponseDto>> findAllUsers(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) UserRole userRole,
            @PageableDefault(page = Constants.DEFAULT_PAGE_NUMBER,
                    size = Constants.DEFAULT_PAGE_SIZE,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(userService.findAll(username,userRole,pageable));
    }


    @Operation(summary = "Find user by ID", description = "Retrieves detailed information for a specific user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the user"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/{userId}")
    public UserResponseDto findUserById(@PathVariable("userId") String userId) throws BadRequestException {
        return userService.findUserById(userId);
    }

    @Operation(summary = "Create a new user", description = "Registers a new user and returns their generated unique ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid user data provided")
    })
    @PostMapping
    public String createUser(@RequestBody UserRequestDto user) {
        return userService.addUser(user).getId();
    }

    @Operation(summary = "Update user", description = "Updates user fields. Recalculates age if birthday is included in the request.")
    @PutMapping("/{userId}")
    public UserResponseDto updateUSer(
            @PathVariable("userId") String userId,
            @RequestBody UserRequestDto user) throws BadRequestException {
        return userService.updateUser(userId, user);
    }

    @Operation(summary = "Delete user", description = "Delete a User from the System.")
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable("userId") String userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }



    @Operation(summary = "Check if an It Support and an Ordinary User exist.", description = "Internal validation to verify if a user ID is valid for ticket assignment")
    @GetMapping("/validate/itSupport/{id}")
    public Boolean validateAssignedById(@PathVariable("id") String assignedUserId ) {
        return userService.validateAssignedUsersById(assignedUserId);
    }

    @Operation(summary = "Fetch full names", description = "Fetch full names for a list of user IDs (useful for ticket logs)")
    @PostMapping("/fetch/usernames")
    public List<UserWithFullNameDto> fetchUsernamesForTicket(@RequestBody List<String> usersIdList) {
        return userService.fetchUsernamesForTicket(usersIdList);
    }

}