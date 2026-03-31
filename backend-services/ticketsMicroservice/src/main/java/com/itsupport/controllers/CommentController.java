package com.itsupport.controllers;

import com.itsupport.dtos.comment.CommentRequestDto;
import com.itsupport.dtos.comment.CommentResponseDto;
import com.itsupport.services.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/comments")
@Tag(name = "Comment Management", description = "Endpoints for creating and managing ticket comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @Operation(summary = "Create a new comment", description = "Adds a comment to a ticket and returns the Location URI of the new resource.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Comment created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid comment data provided")
    })
    @PostMapping
    public ResponseEntity<String> createComment(@RequestBody CommentRequestDto commentRequestDto) throws BadRequestException {
        String commentId = commentService.createComment(commentRequestDto);
        URI uri = URI.create(String.format("/api/v1/comments/%s", commentId));

        return ResponseEntity.created(uri).body(commentId);
    }

    @Operation(summary = "Find comments by Ticket ID", description = "Retrieves a paginated list of comments associated with a specific ticket.")
    @GetMapping("/ticket/{ticketId}")
    public ResponseEntity<Page<CommentResponseDto>> findCommentsByTicketId(
            @Parameter(description = "ID of the ticket to fetch comments for") @PathVariable("ticketId") String ticketId,
            @Parameter(description = "Number of items per page") @RequestParam Integer size,
            @Parameter(description = "Page number (0-indexed)") @RequestParam Integer page) throws BadRequestException {
        return ResponseEntity.ok(commentService.findCommentsByTicketId(ticketId, size, page));
    }

    @Operation(summary = "Update a comment", description = "Updates the text of an existing comment. Returns the updated comment ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Comment updated successfully"),
            @ApiResponse(responseCode = "400", description = "Comment ID not found or invalid content")
    })
    @PutMapping("/{commentId}")
    public ResponseEntity<String> updateComment(
            @Parameter(description = "ID of the comment to update") @PathVariable("commentId") String commentId,
            @RequestBody CommentRequestDto commentRequestDto) throws BadRequestException {
        commentId = commentService.updateComment(commentId, commentRequestDto);
        return ResponseEntity.ok(commentId);
    }
}