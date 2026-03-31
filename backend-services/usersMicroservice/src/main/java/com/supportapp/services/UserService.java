package com.supportapp.services;

import com.supportapp.controllers.UserController;
import com.supportapp.dtos.UserRequestDto;
import com.supportapp.dtos.UserResponseDto;
import com.supportapp.entities.User;
import com.supportapp.enums.UserRole;
import com.supportapp.mappers.UserMapper;
import com.supportapp.dtos.UserWithFullNameDto;
import com.supportapp.repos.UserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final MongoTemplate mongoTemplate;


    public Page<UserResponseDto> findAll(String username , UserRole userRole , Pageable pageable){
        logger.info("Find all users with filters: Username {} , Role {} and pagaable {}.", username,userRole,pageable);

        Query query = prepareQueryFindAll(username,userRole);

        List<User> userResponsePages = mongoTemplate.find(query,User.class);
        long count = mongoTemplate.count(query.skip(-1).limit(-1),User.class);
        List<UserResponseDto> userResponseDtos = userMapper.toResponseDto(userResponsePages);

        logger.info("Returned Users: {}",userResponseDtos);
        return new PageImpl<>(userResponseDtos,pageable,count);
    }

    private Query prepareQueryFindAll(String username , UserRole userRole){

        logger.debug("Preparing Fetching Users Query based on Filters: username {} , role {}." ,username,userRole);
        Query query = new Query();
        List<Criteria> criteria = new ArrayList<>();
        if(username != null){
            username = username.trim();
            String[] nameSplit = username.split(" ");

            if(nameSplit.length ==1){
                Criteria firstName =  Criteria.where("firstName").regex(username,"i");
                Criteria lastName = Criteria.where("lastName").regex(username,"i");
                Criteria c = new Criteria().orOperator(firstName,lastName);

                criteria.add(c);

            }
            else{
                Criteria firstName =  Criteria.where("firstName").regex(nameSplit[0],"i");
                Criteria lastName = Criteria.where("lastName").regex(nameSplit[1],"i");
                Criteria c = new Criteria().andOperator(firstName,lastName);
                criteria.add(c);
            }
            logger.debug("Adding Username \"{}\" Criteria for Fetching Users.",username);

        }


        if(userRole !=null){
            criteria.add(Criteria.where("role").is(userRole));
            logger.debug("Adding Role \"{}\" Criteria for Fetching Users.",userRole);

        }

        if(!criteria.isEmpty()){
            query.addCriteria(new Criteria().andOperator(criteria.toArray(new Criteria[0])));
            logger.debug("No Criteria in Fetching Users.");

        }
        return query;
    }


    public UserResponseDto findUserById(String userId) throws BadRequestException {

        logger.info("Find User by Id {}.",userId);

        Optional<User> optionalUser = userRepository.findById(userId);
        if(optionalUser.isPresent()){
            logger.info("find user by id: {}",userId);

            User user =  optionalUser.get();
            UserResponseDto userResponseDto =userMapper.toResponseDto(user);
            logger.info("Fetched user by Id {} is {}.",userId,userResponseDto);

            return userResponseDto;
        }
        logger.error("User with Id: {} doesn't exist.",userId);
        throw new BadRequestException("User doesn't exist.");
    }

    public UserResponseDto addUser(UserRequestDto userDto){

        logger.info("Add new User {}.",userDto);
        User user = userMapper.toEntity(userDto,null);

        if(user.getBirthday()!=null){
            LocalDate today= LocalDate.now();
            Period period = Period.between(user.getBirthday(),today);
            user.setAge(period.getYears());
            logger.debug("Calculating Age from Birthday {} equals {}.",user.getBirthday(),period.getYears());
        }else {
            user.setAge(null);
        }

        user.setCreatedAt(LocalDateTime.now());
        User createdUser = userRepository.save(user);

        UserResponseDto userResponseDto = userMapper.toResponseDto(createdUser);
        logger.info("Added new user with Id {} : {}",createdUser.getId(),userResponseDto);

        return userResponseDto;
    }

    public UserResponseDto updateUser(String userIdentity,UserRequestDto user) throws BadRequestException {

        Optional<User> userOptional = userRepository.findById(userIdentity);

        if(userOptional.isEmpty()){
            logger.error("User with Id {} doesn't exist.",userOptional);
            throw new BadRequestException("No users with user Id "+ userIdentity +" exists.");
        }

        User existingUser = userOptional.get();
        userMapper.updateUser(user,LocalDateTime.now(),existingUser);

        if(user.getBirthday()!=null){
            LocalDate today= LocalDate.now();
            Period period = Period.between(user.getBirthday(),today);
            existingUser.setAge(period.getYears());
            logger.debug("Calculating Age from Birthday {} equals {}.",user.getBirthday(),period.getYears());

        }

        logger.info("update user with Id: {}",userIdentity);

        User updatedUser = userRepository.save(existingUser);

        return userMapper.toResponseDto(updatedUser);
    }

    public void  deleteUser(String userId) {
        userRepository.deleteById(userId);
    }

    public UserResponseDto findUserByIdAndRole(String userId, UserRole role) throws BadRequestException {

        Optional<User> optionalUser = userRepository.findByUserIDAndRole(userId,role);


        if(optionalUser.isPresent()){
            logger.info("User with Id {} and Role {} exists.",userId , role.getRole());

            User user = optionalUser.get();

            return userMapper.toResponseDto(user);
        }
        logger.error("User with Id {} and Role {} doesn't exist.",userId , role.getRole());

        throw new BadRequestException("User with Id "+ userId +" and Role "+role.getRole()+" doesn't exist.");
    }


    public List<UserWithFullNameDto> fetchUsernamesForTicket(List<String> usersIdList){

        List<UserResponseDto> users = fetchUsersForTicket(usersIdList);

        return users.stream()
                .map((user)-> UserWithFullNameDto.builder().id(user.getId()).fullName(user.getFirstName()+" "+user.getLastName()).build())
                .toList();
    }

    public List<UserResponseDto> fetchUsersForTicket(List<String> usersIdList){

        List<User> userList = userRepository.findAllByIdIn(usersIdList);

        return userMapper.toResponseDto(userList);
    }


    public Boolean validateAssignedUsersById(String assignedUserId) {

        Boolean isAssignedUserValid = false;

        try {
            findUserByIdAndRole(assignedUserId,UserRole.TECHNICAL_SUPPORT); // if not found -> Throws exception
            isAssignedUserValid=true;
        }catch (BadRequestException exception){
            isAssignedUserValid=false;
        }

        return isAssignedUserValid;

    }
}

