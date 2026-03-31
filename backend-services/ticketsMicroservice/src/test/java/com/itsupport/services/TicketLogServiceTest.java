package com.itsupport.services;


import com.itsupport.dtos.ticket.TicketResponseDto;
import com.itsupport.entities.TicketLog;
import com.itsupport.enums.TicketLogAction;
import com.itsupport.mappers.TicketLogMapper;
import com.itsupport.repos.TicketLogRepo;
import org.apache.coyote.BadRequestException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Ticket Log Unit Tests")
public class TicketLogServiceTest {


    @Mock
    private TicketLogRepo ticketLogRepo;

    @Mock
    private TicketService ticketService;

    @Spy
    private TicketLogMapper ticketLogMapper = new TicketLogMapper() ;

    @InjectMocks
    private TicketLogService ticketLogService;

    @Nested
    @DisplayName("Find All Ticket Logs")
    class findAllTicketLogs {
        String ticketId;
        Integer size=5;
        Integer page=1;
        Pageable pageable = PageRequest.of(page,size);

        @Test
       void shouldThrowExceptionCauseTicketIdNotFound () throws BadRequestException {

            when(ticketService.findById(any())).thenThrow(BadRequestException.class);

            assertThrows(BadRequestException.class,()->{
                ticketLogService.findAllTicketLogs(ticketId,pageable);
            });


        }

        @Test
        void shouldReturnPageOfTicketLogs () throws BadRequestException {
            ticketId = "ticketId";

            when(ticketService.findById(ticketId)).thenReturn(TicketResponseDto.builder().build());

            when(ticketLogRepo.findByTicketId(ticketId,pageable)).thenReturn(new PageImpl<>(new ArrayList<>(),pageable,0));
            
            List<String> ticketLogResponseDtos = ticketLogService.findAllTicketLogs(ticketId ,pageable).getContent();

            assertEquals(0,ticketLogResponseDtos.size());
        }


    }


    @Nested
    class createTicketLog{

        String ticketLogId;
        String ticketId;
        String requestDto;

        TicketLog ticketLog;

        @Test
        void shouldCreateTicketLog() throws BadRequestException {
            ticketId="ticket-123";
            ticketLogId = "ticketLog-123";

            ticketLog = TicketLog
                        .builder()
                    .id(ticketLogId)
                    .ticketId(ticketId)
                    .build();

            when(ticketService.findById(ticketId))
                    .thenReturn(TicketResponseDto.builder().build());

            when(ticketLogRepo.save(any())).thenReturn(ticketLog);


            String resultedTicketLog = ticketLogService.createTicketLog(ticketId,null,null,null, TicketLogAction.CREATED);

            assertEquals(ticketLogId, resultedTicketLog);

        }


    }


}
