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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;

import static javax.swing.text.html.FormSubmitEvent.MethodType.GET;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Ticket Service Test")
class TicketServiceTest {

    // mock all dependencies you will need
    @Mock
    TicketRepo ticketRepo;

    @Spy
    TicketMapper ticketMapper = Mappers.getMapper(TicketMapper.class);

    @Mock
    MongoTemplate mongoTemplate;


    @Mock
    ApiCaller apiCaller;

    @Mock
    TicketLogService ticketLogService;

    // the service you will test
    @InjectMocks
    TicketService ticketService;


    @Nested
    @DisplayName("Find all Tickets")
    class findAllTickets {

        String ticketId;
        String reportedUserId;
        String assignedUserId;
        TicketStatus ticketStatus;
        int size;
        int page;
        Pageable pageable;
        Page<Ticket> ticketPage;
        List<Ticket> ticketList;
        Ticket ticket;
        String url = "http://localhost:8888/api/v1/users";
        String path = "/fetch/usernames";
        Set<String> reqBody ;
        HttpMethod methodType = HttpMethod.POST;

        List<UserWithFullNameDto> usernames;


        @BeforeEach
        void setup(){
            reportedUserId = "reportedId";

            assignedUserId="assignedId";

            ticketStatus = TicketStatus.OPENED;
            size=10;


            usernames = List.of(UserWithFullNameDto.builder()
                            .id(reportedUserId)
                            .fullName("Amir Mamdouh")
                            .build()

                    ,
                    UserWithFullNameDto.builder()
                            .id(assignedUserId)
                            .fullName("Ahmed Mohamed")
                            .build());

            reqBody = Set.of(reportedUserId);

            ReflectionTestUtils.setField(ticketService, "userUrl", "http://localhost:8888/api/v1/users");
            ReflectionTestUtils.setField(ticketService, "fetchUsernamesPath", "/fetch/usernames");

            page=1;
            size= 5;
            pageable= PageRequest.of(page,size);



        }

        @Test
        void shouldReturnEmptyList(){

            pageable = PageRequest.of(0,5);

            when(mongoTemplate.find(any(),eq(Ticket.class))).thenReturn(List.of());

            Page<TicketResponseDto> resultedPage = ticketService.findAll(reportedUserId,assignedUserId,ticketStatus,pageable);

            // assert
            assertEquals(0,resultedPage.getContent().size());
        }

        @Test
        void shouldReturnEmptyListWithReportedIdNull(){

            pageable = PageRequest.of(0,5);

            when(mongoTemplate.find(any(),eq(Ticket.class))).thenReturn(List.of());

            Page<TicketResponseDto> resultedPage = ticketService.findAll(null,assignedUserId,ticketStatus,pageable);

            // assert
            assertEquals(0,resultedPage.getContent().size());
        }

        @Test
        void shouldReturnEmptyListWithReportedIdNullAndAssignedIdNull(){

            pageable = PageRequest.of(0,5);

            when(mongoTemplate.find(any(),eq(Ticket.class))).thenReturn(List.of());

            Page<TicketResponseDto> resultedPage = ticketService.findAll(null,null,ticketStatus,pageable);

            // assert
            assertEquals(0,resultedPage.getContent().size());
        }

        @Test
        void shouldReturnEmptyListWithTicketStatusNull(){

            pageable = PageRequest.of(0,5);

            when(mongoTemplate.find(any(),eq(Ticket.class))).thenReturn(List.of());

            Page<TicketResponseDto> resultedPage = ticketService.findAll(reportedUserId,assignedUserId,null,pageable);

            // assert
            assertEquals(0,resultedPage.getContent().size());
        }

        @Test
        void shouldReturnEmptyListAllNull(){

            pageable = PageRequest.of(0,5);

            when(mongoTemplate.find(any(),eq(Ticket.class))).thenReturn(List.of());

            Page<TicketResponseDto> resultedPage = ticketService.findAll(null,null,null,pageable);

            // assert
            assertEquals(0,resultedPage.getContent().size());
        }

