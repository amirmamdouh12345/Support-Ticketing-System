package com.itsupport.mappers;

import com.itsupport.entities.TicketLog;
import org.springframework.stereotype.Component;

import java.util.List;

//@Mapper(componentModel = "spring")
@Component
public class TicketLogMapper {

    public List<String> toResponse(List<TicketLog> ticketLogs){
        return ticketLogs.stream().map((ticketLog)->
                ticketLog.getTicketLogAction()
                .format(
                        ticketLog.getUserId(),
                        ticketLog.getOldVal(),
                        ticketLog.getNewVal(),
                        String.valueOf(ticketLog.getCreatedAt()))).toList();
    }


}
