package com.itsupport.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TicketLogAction {
    CREATED("{Timestamp} - Ticket Created by {userId}"),
    UPDATE_ASSIGNED_USER("{Timestamp} - {userId} changed assignee of Ticket Id {ticketId} from {old} to {new}."),
    UPDATE_STATUS("{Timestamp} - {userId} changed Ticket Status from {old} to {new}"),
    UPDATE_TITLE("{Timestamp} - {userId} changed Title from {old} to {new}."),
    UPDATE_DESCRIPTION("{Timestamp} - {userId} changed Description from {old} to {new}.");

    final String messageTemplate;

    public String format(String userId , String oldVal , String newVal, String timestamp){
        return messageTemplate
                .replace("{Timestamp}", timestamp)
                .replace("{userId}", userId!=null ? userId: "IT_Support")
                .replace("{old}",oldVal !=null? oldVal : "None")
                .replace("{new}",newVal != null? newVal : "None");
    }


}