        @Test
        void shouldReturnOneTicket(){
            ticketId = "ticketId";

            pageable = PageRequest.of(0,5);

            ticket = Ticket.builder()
                    .id(ticketId)
                    .assignedUserId(assignedUserId)
                    .reportedUserId(reportedUserId)
                    .build();

            reqBody = Set.of(

                    reportedUserId,
                    assignedUserId
            );
            when(mongoTemplate.find(any(),eq(Ticket.class))).thenReturn(List.of(ticket));
            when(mongoTemplate.count(any(),eq(Ticket.class))).thenReturn(1L);
            when(
                    apiCaller.callApiWithParameterizedResponseBody(
                            eq(url),
                            eq(path),
                            eq(HttpMethod.POST),
                            eq(reqBody),
                            isNull(),
                            any()
                    )).thenReturn(ResponseEntity.ok(usernames));

            Page<TicketResponseDto> resultedPage = ticketService.findAll(null,null,null,pageable);

            // assert
            assertEquals(1,resultedPage.getContent().size());
            assertEquals(ticketId,resultedPage.getContent().getFirst().getId());
            assertEquals(assignedUserId,resultedPage.getContent().getFirst().getAssignedUserId());
            assertEquals("Ahmed Mohamed",resultedPage.getContent().getFirst().getAssignedUserFullName());
            assertEquals(reportedUserId,resultedPage.getContent().getFirst().getReportedUserId());
            assertEquals("Amir Mamdouh",resultedPage.getContent().getFirst().getReportedUserFullName());
        }



        @Test
        void shouldThrowExceptionCauseExceptionCallAPI(){
            ticketId = "ticketId";

            pageable = PageRequest.of(0,5);

            ticket = Ticket.builder()
                    .id(ticketId)
                    .assignedUserId(assignedUserId)
                    .reportedUserId(reportedUserId)
                    .build();

            reqBody = Set.of(

                    reportedUserId,
                    assignedUserId
            );
            when(mongoTemplate.find(any(),eq(Ticket.class))).thenReturn(List.of(ticket));
            when(mongoTemplate.count(any(),eq(Ticket.class))).thenReturn(1L);
            when(
                    apiCaller.callApiWithParameterizedResponseBody(
                            eq(url),
                            eq(path),
                            eq(HttpMethod.POST),
                            eq(reqBody),
                            isNull(),
                            any()
                    )).thenReturn(ResponseEntity.ok(null));

            // assert
            assertThrows(RuntimeException.class,()->{
                ticketService.findAll(null,null,null,pageable);
            });


        }

        @Test
        void shouldUserApiReturnEmptyListAndFullNamesBeDefaultValues(){
            ticketId = "ticketId";

            pageable = PageRequest.of(0,5);

            ticket = Ticket.builder()
                    .id(ticketId)
                    .assignedUserId(assignedUserId)
                    .reportedUserId(reportedUserId)
                    .build();

            reqBody = Set.of(

                    reportedUserId,
                    assignedUserId
            );
            when(mongoTemplate.find(any(),eq(Ticket.class))).thenReturn(List.of(ticket));
            when(mongoTemplate.count(any(),eq(Ticket.class))).thenReturn(1L);
            when(
                    apiCaller.callApiWithParameterizedResponseBody(
                            eq(url),
                            eq(path),
                            eq(HttpMethod.POST),
                            eq(reqBody),
                            isNull(),
                            any()
                    )).thenReturn(ResponseEntity.ok(List.of()));

            // call
            Page<TicketResponseDto> resultedPage = ticketService.findAll(null,null,null,pageable);


            //assert
            assertEquals(assignedUserId,resultedPage.getContent().getFirst().getAssignedUserId());
            assertEquals(Constants.UNASSIGNED_USER,resultedPage.getContent().getFirst().getAssignedUserFullName());
            assertEquals(reportedUserId,resultedPage.getContent().getFirst().getReportedUserId());
            assertEquals(Constants.NO_REPORTED_USER,resultedPage.getContent().getFirst().getReportedUserFullName());



        }





    }





    @Nested
    @DisplayName("Find Ticket by Id")
    class findByTicketId{
        String ticketId ;
        String url;
        String path;
        Set<String> reqBody ;
        HttpMethod methodType;

        ParameterizedTypeReference<List<UserWithFullNameDto>> responseBody = new ParameterizedTypeReference<List<UserWithFullNameDto>>() {
        };

        List<UserWithFullNameDto> usernames;

        ResponseEntity<List<UserWithFullNameDto>> responseEntity ;

        //arrange
        Ticket ticket;

        String reportedUserId ;

