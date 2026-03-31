package com.itsupport.repos;

import com.itsupport.entities.Ticket;
import com.itsupport.enums.TicketStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface TicketRepo extends MongoRepository<Ticket,String> {

    public Optional<Ticket> findByIdAndTicketStatusAndReportedUserId(String id, TicketStatus ticketStatus , String reportedUserId   );

}
