package com.itsupport.repos;

import com.itsupport.entities.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepo extends MongoRepository<Comment,String> {

    @Query("{ 'ticketId' : ?0 }")
    Page<Comment> findByTicketId(String ticketId, Pageable pageable);


}
