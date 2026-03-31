package com.itsupport.mappers;

import com.itsupport.dtos.comment.CommentRequestDto;
import com.itsupport.dtos.comment.CommentResponseDto;
import com.itsupport.entities.Comment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.time.LocalDateTime;
import java.util.List;

@Mapper(componentModel = "spring"
        ,nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE )
public interface CommentMapper {


    @Mapping(target = "createdAt",ignore = true)
    @Mapping(target = "lastUpdatedAt",ignore = true)
    Comment toEntity(CommentRequestDto commentRequestDto);

    List<CommentResponseDto> toResponseDto(List<Comment> comment);

    // doesn't have the right to update the user who commented on the ticket
    @Mapping(target = "userId",ignore = true)
    @Mapping(source = "lastUpdatedAt" , target = "lastUpdatedAt")
    void updateComment(CommentRequestDto commentRequestDto , LocalDateTime lastUpdatedAt , @MappingTarget Comment comment);

}
