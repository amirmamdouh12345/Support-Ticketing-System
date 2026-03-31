package com.itsupport.entities;

import com.itsupport.enums.TicketLogAction;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "ticket_logs")
@NoArgsConstructor
@Builder
@AllArgsConstructor
@Getter
@Setter
@ToString
public class TicketLog {

    @Id
    private String id;

    private String userId;

    @NotNull
    private String ticketId;

    private TicketLogAction ticketLogAction;


    private String oldVal;

    private String newVal;

    @CreatedDate
    private LocalDateTime createdAt ;

    @LastModifiedDate
    private LocalDateTime lastUpdatedAt;

}