        TicketStatus ticketStatus ;

        @BeforeEach
        void setup(){
            ticketId= "ticket-123";
            reportedUserId = "user-123";

            ReflectionTestUtils.setField(ticketService, "userUrl", "http://localhost:8888/api/v1/users");
            ReflectionTestUtils.setField(ticketService, "fetchUsernamesPath", "/fetch/usernames");


            url = "http://localhost:8888/api/v1/users";
            path = "/fetch/usernames";
            methodType = HttpMethod.POST;
            ticketStatus = TicketStatus.OPENED;



        }



        @Test
        void shouldReturnThrowExceptionBasedOnNotExistingId() {

            //arrange
            String ticketId="unknown";
            Optional<Ticket> optionalTicket = Optional.empty();

            // empty list response
            responseEntity = ResponseEntity.ok(new ArrayList<>());

            when(ticketRepo.findById(ticketId)).thenReturn(optionalTicket);

            // call
            // assert
//            assertNull(resultedTickerResponse);
            assertThrows(BadRequestException.class,()->{
                ticketService.findById(ticketId);
            });

        }


        @Test
        void shouldThrowRuntimeExceptionCauseApiReturnsNullBody() {

            String assignedUserId = "assignedUserId";
            String reportedUserId = "UNKNOWN";

            ticket = Ticket.builder()
                    .id(ticketId)
                    .title("title")
                    .description("description")
                    .assignedUserId(assignedUserId)
                    .reportedUserId(reportedUserId)
                    .createdAt(LocalDateTime.now().minusDays(10))
                    .lastUpdatedAt(null)
                    .ticketStatus(ticketStatus)
                    .build();


            ticketStatus = TicketStatus.OPENED;

            usernames = List.of(UserWithFullNameDto.builder()
                    .id(assignedUserId)
                    .fullName("Amir Mamdouh")
                    .build());

            responseEntity = ResponseEntity.ok(null);

            reqBody = Set.of(assignedUserId,reportedUserId);


            Optional<Ticket> optionalTicket = Optional.of(ticket);

            when(ticketRepo.findById(ticketId)).thenReturn(optionalTicket);

            when(apiCaller.callApiWithParameterizedResponseBody(
                    eq(url),
                    eq(path),
                    eq(methodType),
                    eq(reqBody),
                    isNull(),
                    eq(responseBody))).thenReturn(responseEntity);

            //call

            // assert
            assertThrows(RuntimeException.class,()->{
                ticketService.findById(ticketId);
            });

        }





        @Test
        void shouldNotValidateUsersCauseUsersAreNull() throws BadRequestException {

            String assignedUserId = null;
            String reportedUserId = null;

            ticket = Ticket.builder()
                    .id(ticketId)
                    .title("title")
                    .description("description")
                    .assignedUserId(assignedUserId)
                    .reportedUserId(reportedUserId)
                    .createdAt(LocalDateTime.now().minusDays(10))
                    .lastUpdatedAt(null)
                    .ticketStatus(ticketStatus)
                    .build();


            ticketStatus = TicketStatus.OPENED;

            responseEntity = ResponseEntity.ok(usernames);

            Optional<Ticket> optionalTicket = Optional.of(ticket);

            when(ticketRepo.findById(ticketId)).thenReturn(optionalTicket);

            //call
            TicketResponseDto resultedTickerResponse = ticketService.findById(ticketId);

            // assert
            assertEquals(Constants.NO_REPORTED_USER,resultedTickerResponse.getReportedUserFullName());
            assertEquals(Constants.UNASSIGNED_USER,resultedTickerResponse.getAssignedUserFullName());

            assertEquals("ticket-123",resultedTickerResponse.getId());
            assertEquals("title",resultedTickerResponse.getTitle());
            assertEquals("description",resultedTickerResponse.getDescription());
            assertEquals(TicketStatus.OPENED,resultedTickerResponse.getTicketStatus());

        }

