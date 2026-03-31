package com.itsupport.controllers;

import com.itsupport.constants.Constants;
import com.itsupport.services.TicketLogService;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/ticketLog")
public class TicketLogController {

    private final TicketLogService ticketLogService;

    @GetMapping("ticket/{ticketId}")
    public ResponseEntity<Page<String>> fetchTicketLogs(
            @PathVariable("ticketId") String ticketId ,
            @Parameter(description = "Pageable to return a page of Tickets.")
            @PageableDefault(page = Constants.DEFAULT_PAGE_NUMBER,
                    size = Constants.DEFAULT_PAGE_SIZE,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC) Pageable pageable ) throws BadRequestException {
        return ResponseEntity.ok(ticketLogService.findAllTicketLogs(ticketId, pageable));
    }


}

