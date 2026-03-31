package com.itsupport.mappers;

import com.itsupport.dtos.ticket.SupportTicketRequestDto;
import com.itsupport.dtos.ticket.UserTicketRequestDto;
import com.itsupport.dtos.ticket.TicketResponseDto;
import com.itsupport.entities.Ticket;
import org.mapstruct.*;

import java.time.LocalDateTime;
import java.util.List;


@Mapper(componentModel = "spring"
       ,nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TicketMapper {

    @Mapping(source = "reportedUserId", target = "reportedUserId")
    @Mapping(source = "assignedUserId",target = "assignedUserId" )
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ticketStatus", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "lastUpdatedAt", ignore = true)
    @Mapping(target = "closedAt", ignore = true)
    Ticket toEntity(UserTicketRequestDto userTicketRequestDto,
                    String reportedUserId,
                    String assignedUserId);


    @Mapping(source = "assignedUserFullName", target = "assignedUserFullName")
    @Mapping(source = "reportedUserFullName",target = "reportedUserFullName" )
    TicketResponseDto toResponseDto(Ticket ticket, String assignedUserFullName , String reportedUserFullName);

    void userUpdateTicketContent(UserTicketRequestDto userTicketRequestDto, @MappingTarget Ticket ticket);

    @Mapping(source = "closedAt" , target = "closedAt", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL)
    void supportUpdateTicket(SupportTicketRequestDto supportUserRequestDto, @MappingTarget Ticket ticket, LocalDateTime closedAt);

}