        @Test
        void shouldCallUserNameApiForAssignedUserAndReturnTicketBasedOnId() throws BadRequestException {

            String assignedUserId = "assignedUserId";
            String reportedUserId = "UNKNOWN";

            ticket = Ticket.builder()
                    .id(ticketId)
                    .title("title")
                    .description("description")
                    .assignedUserId(assignedUserId)
                    .reportedUserId(reportedUserId)
                    .createdAt(LocalDateTime.now().minusDays(10))
                    .lastUpdatedAt(null)
                    .ticketStatus(ticketStatus)
                    .build();


            ticketStatus = TicketStatus.OPENED;

            usernames = List.of(UserWithFullNameDto.builder()
                    .id(assignedUserId)
                    .fullName("Amir Mamdouh")
                    .build());

            responseEntity = ResponseEntity.ok(usernames);

            reqBody = Set.of(assignedUserId,reportedUserId);


            Optional<Ticket> optionalTicket = Optional.of(ticket);

            when(ticketRepo.findById(ticketId)).thenReturn(optionalTicket);

            when(apiCaller.callApiWithParameterizedResponseBody(
                    eq(url),
                    eq(path),
                    eq(methodType),
                    eq(reqBody),
                    isNull(),
                    eq(responseBody))).thenReturn(responseEntity);

            //call
            TicketResponseDto resultedTickerResponse = ticketService.findById(ticketId);

            // assert
            assertEquals(Constants.NO_REPORTED_USER,resultedTickerResponse.getReportedUserFullName());
            assertEquals("Amir Mamdouh",resultedTickerResponse.getAssignedUserFullName());

            assertEquals("ticket-123",resultedTickerResponse.getId());
            assertEquals("title",resultedTickerResponse.getTitle());
            assertEquals("description",resultedTickerResponse.getDescription());
            assertEquals(TicketStatus.OPENED,resultedTickerResponse.getTicketStatus());

        }






        @Test
        void shouldNotCallValidateUsers() throws BadRequestException {

            String assignedUserId = null;
            String reportedUserId = null;

            ticket = Ticket.builder()
                    .id(ticketId)
                    .title("title")
                    .description("description")
                    .assignedUserId(assignedUserId)
                    .reportedUserId(reportedUserId)
                    .createdAt(LocalDateTime.now().minusDays(10))
                    .lastUpdatedAt(null)
                    .ticketStatus(ticketStatus)
                    .build();


            ticketStatus = TicketStatus.OPENED;

            responseEntity = ResponseEntity.ok(usernames);

            reqBody = Set.of();


            Optional<Ticket> optionalTicket = Optional.of(ticket);

            when(ticketRepo.findById(ticketId)).thenReturn(optionalTicket);

            //call
            TicketResponseDto resultedTickerResponse = ticketService.findById(ticketId);

            // assert
            assertEquals(Constants.NO_REPORTED_USER,resultedTickerResponse.getReportedUserFullName());
            assertEquals(Constants.UNASSIGNED_USER,resultedTickerResponse.getAssignedUserFullName());

            assertEquals("ticket-123",resultedTickerResponse.getId());
            assertEquals("title",resultedTickerResponse.getTitle());
            assertEquals("description",resultedTickerResponse.getDescription());
            assertEquals(TicketStatus.OPENED,resultedTickerResponse.getTicketStatus());

        }


        @Test
        void shouldCallUserNameApiForReportedUserAndReturnTicketBasedOnId() throws BadRequestException {

            String ticketId="ticket-123";

            String assignedUserId = null;
            String reportedUserId = "reportedUserId";

            ticketStatus = TicketStatus.OPENED;

            ticket = Ticket.builder()
                    .id(ticketId)
                    .title("title")
                    .description("description")
                    .assignedUserId(assignedUserId)
                    .reportedUserId(reportedUserId)
                    .createdAt(LocalDateTime.now().minusDays(10))
                    .lastUpdatedAt(null)
                    .ticketStatus(ticketStatus)
                    .build();


            usernames = List.of(UserWithFullNameDto.builder()
                    .id(reportedUserId)
                    .fullName("Amir Mamdouh")
                    .build());

            responseEntity = ResponseEntity.ok(usernames);

            reqBody = Set.of(reportedUserId);


            Optional<Ticket> optionalTicket = Optional.of(ticket);

            when(ticketRepo.findById(ticketId)).thenReturn(optionalTicket);

            when(apiCaller.callApiWithParameterizedResponseBody(
                    eq(url),
                    eq(path),
                    eq(methodType),
                    eq(reqBody),
                    isNull(),
                    eq(responseBody))).thenReturn(responseEntity);

            //call
            TicketResponseDto resultedTickerResponse = ticketService.findById(ticketId);

            // assert
            assertEquals("ticket-123",resultedTickerResponse.getId());
            assertEquals("title",resultedTickerResponse.getTitle());
            assertEquals("description",resultedTickerResponse.getDescription());
            assertEquals("Amir Mamdouh",resultedTickerResponse.getReportedUserFullName());
            assertEquals(Constants.UNASSIGNED_USER,resultedTickerResponse.getAssignedUserFullName());
            assertEquals(TicketStatus.OPENED,resultedTickerResponse.getTicketStatus());

        }




