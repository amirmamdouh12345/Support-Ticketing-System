package com.itsupport.dtos;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;


@Getter
@Setter
public class ExceptionBody {

    private String message;
    private Integer status;
    private Timestamp timestamp;
    private String apiPath;

}
