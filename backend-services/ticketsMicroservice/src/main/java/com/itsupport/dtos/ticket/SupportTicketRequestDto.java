package com.itsupport.dtos.ticket;

import com.itsupport.enums.TicketStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class SupportTicketRequestDto {

    // it-support user that the ticket is assigned to
    private String assignedUserId;

    private TicketStatus ticketStatus;

}