        @Test
        void shouldReturnThrowExceptionBasedOnNotExistingAssignedUsername() throws BadRequestException {

            //arrange

            ticket = Ticket.builder()
                    .id(ticketId)
                    .title("title")
                    .description("description")
                    .assignedUserId(Constants.UNASSIGNED_USER)
                    .reportedUserId("unknown")
                    .createdAt(LocalDateTime.now().minusDays(10))
                    .lastUpdatedAt(null)
                    .ticketStatus(ticketStatus)
                    .build();

            Optional<Ticket> optionalTicket = Optional.of(ticket);

            when(ticketRepo.findById(ticketId)).thenReturn(optionalTicket);


            //call

            // assert
            assertThrows(RuntimeException.class,()->{
                ticketService.findById(ticketId);
            });

        }



    }

    @Nested
    @DisplayName("Create a new Ticket")
    class createTicket{

        //    String userUrl="http://localhost:9999/api/v1/users/validate/assignUser";
        @Test
        void shouldTCreateTicketSuccessfully() throws BadRequestException {

            String reportedUserId = "user-123";
            String ticketId = "ticket-123";
            UserTicketRequestDto userTicketRequestDto = UserTicketRequestDto.builder()
                    .title("title")
                    .description("description")
                    .build();
            String assignedUserId = Constants.UNASSIGNED_USER;

            Ticket ticket = Ticket.builder()
                    .title("title")
                    .description("description")
                    .reportedUserId(reportedUserId)
                    .assignedUserId(assignedUserId)
                    .createdAt(null)     // frequently changes
                    .build();

            when(ticketRepo.save(any(Ticket.class))).thenReturn(Ticket.builder()
                    .id(ticketId)
                    .title(ticket.getTitle())
                    .description(ticket.getDescription())
                    .reportedUserId(ticket.getReportedUserId())
                    .ticketStatus(ticket.getTicketStatus())
                    .build());

            when(ticketLogService.createTicketLog(ticketId,reportedUserId,null,null, TicketLogAction.CREATED))
                    .thenReturn("ticketLogId");

            String testedTicketId = ticketService.createTicket(userTicketRequestDto,reportedUserId);

            assertEquals(ticketId,testedTicketId );

        }

        @Test
        void shouldThrowBadRequestExceptionWhenPassReportedUserIdNull(){
            String reportedUserId = null;
            UserTicketRequestDto userTicketRequestDto = UserTicketRequestDto.builder()
                    .title("title")
                    .description("description")
                    .build();

            assertThrows(BadRequestException.class ,()-> {
                ticketService.createTicket(userTicketRequestDto,reportedUserId);
            });



        }

        @Test
        void shouldThrowBadRequestExceptionWhenPassTicketTitleNull(){
            String reportedUserId = "reportedUserId";
            UserTicketRequestDto userTicketRequestDto = UserTicketRequestDto.builder()
                    .title(null)
                    .description("description")
                    .build();

            assertThrows(BadRequestException.class ,()-> {
                ticketService.createTicket(userTicketRequestDto,reportedUserId);
            });



        }

    }


    @Nested
    @DisplayName("Update a Ticket attributes")
    class updateTicketContent{

        String ticketId;
        UserTicketRequestDto userTicketRequestDto;
        Ticket ticket;
        Optional<Ticket> optionalTicket;

        String assignedUserId;
        String reportedUserId;
        String userUrl;
        String validateUserPath;
        TicketStatus ticketStatus;
        @BeforeEach
        void setup(){


            ReflectionTestUtils.setField(ticketService, "userUrl", "http://localhost:8888/api/v1/users");
            ReflectionTestUtils.setField(ticketService, "validateUserPath", "/validate/assignUser");


            ticketId = "ticket-123";
            assignedUserId="assigned";
            reportedUserId =  "reported";
            ticketStatus= TicketStatus.IN_PROGRESS;
            userUrl="http://localhost:8888/api/v1/users";
            validateUserPath="/validate/assignUser";


        }

