package com.supportapp;

import com.supportapp.dtos.UserRequestDto;
import com.supportapp.dtos.UserResponseDto;
import com.supportapp.dtos.UserWithFullNameDto;
import com.supportapp.entities.User;
import com.supportapp.enums.UserRole;
import com.supportapp.mappers.UserMapper;
import com.supportapp.repos.UserRepository;
import com.supportapp.services.UserService;
import org.apache.coyote.BadRequestException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("User Service Unit Tests")
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Spy
    private UserMapper userMapper = Mappers.getMapper(UserMapper.class);

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private UserService userService;

    @Nested
    @DisplayName("Find All Users Functionality")
    class findAllUsers {
        int size = 5;
        int page = 0;
        Pageable pageable = PageRequest.of(page, size);
        User user = User.builder()
                .id("user-123")
                .email("amir@amir.com")
                .build();
        List<UserResponseDto> userResponseDtos;
        Page<UserResponseDto> userResponseDtoPage;

        @Test
        @DisplayName("Should return a page of users when filtered by User Role.")
        void shouldReturnPageOfUsersBasedOnRoleFilter() {
            userResponseDtos = List.of(userMapper.toResponseDto(user));
            when(mongoTemplate.find(any(), eq(User.class))).thenReturn(List.of(user));

            userResponseDtoPage = userService.findAll(null, UserRole.USER, pageable);

            assertEquals(1, userResponseDtoPage.getContent().size());
            assertEquals(userResponseDtos.getFirst().getEmail(), userResponseDtoPage.getContent().getFirst().getEmail());
        }

        @Test
        @DisplayName("Should return a page of users when filtered by First Name.")
        void shouldReturnPageOfUsersWithFirstNameFilter() {
            userResponseDtos = List.of(userMapper.toResponseDto(user));
            when(mongoTemplate.find(any(), eq(User.class))).thenReturn(List.of(user));

            userResponseDtoPage = userService.findAll("Amir", UserRole.USER, pageable);

            assertEquals(1, userResponseDtoPage.getContent().size());
            assertEquals(userResponseDtos.getFirst().getEmail(), userResponseDtoPage.getContent().getFirst().getEmail());
        }

        @Test
        @DisplayName("Should return a page of users when filtered by both First and Last Name.")
        void shouldReturnPageOfUsersWithFirstAndLastNamesFilter() {
            userResponseDtos = List.of(userMapper.toResponseDto(user));
            when(mongoTemplate.find(any(), eq(User.class))).thenReturn(List.of(user));

            userResponseDtoPage = userService.findAll("Amir Mamdouh", null, pageable);

            assertEquals(1, userResponseDtoPage.getContent().size());
            assertEquals(userResponseDtos.getFirst().getEmail(), userResponseDtoPage.getContent().getFirst().getEmail());
        }

        @Test
        @DisplayName("Should return a page of all users when no filters are provided.")
        void shouldReturnPageOfUsersWithoutFilter() {
            userResponseDtos = List.of(userMapper.toResponseDto(user));
            when(mongoTemplate.find(any(), eq(User.class))).thenReturn(List.of(user));

            userResponseDtoPage = userService.findAll(null, null, pageable);

            assertEquals(1, userResponseDtoPage.getContent().size());
            assertEquals(userResponseDtos.getFirst().getEmail(), userResponseDtoPage.getContent().getFirst().getEmail());
        }
    }

    @Nested
    @DisplayName("Find User By ID")
    class findUserById {
        String userId;
        String userEmail;
        Optional<User> optionalUser;

        @Test
        @DisplayName("Should throw BadRequestException when user ID is not found in the database.")
        void shouldThrowBadRequestException() {
            userId = "UNKNOWN";
            optionalUser = Optional.empty();
            when(userRepository.findById(userId)).thenReturn(optionalUser);

            assertThrows(BadRequestException.class, () -> {
                userService.findUserById(userId);
            });
        }

        @Test
        @DisplayName("Should return a UserResponseDto when a valid user ID is provided.")
        void shouldReturnUserResponse() throws BadRequestException {
            userId = "user-123";
            userEmail = "amir@amir.com";
            User user = User.builder()
                    .id(userId)
                    .email(userEmail).build();
            optionalUser = Optional.of(user);

            when(userRepository.findById(userId)).thenReturn(optionalUser);

            UserResponseDto userResponseDto = userService.findUserById(userId);

            assertEquals(userId, userResponseDto.getId());
            assertEquals(userEmail, userResponseDto.getEmail());
        }
    }

    @Nested
    @DisplayName("Add a New User")
    class addUser {
        UserRequestDto userRequestDto;
        String userEmail;
        String userId;

        @Test
        @DisplayName("Should successfully create a user and calculate age when birthday is provided.")
        void shouldCreateUserWithBirthday() {
            userId = "user-123";
            userEmail = "amir@amir.com";
            userRequestDto = UserRequestDto
                    .builder()
                    .email(userEmail)
                    .birthday(LocalDate.now().minusYears(20))
                    .build();

            User resultedUser = userMapper.toEntity(userRequestDto, userId);
            resultedUser.setId(userId);
            resultedUser.setAge(20);
            when(userRepository.save(any())).thenReturn(resultedUser);

            UserResponseDto actualUser = userService.addUser(userRequestDto);

            assertEquals(userId, actualUser.getId());
            assertEquals(userEmail, actualUser.getEmail());
            assertEquals(LocalDate.now().minusYears(20), actualUser.getBirthday());
            assertEquals(20, actualUser.getAge());
        }

        @Test
        @DisplayName("Should successfully create a user without age calculation when birthday is missing.")
        void shouldCreateUserWithoutBirthday() {
            userId = "user-123";
            userEmail = "amir@amir.com";
            userRequestDto = UserRequestDto
                    .builder()
                    .email(userEmail)
                    .build();

            User resultedUser = userMapper.toEntity(userRequestDto, userId);
            resultedUser.setId(userId);
            when(userRepository.save(any())).thenReturn(resultedUser);

            UserResponseDto actualUser = userService.addUser(userRequestDto);

            assertEquals(userId, actualUser.getId());
            assertEquals(userEmail, actualUser.getEmail());
            assertNull(actualUser.getBirthday());
            assertNull(actualUser.getAge());
        }
    }

    @Nested
    @DisplayName("Update an Existing User")
    class updateUser {
        String userId = "user-123";

        @Test
        @DisplayName("Should throw BadRequestException when trying to update a non-existent user ID.")
        void updateUser_UserNotFound_ThrowsBadRequestException() {
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            assertThrows(BadRequestException.class, () ->
                    userService.updateUser(userId, UserRequestDto.builder().build())
            );

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should update user and recalculate age when a new birthday is provided in the update request.")
        void updateUser_WithBirthday_CalculatesAge() throws BadRequestException {
            User existingUser = User.builder().build();
            UserRequestDto dto = UserRequestDto.builder().build();
            dto.setBirthday(LocalDate.now().minusYears(20));

            when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArguments()[0]);

            userService.updateUser(userId, dto);

            assertEquals(20, existingUser.getAge());
            verify(userRepository).save(existingUser);
        }

        @Test
        @DisplayName("Should skip age calculation and maintain existing age when birthday is null in the update request.")
        void updateUser_BirthdayNull_SkipsAgeCalculation() throws BadRequestException {
            User existingUser = User.builder().build();
            existingUser.setAge(30);

            UserRequestDto dto = UserRequestDto.builder().build();
            dto.setBirthday(null);

            when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArguments()[0]);

            userService.updateUser(userId, dto);

            assertEquals(30, existingUser.getAge());
            verify(userRepository).save(existingUser);
        }
    }

    @Nested
    @DisplayName("Find User By ID and Role")
    class findUserByIdAndRole {
        Optional<User> optionalUser;
        String userId = "user-123";
        UserRole userRole = UserRole.TECHNICAL_SUPPORT;
        String email = "amir@amir.com";

        @Test
        @DisplayName("Should throw BadRequestException when no user is found matching both the ID and the specific Role.")
        void shouldThrowBadRequest() {
            optionalUser = Optional.empty();
            when(userRepository.findByUserIDAndRole(userId, userRole)).thenReturn(optionalUser);

            assertThrows(BadRequestException.class, () -> {
                userService.findUserByIdAndRole(userId, userRole);
            });
        }

        @Test
        @DisplayName("Should return the UserResponseDto when a user matches both the provided ID and Role.")
        void shouldReturnAUser() throws BadRequestException {
            User user = User.builder().id(userId).email(email).build();
            optionalUser = Optional.of(user);
            when(userRepository.findByUserIDAndRole(userId, userRole)).thenReturn(optionalUser);

            UserResponseDto userResponseDto = userService.findUserByIdAndRole(userId, userRole);

            assertEquals(userId, userResponseDto.getId());
            assertEquals(email, userResponseDto.getEmail());
        }
    }

    @Nested
    @DisplayName("Validate Assigned Users By ID")
    class validateAssignedUsersById {
        Optional<User> optionalUser;
        String userId = "user-123";
        UserRole userRole = UserRole.TECHNICAL_SUPPORT;
        String email = "amir@amir.com";

        @Test
        @DisplayName("Should return false when the user does not exist or does not have the Technical Support role.")
        void shouldReturnFalseWhenUserNotFound() {
            optionalUser = Optional.empty();
            when(userRepository.findByUserIDAndRole(userId, userRole)).thenReturn(optionalUser);

            Boolean isTechnicalSupportExists = userService.validateAssignedUsersById(userId);

            assertEquals(false, isTechnicalSupportExists);
        }

        @Test
        @DisplayName("Should return true when a valid Technical Support user is found for the given ID.")
        void shouldReturnTrueWhenUserExists() {
            User user = User.builder().id(userId).email(email).build();
            optionalUser = Optional.of(user);
            when(userRepository.findByUserIDAndRole(userId, userRole)).thenReturn(optionalUser);

            Boolean isTechnicalSupportExists = userService.validateAssignedUsersById(userId);

            assertEquals(true, isTechnicalSupportExists);
        }
    }

    @Nested
    @DisplayName("Fetch Users for Ticket")
    class fetchUsersForTicket {
        User user = User.builder()
                .id("user-123")
                .email("amir@amir.com")
                .build();
        List<String> userIds = List.of("user-123");
        List<User> userList = List.of(user);

        @Test
        @DisplayName("Should return a list of UserResponseDtos for all provided user IDs.")
        void shouldFetchUserBasedOnIds() {
            when(userRepository.findAllByIdIn(userIds)).thenReturn(userList);

            List<UserResponseDto> responseDtos = userService.fetchUsersForTicket(userIds);

            assertEquals(1, responseDtos.size());
            assertEquals(user.getId(), responseDtos.getFirst().getId());
        }
    }

    @Nested
    @DisplayName("Fetch Usernames for Ticket")
    class fetchUsernamesForTicket {
        User user = User.builder()
                .id("user-123")
                .email("amir@amir.com")
                .build();
        List<String> userIds = List.of("user-123");
        List<User> userList = List.of(user);

        @Test
        @DisplayName("Should return a list of DTOs containing full names based on the provided user IDs.")
        void shouldFetchUsernamesBasedOnIds() {
            when(userRepository.findAllByIdIn(userIds)).thenReturn(userList);

            List<UserWithFullNameDto> responseDtos = userService.fetchUsernamesForTicket(userIds);

            assertEquals(1, responseDtos.size());
            assertEquals(user.getId(), responseDtos.getFirst().getId());
        }
    }

    @Nested
    @DisplayName("Delete User")
    class deleteUser {
        @Test
        @DisplayName("Should call the repository delete method exactly once for the specified user ID.")
        void shouldDeleteUser() {
            String userId = "userId";

            userService.deleteUser(userId);

            verify(userRepository, times(1)).deleteById(userId);
        }
    }
}