package com.itsupport.services;

import com.itsupport.dtos.comment.CommentRequestDto;
import com.itsupport.dtos.comment.CommentResponseDto;
import com.itsupport.entities.Comment;
import com.itsupport.entities.Ticket;
import com.itsupport.mappers.CommentMapper;
import com.itsupport.repos.CommentRepo;
import org.apache.coyote.BadRequestException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Comments Service Test")
public class CommentServiceTest {

    @Mock
    private CommentRepo commentRepo;


    @Mock
    private TicketService ticketService;

    @Spy
    private CommentMapper commentMapper = Mappers.getMapper(CommentMapper.class);


    @InjectMocks
    private CommentService commentService;


    @Nested
    @DisplayName("Find Comment By Ticket Id")
    class findCommentsByTicketId{

        String ticketId;
        Integer page = 0;
        Integer size =4;
        Pageable pageable = PageRequest.of(page,size);
        Page<Comment> comments;
        List<CommentResponseDto> commentResponseDtos;
        Comment comment;

        @Test
        void shouldThrowBadRequestCauseTicketIdNull() {
            ticketId = null;

            assertThrows(BadRequestException.class,()->{
                commentService.findCommentsByTicketId(ticketId,size,page);
            });

        }



        @Test
        void shouldThrowBadRequestCauseTicketIdEmpty()  {
            ticketId = "";
            assertThrows(BadRequestException.class,()->{
                commentService.findCommentsByTicketId(ticketId,size,page);
            });

        }

        @Test
        void shouldReturnPageOfComments() throws BadRequestException {
            ticketId = "ticket-123";
            comment = Comment.builder()
                    .id("commentId")
                    .comment("first comment")
                    .userId("user-123")
                    .ticketId(ticketId)
                    .build();
            comments = new PageImpl<>(List.of(comment), pageable,1);
            commentResponseDtos  =commentMapper.toResponseDto(comments.getContent());

            when(commentRepo.findByTicketId(ticketId,pageable)).thenReturn(comments);

            List<CommentResponseDto> result = commentService.findCommentsByTicketId(ticketId,size,page).getContent();

            assertEquals(1,result.size());
            assertEquals(commentResponseDtos.getFirst().getTicketId(),result.getFirst().getTicketId());
            assertEquals(commentResponseDtos.getFirst().getComment(),result.getFirst().getComment());
            assertEquals(commentResponseDtos.getFirst().getUserId(),result.getFirst().getUserId());

        }

        @Test
        void shouldReturnEmptyOfComments() throws BadRequestException {
            // Arrange
            ticketId = "ticket-123";
            comments = new PageImpl<>(List.of(), pageable,0);

            when(commentRepo.findByTicketId(ticketId,pageable)).thenReturn(comments);

            // Act
            List<CommentResponseDto> result = commentService.findCommentsByTicketId(ticketId,size,page).getContent();

            // Assert
            assertEquals(0,result.size());

        }

    }

    @Nested
    @DisplayName("Create a new Comment")
    class createComment{

        CommentRequestDto commentRequestDto;
        String ticketId;
        Comment comment;

        @Test
        void shouldThrowBadRequest() throws BadRequestException {
            // Arrange
            ticketId= "ticket-123";
            commentRequestDto= CommentRequestDto.builder()
                    .comment("This ticket is urgent.")
                    .userId("user-123")
                    .ticketId(ticketId)
                    .build();

            when(ticketService.validateTicketId(ticketId)).thenThrow(BadRequestException.class);

            // Act & Assert
            assertThrows(BadRequestException.class,()->{
                commentService.createComment(commentRequestDto);
            });

        }

        @Test
        void shouldCreateComment() throws BadRequestException {
            ticketId= "ticket-123";
            commentRequestDto= CommentRequestDto.builder()
                    .comment("This ticket is urgent.")
                    .userId("user-123")
                    .ticketId(ticketId)
                    .build();

            comment = commentMapper.toEntity(commentRequestDto);

            when(ticketService.validateTicketId(ticketId)).thenReturn(new Ticket());
            when(commentRepo.save(any())).thenReturn(comment);

            comment.setId("comment-123");


            String resultedCommentId = commentService.createComment(commentRequestDto);

            assertEquals("comment-123",resultedCommentId);

        }




    }

    @Nested
    @DisplayName("Update an Existing Comment")
    class updateComment{

        @Test
        void updateComment_Success() throws BadRequestException {
            // Arrange
            String id = "123";
            CommentRequestDto dto = CommentRequestDto.builder().build();
            dto.setComment("Updated text");

            Comment existingComment = new Comment();
            existingComment.setId(id);

            when(commentRepo.findById(id)).thenReturn(Optional.of(existingComment));
            when(commentRepo.save(any(Comment.class))).thenReturn(existingComment);

            // Act
            String resultId = commentService.updateComment(id, dto);

            // Assert
            assertEquals(id, resultId);
            verify(commentRepo).save(existingComment);
            verify(commentMapper).updateComment(eq(dto), any(LocalDateTime.class), eq(existingComment));
        }

        @Test
        void updateComment_IdNotFound_ThrowsException() {
            // Arrange
            String id = "non-existent";
            when(commentRepo.findById(id)).thenReturn(Optional.empty());

            // Act & Assert
            BadRequestException exception = assertThrows(BadRequestException.class, () -> {
                commentService.updateComment(id,CommentRequestDto.builder().build());
            });

            assertEquals("Comment Id doesn't exist.", exception.getMessage());
        }


        @Test
        void updateComment_EmptyComment_ThrowsException() {
            // Arrange
            String id = "123";
            CommentRequestDto dto = CommentRequestDto.builder().build();
            dto.setComment(null); // Explicitly null

            Comment existingComment = new Comment();
            when(commentRepo.findById(id)).thenReturn(Optional.of(existingComment));

            // Act & Assert
            BadRequestException exception = assertThrows(BadRequestException.class, () -> {
                commentService.updateComment(id, dto);
            });

            assertEquals("Comment is empty", exception.getMessage());
        }

    }



}
