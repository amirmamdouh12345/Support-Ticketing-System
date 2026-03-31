package com.itsupport.repos;

import com.itsupport.entities.TicketLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TicketLogRepo extends MongoRepository<TicketLog,String> {

    @Query("{ 'ticketId' : ?0 }")
    Page<TicketLog> findByTicketId(String ticketId, Pageable pageable);

}
