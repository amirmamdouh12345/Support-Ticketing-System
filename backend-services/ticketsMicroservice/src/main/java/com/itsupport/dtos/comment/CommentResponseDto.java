package com.itsupport.dtos.comment;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentResponseDto {

    private String id;

    private String comment;

    private String userId;

    private String ticketId;

}
