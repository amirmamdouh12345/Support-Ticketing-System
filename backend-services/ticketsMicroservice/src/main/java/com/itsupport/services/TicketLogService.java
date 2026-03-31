package com.itsupport.services;

import com.itsupport.entities.TicketLog;
import com.itsupport.enums.TicketLogAction;
import com.itsupport.mappers.TicketLogMapper;
import com.itsupport.repos.TicketLogRepo;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor

public class TicketLogService {


    private final TicketLogRepo ticketLogRepo;

    private final TicketService ticketService;

    private final TicketLogMapper ticketLogMapper;


    public Page<String> findAllTicketLogs(String ticketId ,Pageable pageable) throws BadRequestException {

        // validate on ticket
        ticketService.findById(ticketId);

        Page<TicketLog> ticketLogsPages = ticketLogRepo.findByTicketId(ticketId, pageable );
        List<String> ticketResponseDtos = ticketLogMapper.toResponse(ticketLogsPages.getContent());
        return new PageImpl<>(ticketResponseDtos,pageable, ticketLogsPages.getTotalElements());
    }

    public String createTicketLog(String ticketId , String userId , String oldVal ,String newVal , TicketLogAction ticketLogAction) throws BadRequestException {

        TicketLog ticketLog = TicketLog.builder()
                .ticketId(ticketId)
                .userId(userId)
                .oldVal(oldVal)
                .newVal(newVal)
                .createdAt(LocalDateTime.now())
                .ticketLogAction(ticketLogAction)
                .build();

        ticketService.findById(ticketLog.getTicketId());

        return ticketLogRepo.save(ticketLog).getId();
    }


}
