package com.supportapp.repos;

import com.supportapp.entities.User;
import com.supportapp.enums.UserRole;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User,String> {

    @Query("{ 'id' : ?0 , 'role': ?1 }")
    Optional<User> findByUserIDAndRole(String userId, UserRole userRole);

    List<User> findAllByIdIn(List<String> ids);

}
