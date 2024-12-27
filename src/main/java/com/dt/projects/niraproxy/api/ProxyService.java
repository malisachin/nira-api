package com.dt.projects.niraproxy.api;

import com.dt.projects.niraproxy.util.ApiResponse;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.text.ParseException;

public interface ProxyService {
     ApiResponse getToken(String identifier) throws ParseException;
     ApiResponse getPerson(String identifier,String nationalId, String token);
     ApiResponse getIdCard(String identifier,String cardNumber, String token) throws ParseException, JsonProcessingException;
}
