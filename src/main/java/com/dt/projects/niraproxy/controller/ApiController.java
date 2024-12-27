package com.dt.projects.niraproxy.controller;

import com.dt.projects.niraproxy.api.ProxyService;
import com.dt.projects.niraproxy.util.ApiResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.constraints.NotNull;
import java.text.ParseException;
import java.util.*;

import static com.dt.projects.niraproxy.Constants.login;
import static com.dt.projects.niraproxy.Constants.profile;

@RestController
@Validated
public class ApiController {

    @Autowired
    ProxyService proxyService;

    @GetMapping(login)
    public ApiResponse login(@RequestHeader("daes_authorization") @NotNull String daesAuthorization,
                             @RequestHeader(value = "identifier", required = false) String identifier) throws ParseException {
        Optional<String> identifierStr;
        if(verifyDaesAuthorization(daesAuthorization)) {
            identifierStr = Optional.ofNullable(identifier);
            String d = identifierStr.isPresent() ? identifierStr.get() : null;
            return proxyService.getToken(d);
        }
        else
            return AppUtil.createApiResponse(false, "Bad Request", null);
    }
    @GetMapping(profile)
    public ApiResponse profile(
            @RequestHeader("daes_authorization") @NotNull String daesAuthorization,
            @RequestHeader("access_token") @NotNull String accessToken,
            @RequestHeader(value = "identifier", required = false) String identifier,
            @PathVariable String cardNumber
    ) throws ParseException, JsonProcessingException {
        Optional<String> identifierStr;
        if (verifyDaesAuthorization(daesAuthorization)) {
            identifierStr = Optional.ofNullable(identifier);
            String d = identifierStr.isPresent() ? identifierStr.get() : AppUtil.encrypt(cardNumber);
            return proxyService.getIdCard(d, cardNumber, accessToken);
        }
        else
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid Headers");
    }
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map<String, String> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return errors;
    }
    boolean verifyDaesAuthorization(String daesAuthorization){
       return daesAuthorization.equals("VUpneWQ3OEp9eVMvKV1WOkxKTEtoakBxZjllSlFrSA==");
    }

    @GetMapping("/hello")
    String test(){
        return "hello";
    }
}
