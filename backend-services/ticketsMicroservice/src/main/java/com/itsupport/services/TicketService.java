package com.itsupport.services;

import com.itsupport.constants.Constants;
import com.itsupport.dtos.ticket.SupportTicketRequestDto;
import com.itsupport.dtos.ticket.UserTicketRequestDto;
import com.itsupport.dtos.ticket.TicketResponseDto;
import com.itsupport.dtos.UserWithFullNameDto;
import com.itsupport.entities.Ticket;
import com.itsupport.enums.TicketLogAction;
import com.itsupport.enums.TicketStatus;
import com.itsupport.external.ApiCaller;
import com.itsupport.mappers.TicketMapper;
import com.itsupport.repos.TicketRepo;
import org.apache.coyote.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class TicketService {

    private static final Logger logger = LoggerFactory.getLogger(TicketService.class);

    private final TicketRepo ticketRepo;
    private final ApiCaller apiCaller;
    private final TicketMapper ticketMapper;
    private final MongoTemplate mongoTemplate;
    private final TicketLogService ticketLogService;
    @Value("${spring.microservice.ticket.url}")
    private String userUrl;

    @Value("${spring.microservice.user.validate}")
    private String validateUserPath;

    @Value("${spring.microservice.user.fetch.usernames}")
    private String fetchUsernamesPath;

    public TicketService(TicketRepo ticketRepo,
                         ApiCaller apiCaller,
                         TicketMapper ticketMapper,
                         MongoTemplate mongoTemplate, @Lazy TicketLogService ticketLogService) {
        this.ticketRepo = ticketRepo;
        this.apiCaller = apiCaller;
        this.ticketMapper = ticketMapper;
        this.mongoTemplate = mongoTemplate;
        this.ticketLogService = ticketLogService;
    }

    public Page<TicketResponseDto> findAll(
            String reportedUserId,
            String assignedUserId,
            TicketStatus ticketStatus,
            Pageable pageable)
            {

        logger.info("Request to find all Tickets with Filters Reported User Id {} , Assigned User Id {} and ticket status {}.", reportedUserId , assignedUserId, ticketStatus);

        Query query = prepareQueryForFetchingTicketPages(reportedUserId,assignedUserId,ticketStatus).with(pageable);
        List<Ticket> ticketsList = mongoTemplate.find(query, Ticket.class);

        // there's no tickets
        if(ticketsList.isEmpty()){
            logger.warn("There's no tickets founded with Filters Reported User Id {} , Assigned User Id {} and ticket status {}", reportedUserId , assignedUserId, ticketStatus);
            return new PageImpl<>(List.of(),pageable,0);
        }

        long count = mongoTemplate.count(query.skip(-1).limit(-1), Ticket.class);
        Page<Ticket> ticketPage =  new PageImpl<>(ticketsList,pageable,count);

        Set<String> usernamesValidationRequestBody = prepareUsernamesValidationRequest(ticketsList);

        try {
            Map<String, String> userIdsNamesMapper = fetchUsernamesFromUserApi(usernamesValidationRequestBody);

            return ticketPage.map((ticket) -> ticketMapper.toResponseDto(ticket,
                    userIdsNamesMapper.getOrDefault(ticket.getAssignedUserId(), Constants.UNASSIGNED_USER),
                    userIdsNamesMapper.getOrDefault(ticket.getReportedUserId(), Constants.NO_REPORTED_USER)));
        }
        catch (RuntimeException exp){
            logger.error("Failed to enrich tickets with usernames. Ticket Ids {}",usernamesValidationRequestBody, exp);
            throw exp;
        }
    }

    private Map<String,String> fetchUsernamesFromUserApi(Set<String> usernamesValidationRequestBody ){

        logger.debug("Fetching Usernames from User Microservice API with User Ids: {}.",usernamesValidationRequestBody);

        ResponseEntity<List<UserWithFullNameDto>> usernamesListResponse =
                apiCaller.callApiWithParameterizedResponseBody(
                    userUrl,
                    fetchUsernamesPath,
                    HttpMethod.POST,
                    usernamesValidationRequestBody,
                    null,
                    new ParameterizedTypeReference<>() {
                    }
                );

        Map<String, String> userIdsNamesMapper = new HashMap<>();

        // Miscommunication with User Microservice API
        if (usernamesListResponse == null ||  usernamesListResponse.getBody() == null){
            logger.error("Calling API {}/{} caused response: {}",userUrl,fetchUsernamesPath ,usernamesListResponse);
            throw new RuntimeException("findAllTickets: Calling API "+userUrl+fetchUsernamesPath+" caused response:" + usernamesListResponse);
        }


        if (usernamesListResponse.getBody().isEmpty())
            logger.warn("No Usernames Fetched for Users with  Request: {}.",usernamesValidationRequestBody);
        else
            logger.debug("Fetched Usernames from User Microservice API: Request {} - Response {}.", usernamesValidationRequestBody, usernamesListResponse.getBody());

        for (UserWithFullNameDto user : usernamesListResponse.getBody()){
            userIdsNamesMapper.put(user.getId(),user.getFullName());
        }

        return userIdsNamesMapper;
    }



    private Query prepareQueryForFetchingTicketPages(
            String reportedUserId,
            String assignedUserId,
            TicketStatus ticketStatus
    ){


        Query query = new Query();

        List<Criteria> criteria = new ArrayList<>();

        if(reportedUserId == null && assignedUserId == null && ticketStatus == null){

            logger.debug("Preparing a Query to fetch all tickets except Closed Tickets exceeds two weeks ago.");

            Criteria shouldNotBeCLOSED = Criteria.where("ticketStatus").ne(TicketStatus.CLOSED);
            Criteria shouldNotLastUpdatesExceedsTwoWeeks = Criteria.where("closedAt").gte(LocalDateTime.now().minusWeeks(2));
            criteria.add(
                    new Criteria().orOperator(
                            shouldNotBeCLOSED,
                            shouldNotLastUpdatesExceedsTwoWeeks
                    )
            );
        }
        else {
            logger.debug("Preparing a Query with filters - reportedId: {} , assignedId: {} and ticketStatus: {}",reportedUserId,assignedUserId,ticketStatus);

            if (reportedUserId != null){
                criteria.add(
                        Criteria.where("reportedUserId").is(reportedUserId)
                );
            }

            if (assignedUserId != null){
                criteria.add(
                        Criteria.where("assignedUserId").is(assignedUserId)
                );
            }

            if (ticketStatus != null){
                criteria.add(
                        Criteria.where("ticketStatus").is(ticketStatus)
                );
            }

        }

        query.addCriteria(new Criteria().andOperator(criteria.toArray(new Criteria[0])));

        logger.debug("Final MongoDB Filter: {}",query);

        return query;
    }

    private Set<String> prepareUsernamesValidationRequest(List<Ticket> tickets){

        logger.debug("Preparing User Ids as a Request Body.");

        Set<String> usersIdSet = new HashSet<>();
        for (Ticket ticket : tickets){
            if(ticket.getAssignedUserId()!=null )
                usersIdSet.add(ticket.getAssignedUserId());


            if(ticket.getReportedUserId() !=null )
                usersIdSet.add(ticket.getReportedUserId());

        }

        if(usersIdSet.isEmpty())
            logger.warn("User Ids Request Body is Empty.");
        else
            logger.debug("User Ids Request Body: {}.",usersIdSet);

        return usersIdSet;
    }

    public TicketResponseDto findById(String ticketId) throws BadRequestException {

        logger.info("Find Ticket with Id {}.", ticketId);

        Ticket ticket = validateTicketId(ticketId);

        Set<String> usersAttachedToTicket = prepareUsernamesValidationRequest(List.of(ticket));

        if(usersAttachedToTicket.isEmpty()){
            logger.warn("Fetched Ticket by Id {} is not Reported or Assigned to Users.",ticketId);
            return ticketMapper.toResponseDto(
                    ticket,
                    Constants.UNASSIGNED_USER,
                    Constants.NO_REPORTED_USER);
        }

        // key -> userId      , value -> username
        Map<String,String> usernames = fetchUsernamesFromUserApi(usersAttachedToTicket);

        TicketResponseDto ticketResponseDto = ticketMapper.toResponseDto(
                ticket,
                usernames.getOrDefault(ticket.getAssignedUserId(),Constants.UNASSIGNED_USER),
                usernames.getOrDefault(ticket.getReportedUserId(),Constants.NO_REPORTED_USER));

        logger.info("Fetch Ticket by Id {} : {} " , ticketId , ticketResponseDto);

        return ticketResponseDto;
    }


    public String createTicket(UserTicketRequestDto userTicketRequestDto, String reportedUserId) throws BadRequestException {

        logger.info("Create a new Ticket reported by User Id {}",reportedUserId);

        if(reportedUserId == null || userTicketRequestDto.getTitle() ==null){
            logger.error("Reported User and Title shouldn't be null.");
            throw new BadRequestException("Reported User and Title shouldn't be null");
        }

        Ticket ticket = ticketMapper.toEntity(userTicketRequestDto,reportedUserId,null);

        ticket.setTicketStatus(TicketStatus.OPENED);

        Ticket createdTicket = ticketRepo.save(ticket);


        ticketLogService.createTicketLog(createdTicket.getId(),reportedUserId,null,null,TicketLogAction.CREATED);


        logger.info("createTicket: New Ticket created Reported by User Id {} with ticket Id {}.",ticket.getReportedUserId(),createdTicket.getId());

        return createdTicket.getId();
    }

    public void updateTicketStatus(String ticketId , String itSupportId , SupportTicketRequestDto ticketRequest) throws BadRequestException {

        logger.info("Update Ticket Content by Id {}.", ticketId);

        Ticket existingTicket = validateTicketId(ticketId);

        boolean isUserValidated = true;

        isUserValidated = isAssignedUserValidatedUserApi(itSupportId);

        if( ticketRequest.getAssignedUserId() != null ) {
            isUserValidated = isAssignedUserValidatedUserApi(ticketRequest.getAssignedUserId());
        }

        if(!isUserValidated){
            logger.error("Technical Support Id {} doesn't match. " , ticketRequest.getAssignedUserId()  );
            throw new BadRequestException("Technical Support Id "+ ticketRequest.getAssignedUserId() +" doesn't match.");
        }

        ticketMapper.supportUpdateTicket(ticketRequest ,
                existingTicket ,
                ticketRequest.getTicketStatus() == existingTicket.getTicketStatus() ? existingTicket.getClosedAt()
                        : ticketRequest.getTicketStatus() == TicketStatus.CLOSED ?
                        LocalDateTime.now() :
                        null
        );

        if (ticketRequest.getTicketStatus()!= null ){
            ticketLogService.createTicketLog(ticketId,"currentItSupport",existingTicket.getTicketStatus().toString(),ticketRequest.getTicketStatus().toString(),TicketLogAction.UPDATE_TITLE);
        }

        if (ticketRequest.getAssignedUserId()!= null){
            ticketLogService.createTicketLog(ticketId,"currentItSupport",existingTicket.getAssignedUserId(),ticketRequest.getAssignedUserId(),TicketLogAction.UPDATE_DESCRIPTION);
        }

        ticketRepo.save(existingTicket);


    }

    public void updateTicketContent(String ticketId , String reportedId , UserTicketRequestDto ticketRequest) throws BadRequestException {

        logger.info("Update Ticket Content by Id {}.", ticketId);

        Ticket existingTicket = isTicketStausOpened(ticketId, reportedId);

        if (ticketRequest.getTitle()!= null){
           ticketLogService.createTicketLog(ticketId,reportedId,existingTicket.getTitle(),ticketRequest.getTitle(),TicketLogAction.UPDATE_TITLE);
        }

        if (ticketRequest.getDescription()!= null){
            ticketLogService.createTicketLog(ticketId,reportedId,existingTicket.getDescription(),ticketRequest.getDescription(),TicketLogAction.UPDATE_DESCRIPTION);
        }

        ticketMapper.userUpdateTicketContent(ticketRequest,existingTicket);

        logger.debug("Update Ticket Content by Id {} to {}.", ticketId,ticketRequest);

        logger.info("updateTicket: ticket with Id {} is updated to {}." , ticketId ,existingTicket );

        ticketRepo.save(existingTicket);
    }

    Ticket validateTicketId(String ticketId) throws BadRequestException {

        Optional<Ticket> optionalTicket = ticketRepo.findById(ticketId);

        if(optionalTicket.isPresent()){
            logger.debug("validateTicketId: Ticket with Id {} exists.",ticketId);
            return optionalTicket.get();
        }
        logger.error("validateTicketId: Ticket with Id {} doesn't exist.",ticketId);
        throw new BadRequestException("Ticket Id doesn't exist");
    }

    public Ticket isTicketStausOpened(String ticketId , String reportedUserId) throws BadRequestException {

        Optional<Ticket> optionalTicket = ticketRepo.findByIdAndTicketStatusAndReportedUserId(ticketId , TicketStatus.OPENED , reportedUserId );

        if(optionalTicket.isPresent()){
            logger.debug("Ticket with Id {} is Opened.",ticketId);
            return optionalTicket.get();
        }

        logger.error("There's no Opened Ticket with Id {} related to user id {}.",ticketId, reportedUserId);
        throw new BadRequestException("There's no Opened Ticket with Id "+ ticketId +" related to user id "+reportedUserId+" is not Opened.");
    }

    public boolean isAssignedUserValidatedUserApi(String assignedUserId){

        logger.debug("Sending Validation Request To User Service for Assigned Id {}",assignedUserId);
        ResponseEntity<Boolean> responseEntity =
                apiCaller.callApi(userUrl,
                        validateUserPath+"/" + assignedUserId,
                        HttpMethod.GET,
                        null,
                        null,
                        Boolean.class);

        return  Boolean.TRUE.equals(responseEntity.getBody());
    }

}
