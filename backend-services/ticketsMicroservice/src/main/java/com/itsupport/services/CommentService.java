package com.itsupport.services;

import com.itsupport.dtos.comment.CommentRequestDto;
import com.itsupport.dtos.comment.CommentResponseDto;
import com.itsupport.entities.Comment;
import com.itsupport.mappers.CommentMapper;
import com.itsupport.repos.CommentRepo;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
@RequiredArgsConstructor
@Service
public class CommentService {

    private final Logger logger = LoggerFactory.getLogger(CommentService.class);

    private final CommentRepo commentRepo;

    private final TicketService ticketService;

    private final CommentMapper commentMapper;

    public Page<CommentResponseDto> findCommentsByTicketId(String ticketId,Integer size , Integer page) throws BadRequestException {

        logger.info("Find a Page of comments relevant to a Ticket Id.");

        if(ticketId==null || ticketId.isEmpty()){
            logger.error("Find Comments failed cause Ticket Id is not specified.");
            throw new BadRequestException("Find Comments failed cause Ticket Id is not specified.");
        }

        Pageable pageable= PageRequest.of(page,size);

        Page<Comment> comments =  commentRepo.findByTicketId(ticketId,pageable);

        List<CommentResponseDto> commentResponseDtos = commentMapper.toResponseDto(comments.getContent());


        if(comments.isEmpty()) {
            logger.error("No Comments found relevant to Ticket Id {}.",ticketId);
        }else {
            logger.info("There's {} comments attached to ticket Id {}.", comments.getContent().size() ,ticketId);
        }
        return new PageImpl<>(commentResponseDtos,pageable,comments.getTotalElements());
    }

    public String createComment(CommentRequestDto commentRequestDto) throws BadRequestException {

        logger.info("User Id {} creates a new Comment on Ticket Id {}." , commentRequestDto.getUserId() , commentRequestDto.getTicketId());

        // validate ticketId and userId
        ticketService.validateTicketId(commentRequestDto.getTicketId());

        Comment comment = commentMapper.toEntity(commentRequestDto);

        logger.info("a new Comment on Ticket Id {} is Created.",commentRequestDto.getTicketId());

        return commentRepo.save(comment).getId();
    }

    public String updateComment(String commentId , CommentRequestDto commentRequestDto) throws BadRequestException {

        logger.info("User Id {} updates an existing Comment by Id {} on Ticket Id {}.",commentRequestDto.getUserId(),commentId,commentRequestDto.getTicketId());

        Comment existingComment = commentRepo.findById(commentId).orElseThrow(()-> new BadRequestException("Comment Id doesn't exist."));

        commentMapper.updateComment(commentRequestDto,LocalDateTime.now(),existingComment);

        if (commentRequestDto.getComment()==null){
            logger.error("Comment is empty.");
            throw new BadRequestException("Comment is empty");
        }

        logger.info("Comment with Id {} is updated successfully.",commentId);

        return commentRepo.save(existingComment).getId();
    }

}