        @Test
        void shouldThrowBadRequestExceptionWithIdNotExist() {
            // arrange
            ticketId = "UNKNOWN";
            reportedUserId = "UNKNOWN";
            ticketStatus= TicketStatus.OPENED;
            userTicketRequestDto = UserTicketRequestDto.builder()
                    .title("title2")
                    .build();

            optionalTicket = Optional.empty();

            when(ticketRepo.findByIdAndTicketStatusAndReportedUserId(ticketId,ticketStatus,reportedUserId)).thenReturn(optionalTicket);

            // call

            assertThrows(BadRequestException.class, () -> {
                ticketService.updateTicketContent(ticketId, reportedUserId, userTicketRequestDto);
            });

            // assert
            verify(ticketRepo, times(1)).findByIdAndTicketStatusAndReportedUserId(ticketId,ticketStatus,reportedUserId);

        }

        @Test
        void shouldUpdateUserContentSuccessfully() throws BadRequestException {
            // arrange

            reportedUserId = "reportedId";
            assignedUserId = "assignedId";
            ticketStatus= TicketStatus.OPENED;

            userTicketRequestDto = UserTicketRequestDto.builder()
                    .title("title2")
                    .build();


            ticket = Ticket.builder()
                    .id(ticketId)
                    .title("title")
                    .description("description")
                    .assignedUserId(assignedUserId)
                    .reportedUserId(reportedUserId)
                    .createdAt(LocalDateTime.now().minusDays(10))
                    .lastUpdatedAt(null)
                    .ticketStatus(ticketStatus)
                    .build();

            optionalTicket = Optional.of(ticket);

            when(ticketRepo.findByIdAndTicketStatusAndReportedUserId(ticketId,ticketStatus,reportedUserId)).thenReturn(optionalTicket);
            when(ticketRepo.save(any())).thenReturn(ticket);
            // call
            ticketService.updateTicketContent(ticketId, reportedUserId , userTicketRequestDto);

            // assert

            verify(ticketRepo,times(1)).findByIdAndTicketStatusAndReportedUserId(ticketId,ticketStatus,reportedUserId);
            verify(ticketRepo,times(1)).save(any());
        }


        @Test
        void shouldUpdateDescriptionSuccessfully() throws BadRequestException {
            // arrange

            reportedUserId = "reportedId";
            assignedUserId = "assignedId";
            ticketStatus= TicketStatus.OPENED;

            userTicketRequestDto = UserTicketRequestDto.builder()
                    .description("description")
                    .build();


            ticket = Ticket.builder()
                    .id(ticketId)
                    .title("title")
                    .description("description")
                    .assignedUserId(assignedUserId)
                    .reportedUserId(reportedUserId)
                    .createdAt(LocalDateTime.now().minusDays(10))
                    .lastUpdatedAt(null)
                    .ticketStatus(ticketStatus)
                    .build();

            optionalTicket = Optional.of(ticket);

            when(ticketRepo.findByIdAndTicketStatusAndReportedUserId(ticketId,ticketStatus,reportedUserId)).thenReturn(optionalTicket);
            when(ticketRepo.save(any())).thenReturn(ticket);
            // call
            ticketService.updateTicketContent(ticketId, reportedUserId , userTicketRequestDto);

            // assert

            verify(ticketRepo,times(1)).findByIdAndTicketStatusAndReportedUserId(ticketId,ticketStatus,reportedUserId);
            verify(ticketRepo,times(1)).save(any());

        }


    }




    @Nested
    class updateTicketStatus{

        String ticketId;

        TicketStatus ticketStatus;

        String reportedUserId;

        SupportTicketRequestDto supportTicketRequestDto;

        Ticket ticket;

        String assignedUserId;

        String url;

        String path;

        Set<String> repBody;


        @Test
        void shouldThrowIdNotFoundException(){



            when(ticketRepo.findById(ticketId)).thenReturn(Optional.empty());


            assertThrows(BadRequestException.class,()->{
                ticketService.updateTicketStatus(ticketId,assignedUserId,supportTicketRequestDto);
            });

        }

