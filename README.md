# Project Design: Architecture & Database Schema

This repository contains the high-level architecture and database design for the ticketing system, utilizing **React**, **Keycloak**, **Java Spring Boot**, and **MongoDB**.

## 1. System Architecture Diagram

This diagram illustrates the authentication flow and how the frontend interacts with the backend and security layers.

![Microservice Architecture](./docs/It_Support_Architecture_diagram.drawio.svg)

## 2. Database Schema (ER Diagram)

The following schema defines the relationships for the ticketing system.

```mermaid
erDiagram
    User ||--o{ Ticket : "assigned to"
    User ||--o{ Ticket : "reports"
    User ||--o{ Comment : "writes"

    Ticket ||--o{ Comment : "has"
    Ticket ||--o{ TicketLog : "generates"

    User {
        Long userId
        string uuid 
        string name
        string email
        string role
    }

    Ticket {
        string id
        string title
        String description
        Long assigneeId
        Long reporterId 
        Date date 
        Date lastUpdatedDate
        String ticketStatus
    }
    TicketLog {
        Long id
        String log
        Long ticketId
    }
    Comment {
        Long id
        String comment 
        Long userId
        Long ticketId
    }

```
