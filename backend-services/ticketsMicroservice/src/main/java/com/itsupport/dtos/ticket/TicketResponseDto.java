package com.itsupport.dtos.ticket;

import com.itsupport.enums.TicketStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@ToString
public class TicketResponseDto {

    private String id;

    private String title;

    private String description;


    private String assignedUserId;

    // it-support user that the ticket is assigned to
    private String assignedUserFullName;

    // user who created the ticket
    private String reportedUserFullName;

    private String reportedUserId;

    private LocalDateTime closedAt;

    private TicketStatus ticketStatus;

}