        @Test
        void shouldNotCallValidationAssignedAPI() throws BadRequestException {
            ticketId="ticketId";
            reportedUserId= "reportedUserId";

            url = "http://localhost:8888/api/v1/users";
            path = "/validate/itSupport";

            supportTicketRequestDto = SupportTicketRequestDto
                    .builder()
                    .assignedUserId(null)
                    .ticketStatus(TicketStatus.IN_PROGRESS)
                    .build();


            ReflectionTestUtils.setField(ticketService, "userUrl", "http://localhost:8888/api/v1/users");
            ReflectionTestUtils.setField(ticketService, "validateUserPath", "/validate/itSupport");


            ticket = Ticket.builder()
                    .id(ticketId)
                    .reportedUserId(reportedUserId)
                    .build();



            when(ticketRepo.findById(
                    ticketId)).thenReturn(Optional.of(ticket));


            when(apiCaller.callApi(any(),
                    any(),
                    any(),
                    any(),
                    any(),
                    any())).thenReturn(ResponseEntity.ok(Boolean.TRUE));

            ticketService.updateTicketStatus(ticketId,assignedUserId,supportTicketRequestDto);

        }



        @Test
        void shouldCallValidationAssignedAPIReturnsTrue() throws BadRequestException {
            ticketId="ticketId";
            reportedUserId= "reportedUserId";
            assignedUserId= "assignedUserId";

            url = "http://localhost:8888/api/v1/users";
            path = "/validate/itSupport";

            supportTicketRequestDto = SupportTicketRequestDto
                    .builder()
                    .assignedUserId(assignedUserId)
                    .ticketStatus(TicketStatus.IN_PROGRESS)
                    .build();

            repBody = Set.of(assignedUserId);

            ReflectionTestUtils.setField(ticketService, "userUrl", "http://localhost:8888/api/v1/users");
            ReflectionTestUtils.setField(ticketService, "validateUserPath", "/validate/itSupport");


            ticket = Ticket.builder()
                    .id(ticketId)
                    .assignedUserId(assignedUserId)
                    .reportedUserId(reportedUserId)
                    .ticketStatus(TicketStatus.CLOSED)
                    .build();



            when(ticketRepo.findById(
                    ticketId)).thenReturn(Optional.of(ticket));

            when(
                    apiCaller.callApi(
                            eq(url),
                            eq(path+"/"+assignedUserId),
                            eq(HttpMethod.GET),
                            isNull(),
                            isNull(),
                            eq(Boolean.class)
                    )).thenReturn(ResponseEntity.ok(Boolean.TRUE));

            ticketService.updateTicketStatus(ticketId, assignedUserId,  supportTicketRequestDto);

        }

        @Test
        void shouldCallValidationAssignedAPIReturnsTrueWithUpdate() throws BadRequestException {
            ticketId="ticketId";
            reportedUserId= "reportedUserId";
            assignedUserId= "assignedUserId";

            url = "http://localhost:8888/api/v1/users";
            path = "/validate/itSupport";

            supportTicketRequestDto = SupportTicketRequestDto
                    .builder()
                    .assignedUserId(assignedUserId)
                    .ticketStatus(null)
                    .build();

            repBody = Set.of(assignedUserId);

            ReflectionTestUtils.setField(ticketService, "userUrl", "http://localhost:8888/api/v1/users");
            ReflectionTestUtils.setField(ticketService, "validateUserPath", "/validate/itSupport");


            ticket = Ticket.builder()
                    .id(ticketId)
                    .assignedUserId(assignedUserId)
                    .reportedUserId(reportedUserId)
                    .ticketStatus(TicketStatus.IN_PROGRESS)
                    .build();



            when(ticketRepo.findById(
                    ticketId)).thenReturn(Optional.of(ticket));

            when(
                    apiCaller.callApi(
                            eq(url),
                            eq(path+"/"+assignedUserId),
                            eq(HttpMethod.GET),
                            isNull(),
                            isNull(),
                            eq(Boolean.class)
                    )).thenReturn(ResponseEntity.ok(Boolean.TRUE));

            ticketService.updateTicketStatus(ticketId, assignedUserId,  supportTicketRequestDto);

        }




