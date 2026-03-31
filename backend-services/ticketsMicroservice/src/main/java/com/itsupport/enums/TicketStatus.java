package com.itsupport.enums;

public enum TicketStatus {

    OPENED("OPENED"),
    IN_PROGRESS("IN_PROGRESS"),
    CLOSED("CLOSED");

    TicketStatus(String status) {
        this.status = status;
    }

    final String status;
}
