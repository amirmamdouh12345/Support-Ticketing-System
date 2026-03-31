package com.itsupport.dtos.comment;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
public class CommentRequestDto {

    private String comment;

    private String userId;

    private String ticketId;

}
