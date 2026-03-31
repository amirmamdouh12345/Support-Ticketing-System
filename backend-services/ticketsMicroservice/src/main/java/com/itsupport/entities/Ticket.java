package com.itsupport.entities;

import com.itsupport.enums.TicketStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "tickets")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@ToString
public class Ticket {

    @Id
    private String id;

    @NotNull
    private String title;

    private String description;

    // it-support user that the ticket is assigned to
    private String assignedUserId;

    // user who created the ticket
    private String reportedUserId;

    private TicketStatus ticketStatus;

    private LocalDateTime closedAt;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime lastUpdatedAt;


}
