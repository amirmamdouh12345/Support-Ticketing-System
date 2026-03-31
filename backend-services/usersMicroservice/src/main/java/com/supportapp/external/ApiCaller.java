package com.supportapp.external;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class ApiCaller {

    private final Logger logger = LoggerFactory.getLogger(ApiCaller.class);

    @Autowired
    private RestTemplate restTemplate;

    public <T,R> ResponseEntity<R> callApi(String url ,
                                     String apiPath,
                                     HttpMethod httpMethod ,
                                     T requestBody,
                                     HttpHeaders httpHeaders,
                                     Class<R> responseBody ){

        logger.info("ApiCaller Request Sending: URl: {} , HttpMethod: {} , Request Body: {}",url+apiPath,httpMethod,requestBody);

        HttpEntity<T> httpEntity = new HttpEntity<>(requestBody,httpHeaders);
        ResponseEntity<R> response= null;

        try {
           response = restTemplate.exchange(url + apiPath, httpMethod, httpEntity, responseBody);
        }catch (Exception exp){
            logger.error("ApiCaller Response Receive Exception: URl: {} , HttpMethod: {} , Request Body: {} , Response {} ",url+apiPath,httpMethod,requestBody,response);
            throw exp;
        }

        logger.info("ApiCaller Response Receive: URl: {} , HttpMethod: {} , Request Body: {} , Response Status {} , Response Body {}",url+apiPath,httpMethod,requestBody,response.getStatusCode(),response.getBody());

        return response;
    }


    public <T,R> ResponseEntity<R> callApiWithParameterizedResponseBody(String url ,
                                           String apiPath,
                                           HttpMethod httpMethod ,
                                           T requestBody,
                                           HttpHeaders httpHeaders,
                                           ParameterizedTypeReference<R> responseBody ){

        logger.info("ApiCaller Request Sending: URl: {} , HttpMethod: {} , Request Body: {}",url+apiPath,httpMethod,requestBody);

        HttpEntity<T> httpEntity = new HttpEntity<>(requestBody,httpHeaders);
        ResponseEntity<R> response= null;

        try {
            response = restTemplate.exchange(url + apiPath, httpMethod, httpEntity, responseBody);

        }catch (Exception exp){
            logger.error("ApiCaller Response Receive Exception: URl: {} , HttpMethod: {} , Request Body: {} , Response {} ",url+apiPath,httpMethod,requestBody,response);
            throw exp;
        }

        logger.info("ApiCaller Response Receive: URl: {} , HttpMethod: {} , Request Body: {} , Response Status {} , Response Body {}",url+apiPath,httpMethod,requestBody,response.getStatusCode(),response.getBody());

        return response;
    }

}
