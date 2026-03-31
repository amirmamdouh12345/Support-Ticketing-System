package com.itsupport.dtos.ticket;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UserTicketRequestDto {

    private String title;

    private String description;


}