        @Test
        void shouldCallValidationAssignedAPIReturnsTrueWithClosedStatus() throws BadRequestException {
            ticketId="ticketId";
            reportedUserId= "reportedUserId";
            assignedUserId= "assignedUserId";

            url = "http://localhost:8888/api/v1/users";
            path = "/validate/itSupport";

            supportTicketRequestDto = SupportTicketRequestDto
                    .builder()
                    .assignedUserId(assignedUserId)
                    .ticketStatus(TicketStatus.CLOSED)
                    .build();

            repBody = Set.of(assignedUserId);

            ReflectionTestUtils.setField(ticketService, "userUrl", "http://localhost:8888/api/v1/users");
            ReflectionTestUtils.setField(ticketService, "validateUserPath", "/validate/itSupport");


            ticket = Ticket.builder()
                    .id(ticketId)
                    .assignedUserId(assignedUserId)
                    .reportedUserId(reportedUserId)
                    .ticketStatus(TicketStatus.OPENED)
                    .build();



            when(ticketRepo.findById(
                    ticketId)).thenReturn(Optional.of(ticket));

            when(
                    apiCaller.callApi(
                            eq(url),
                            eq(path+"/"+assignedUserId),
                            eq(HttpMethod.GET),
                            isNull(),
                            isNull(),
                            eq(Boolean.class)
                    )).thenReturn(ResponseEntity.ok(Boolean.TRUE));

            ticketService.updateTicketStatus(ticketId, assignedUserId , supportTicketRequestDto);

        }


        @Test
        void shouldCallValidationAssignedAPIReturnsTrueWithNoUpdateStatus() throws BadRequestException {
            ticketId="ticketId";
            reportedUserId= "reportedUserId";
            assignedUserId= "assignedUserId";

            url = "http://localhost:8888/api/v1/users";
            path = "/validate/itSupport";

            supportTicketRequestDto = SupportTicketRequestDto
                    .builder()
                    .assignedUserId(assignedUserId)
                    .ticketStatus(TicketStatus.OPENED)
                    .build();

            repBody = Set.of(assignedUserId);

            ReflectionTestUtils.setField(ticketService, "userUrl", "http://localhost:8888/api/v1/users");
            ReflectionTestUtils.setField(ticketService, "validateUserPath", "/validate/itSupport");


            ticket = Ticket.builder()
                    .id(ticketId)
                    .assignedUserId(assignedUserId)
                    .reportedUserId(reportedUserId)
                    .ticketStatus(TicketStatus.OPENED)
                    .build();



            when(ticketRepo.findById(
                    ticketId)).thenReturn(Optional.of(ticket));

            when(
                    apiCaller.callApi(
                            eq(url),
                            eq(path+"/"+assignedUserId),
                            eq(HttpMethod.GET),
                            isNull(),
                            isNull(),
                            eq(Boolean.class)
                    )).thenReturn(ResponseEntity.ok(Boolean.TRUE));

            ticketService.updateTicketStatus(ticketId , assignedUserId ,supportTicketRequestDto);

        }




        @Test
        void shouldCallValidationAssignedAPIReturnsFalse() {
            ticketId="ticketId";
            reportedUserId= "reportedUserId";
            assignedUserId= "assignedUserId";

            url = "http://localhost:8888/api/v1/users";
            path = "/validate/itSupport";

            supportTicketRequestDto = SupportTicketRequestDto
                    .builder()
                    .assignedUserId(assignedUserId)
                    .ticketStatus(TicketStatus.IN_PROGRESS)
                    .build();

            repBody = Set.of(assignedUserId);

            ReflectionTestUtils.setField(ticketService, "userUrl", "http://localhost:8888/api/v1/users");
            ReflectionTestUtils.setField(ticketService, "validateUserPath", "/validate/itSupport");


            ticket = Ticket.builder()
                    .id(ticketId)
                    .assignedUserId(assignedUserId)
                    .reportedUserId(reportedUserId)
                    .build();



            when(ticketRepo.findById(
                    ticketId)).thenReturn(Optional.of(ticket));

            when(
                    apiCaller.callApi(
                            eq(url),
                            eq(path+"/"+assignedUserId),
                            eq(HttpMethod.GET),
                            isNull(),
                            isNull(),
                            eq(Boolean.class)
                    )).thenReturn(ResponseEntity.ok(Boolean.FALSE));

            assertThrows(BadRequestException.class,()->{
                ticketService.updateTicketStatus(ticketId ,assignedUserId,supportTicketRequestDto);
            });

        }
    }
}


