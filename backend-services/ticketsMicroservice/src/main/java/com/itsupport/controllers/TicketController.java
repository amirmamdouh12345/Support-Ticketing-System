package com.itsupport.controllers;

import com.itsupport.constants.Constants;
import com.itsupport.dtos.ticket.SupportTicketRequestDto;
import com.itsupport.dtos.ticket.UserTicketRequestDto;
import com.itsupport.dtos.ticket.TicketResponseDto;
import com.itsupport.enums.TicketStatus;
import com.itsupport.services.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tickets")
@Tag(name = "Ticket Management", description = "Endpoints for creating, filtering, and updating support tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @Operation(summary = "Find all tickets with filters",
            description = "Retrieves a paginated list of tickets. You can filter by the reporter's ID or the ticket status.")
    @GetMapping("/")
    public ResponseEntity<Page<TicketResponseDto>> findAllTickets(
            @Parameter(description = "Filter by the ID of the user who reported the ticket")
            @RequestParam(required = false) String reportedId,
            @Parameter(description = "Filter by the ID of the user who the ticket is assigned to")
            @RequestParam(required = false) String assignedId,
            @Parameter(description = "Filter by status (e.g., OPEN, IN_PROGRESS, CLOSED)")
            @RequestParam(required = false) TicketStatus ticketStatus,

            @Parameter(description = "Pageable to return a page of Tickets.")
            @PageableDefault(page = Constants.DEFAULT_PAGE_NUMBER,
                    size = Constants.DEFAULT_PAGE_SIZE,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC) Pageable pageable    ){
        // Logic check: If you pass page 1 to the repo, it returns the 2nd page.

        Page<TicketResponseDto> ticketResponseDtos = ticketService.findAll(reportedId , assignedId, ticketStatus,pageable);
        return ResponseEntity.ok(ticketResponseDtos);
    }

    @Operation(summary = "Get ticket by ID", description = "Fetches complete details for a single ticket.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the ticket"),
            @ApiResponse(description = "Ticket not found", responseCode = "400")
    })
    @GetMapping("/{ticketId}")
    public ResponseEntity<TicketResponseDto> findTicketById(
            @Parameter(description = "The unique ID of the ticket")
            @PathVariable("ticketId") String ticketId) throws BadRequestException {
        TicketResponseDto ticketResponseDto = ticketService.findById(ticketId);
        return ResponseEntity.ok(ticketResponseDto);
    }

    @Operation(summary = "Create a new ticket",
            description = "Creates a ticket associated with a specific reported user ID (typically from JWT).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Ticket created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid ticket data")
    })
    @PostMapping("/{reportedUserId}")
    public ResponseEntity<String> createTicket(
            @RequestBody UserTicketRequestDto userTicketRequestDto,
            @Parameter(description = "The ID of the user reporting the issue")
            @PathVariable(value = "reportedUserId") String reportedUserId) throws BadRequestException {

        String ticketId = ticketService.createTicket(userTicketRequestDto, reportedUserId);
        return ResponseEntity.status(201).body(ticketId);
    }

    @Operation(summary = "Reported User updates his own ticket Content", description = "Updates ticket details. Returns 204 No Content on success.")
    @PutMapping("/{ticketId}/user/{reportedId}")
    public ResponseEntity<Void> userUpdateTicket(
            @PathVariable("ticketId") String ticketId,
            @PathVariable("reportedId") String reportedId,
            @RequestBody UserTicketRequestDto userTicketRequestDto) throws BadRequestException {
        ticketService.updateTicketContent(ticketId , reportedId, userTicketRequestDto);
        return ResponseEntity.noContent().build();
    }

    // TODO: ItSupport should be deleted and check Role comes from Jwt.
    @Operation(summary = "Technical Support updates the ticket Status", description = "Updates ticket details. Returns 204 No Content on success.")
    @PutMapping("/{ticketId}/ItSupport/{itSupportId}")
    public ResponseEntity<Void> supportUpdateTicket(
            @PathVariable("ticketId") String ticketId,
            @PathVariable("itSupportId") String itSupportId,
            @RequestBody SupportTicketRequestDto userTicketRequestDto) throws BadRequestException {
        ticketService.updateTicketStatus(ticketId , itSupportId , userTicketRequestDto);
        return ResponseEntity.noContent().build();
    }

}