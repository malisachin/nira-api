package com.dt.projects.niraproxy.api;

import com.dt.projects.niraproxy.auditlog.LogMessageType;
import com.dt.projects.niraproxy.auditlog.LogModelServiceIface;
import com.dt.projects.niraproxy.auditlog.ServiceNames;
import com.dt.projects.niraproxy.controller.AppUtil;
import com.dt.projects.niraproxy.util.ApiResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@EnableScheduling
public class ProxyServiceImpl implements ProxyService{
    @Value("${nira.configFile}")
    String configFile;
    @Value(("${nira.resetPwdLimitInDays}"))
    int resetPwdLimitInDays;

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    LogModelServiceIface logModelService;

    @Override
    public ApiResponse getToken(String identifier) throws ParseException {
        Date startTime = new Date();
        try {
            HttpHeaders headers = new HttpHeaders();
            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            headers.set("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
            headers.set("Authorization", "Basic " + ServiceHelper.userCredentialsToken());
            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, headers);
            map.add("grant_type", "client_credentials");
            ResponseEntity<String> response = restTemplate.exchange(ServiceHelper.tokenUrl, HttpMethod.POST, entity,
                    String.class);
            ObjectMapper mapper = new ObjectMapper();
            if (response.getStatusCode().equals(HttpStatus.OK)) {
                JsonNode root = mapper.readTree(response.getBody());
                Date endTime = new Date();
                String logMessage = String.format("NIRA API Login success in %.2f seconds",
                        AppUtil.getDifferenceInSeconds(startTime, endTime));
                logModelService.setLogModel(
                        startTime,
                        endTime,
                        identifier,
                        ServiceNames.NIRA_API.name().toString(),
                        AppUtil.getCorrelationID(),
                        logMessage,
                        LogMessageType.SUCCESS.name().toString(),
                        null
                    );
                return AppUtil.createApiResponse(true, "done", root);
            } else {
                throw new Exception("Error while login");
            }
            } catch (Exception e){
            Date endTime = new Date();
            String logMessage = String.format("NIRA API Login failed in %.2f seconds", AppUtil.getDifferenceInSeconds(startTime,endTime));
            logModelService.setLogModel(
                    startTime,
                    new Date(),
                    identifier,
                    ServiceNames.NIRA_API.name().toString(),
                    AppUtil.getCorrelationID(),
                    logMessage+" "+e.getMessage() ,
                    LogMessageType.FAILURE.name().toString(),
                    null
            );
            return AppUtil.createApiResponse(false, "fail",e.getMessage());
        }
    }

    @Override
    public ApiResponse getPerson(String identifier, String nationalId, String token) {
        try {
            HttpHeaders headers = getHeaders(token);
            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, headers);
            String url = String.format(ServiceHelper.getIdCardUrl, nationalId);
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    String.class);
            ObjectMapper mapper = new ObjectMapper();
            if (response.getStatusCode().equals(HttpStatus.OK)) {
                JsonNode root = mapper.readTree(response.getBody());
                return AppUtil.createApiResponse(true, "done", root);
            } else {
                JsonNode root = mapper.readTree(response.getBody());
                return AppUtil.createApiResponse(false, "fail", root);
            }
        } catch (Exception e){
            return AppUtil.createApiResponse(false, "fail",e.getMessage());
        }
    }

    @Override
    public ApiResponse getIdCard(String identifier, String cardNumber, String token) throws ParseException, JsonProcessingException {
        Date startTime = new Date();
        String errorMsg  = null;
        try {
            HttpHeaders headers = getHeaders(token);
            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, headers);
            String url = String.format(ServiceHelper.getIdCardUrl, cardNumber);
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    String.class);
            ObjectMapper mapper = new ObjectMapper();
//            System.out.println(response);
            if (response.getStatusCode().equals(HttpStatus.OK)) {
                JsonNode root = mapper.readTree(response.getBody());
                Date endTime = new Date();
                String logMessage = String.format("NIRA getIdByCardNumber success in %.2f seconds",
                AppUtil.getDifferenceInSeconds(startTime, endTime));
                logModelService.setLogModel(
                        startTime,
                        endTime,
                        identifier,
                        ServiceNames.NIRA_API.name().toString(),
                        AppUtil.getCorrelationID(),
                        logMessage,
                        LogMessageType.SUCCESS.name().toString(),null
                );
                return AppUtil.createApiResponse(true, "done", root);
            } else {
               throw new Exception("Error while getIdByCardNumber. NIRA Status Code: "+ response.getStatusCode());
            }
        }  catch (HttpClientErrorException e) {
            String responseBodyAsString = e.getResponseBodyAsString();
            try{
                Date endTime = new Date();
                JsonNode root =  new ObjectMapper().readTree(responseBodyAsString);
                String msg = root.get("return").get("transactionStatus").get("error").get("message").asText();
                String logMessage = String.format("NIRA getIdByCardNumber HttpClientError Exception in %.2f seconds",
                        AppUtil.getDifferenceInSeconds(startTime, endTime));
                logModelService.setLogModel(
                        startTime,
                        endTime,
                        identifier,
                        ServiceNames.NIRA_API.name().toString(),
                        AppUtil.getCorrelationID(),
                        logMessage+" "+msg,
                        LogMessageType.SUCCESS.name().toString(),
                        root.toString()
                );
                return AppUtil.createApiResponse(true, msg, root);

            } catch (Exception ex){
                errorMsg = responseBodyAsString;
            }
        } catch (Exception e){
           errorMsg = e.getMessage();
        }

        Date endTime = new Date();
        String logMessage = String.format("NIRA getIdByCardNumber failed in %.2f seconds",
                AppUtil.getDifferenceInSeconds(startTime, endTime));
        logModelService.setLogModel(
                startTime,
                endTime,
                identifier,
                ServiceNames.NIRA_API.name().toString(),
                AppUtil.getCorrelationID(),
                logMessage+" "+errorMsg,
                LogMessageType.FAILURE.name().toString(),
                null
        );
        return AppUtil.createApiResponse(false, errorMsg, null);
    }

    public JsonNode changePassword(String token, String encryptedPassword, String encryptedOldPassword) throws ParseException {
        Date startTime = new Date();
        try {
            HttpHeaders headers = getHeaders(token);
            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, headers);
            map.add("newPassword", encryptedPassword);

            ResponseEntity<String> response = restTemplate.exchange(
                    ServiceHelper.changePassword,
                    HttpMethod.POST,
                    entity,
                    String.class);
            ObjectMapper mapper = new ObjectMapper();
            if (response.getStatusCode().equals(HttpStatus.OK)) {
                JsonNode root = mapper.readTree(response.getBody());
               // System.out.println(root);
                if(root.get("return").get("transactionStatus").get("transactionStatus")
                        .asText(null).toLowerCase(Locale.ROOT).equals("ok")){
                    Date endTime = new Date();
                    String logMessage = String.format("NIRA PasswordChange ("+encryptedOldPassword+"|"+encryptedPassword+") success in %.2f seconds",
                            AppUtil.getDifferenceInSeconds(startTime, endTime));
                    logModelService.setLogModel(
                            startTime,
                            endTime,
                            "SELF",
                            ServiceNames.NIRA_API.name().toString(),
                            AppUtil.getCorrelationID(),
                            logMessage,
                            LogMessageType.SUCCESS.name().toString(),
                            null
                    );
                    return root;
                }
            } else {
                throw new Exception("Error while change password");
            }
        } catch (Exception e){
            Date endTime = new Date();
            String logMessage = String.format("NIRA PasswordChange ("+encryptedOldPassword+"|"+encryptedPassword+") failed in %.2f seconds",
                    AppUtil.getDifferenceInSeconds(startTime, endTime));
            logModelService.setLogModel(
                    startTime,
                    endTime,
                    "SELF",
                    ServiceNames.NIRA_API.name().toString(),
                    AppUtil.getCorrelationID(),
                    logMessage+" "+e.getMessage(),
                    LogMessageType.FAILURE.name().toString(),
                    null
            );
        }
        return null;
    }

    HttpHeaders getHeaders(String token){
        try {
            File file = new File(configFile);
            ObjectMapper mapper = new ObjectMapper();
            NiraCredentials niraCredentials = mapper.readValue(file, NiraCredentials.class);

//             create nira-nonce
            SecureRandom random = new SecureRandom();
            byte nonceBytes[] = new byte[16];
            random.nextBytes(nonceBytes);
            String base64Nonce =  Base64.getEncoder().encodeToString(nonceBytes);

//            create nira-created and nira-created-alt
            SimpleDateFormat sdfForCreated  = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX"); //With last colon
            sdfForCreated.setTimeZone(TimeZone.getTimeZone("Africa/Nairobi"));
            SimpleDateFormat sdfForPassword = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ"); //Without last colon
            sdfForPassword.setTimeZone(TimeZone.getTimeZone("Africa/Nairobi"));
            Date oCreated  = new Date();
            String created = sdfForCreated.format(oCreated);

//            create nira-auth-forward
            MessageDigest sha1PasswordOnlyDigest = MessageDigest.getInstance("SHA-1");
            sha1PasswordOnlyDigest.update(niraCredentials.getPassword().getBytes());
            byte [] sha1PasswordBytes = sha1PasswordOnlyDigest.digest();

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            outputStream.write(nonceBytes);
            outputStream.write(sdfForPassword.format(oCreated).getBytes());
            outputStream.write(sha1PasswordBytes);

            MessageDigest sha1PasswordMessageDigest = MessageDigest.getInstance("SHA-1");
            sha1PasswordMessageDigest.update(outputStream.toByteArray());

            byte [] sha1PasswordDigest = sha1PasswordMessageDigest.digest();

            String base64PasswordDigest = Base64.getEncoder().encodeToString(sha1PasswordDigest);

            ByteArrayOutputStream outputStream2 = new ByteArrayOutputStream();
            outputStream2.write(niraCredentials.getUsername().getBytes());
            outputStream2.write(":".getBytes());
            outputStream2.write(base64PasswordDigest.toString().getBytes());
            String niraAuthForward = Base64.getEncoder().encodeToString(
                    outputStream2.toByteArray()
            );
//            prepare headers
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            headers.add("nira-nonce",base64Nonce);
            headers.add("nira-created", created);
            headers.add("nira-auth-forward", niraAuthForward);
            return headers;
        } catch (Exception e){
            System.out.println(e.getMessage());
        }
        return null;
    }


 //   @Scheduled(fixedDelay = 86400000) // run every 24hrs
    public void passwordResetScheduler() throws IOException, NoSuchPaddingException, IllegalBlockSizeException, CertificateException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
//        read nira credentials file and check if password need reset
//        if yes, invoke changePassword(String token, String encryptedPassword)
//        do nothing otherwise
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//        try{
//            long s = sdf.parse("2022-01-01").getTime();
//            System.out.println(s);
//        } catch (Exception e){
//            System.out.println(e.getMessage());
//        }
        File file = new File(configFile);
        ObjectMapper mapper = new ObjectMapper();
        NiraCredentials niraCredentials = mapper.readValue(file, NiraCredentials.class);
       // System.out.println(niraCredentials);
        if(isPasswordNeedReset(niraCredentials)){
            String newPassword = PwdServiceHelper.generateDynamicPwd(niraCredentials.getPassword());
            String encryptedPassword = ServiceHelper.getEncryptedPassword(newPassword);
            String encryptedOldPassword = ServiceHelper.getEncryptedPassword(niraCredentials.getPassword());
            try{
                ApiResponse apiResponse = getToken("SELF");
                if(apiResponse.isSuccess()){
                    JsonNode root = mapper.readTree(
                            apiResponse.getResult().toString()
                    );
                    String accessToken = root.get("access_token").asText(null);
                    if(accessToken!=null){
                        System.out.println("invoke changePassword");
                        JsonNode jsonNode = null;
                        jsonNode = changePassword(accessToken, encryptedPassword, encryptedOldPassword);
                        if(jsonNode!=null){
                            int passwordDaysLeft = jsonNode.get("return").get("transactionStatus")
                                    .get("passwordDaysLeft").asInt();
                            niraCredentials.setLastModifiedTime(Calendar.getInstance().getTime());
                            niraCredentials.setPassword(newPassword);
                            niraCredentials.setNoOfDaysLeft(passwordDaysLeft);
                            try ( FileWriter fileWriter = new FileWriter(configFile)){
                                fileWriter.write(mapper.valueToTree(niraCredentials).toString());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            System.out.println(niraCredentials);
                        }
                    }
                } else {
                    System.out.println("password reset failed while getting accessToken");
                }
            } catch (Exception e){
                System.out.println(e.getMessage());
            }
        }
    }

    private Boolean isPasswordNeedReset(NiraCredentials niraCredentials) {
        Calendar pwdExpiryDate = Calendar.getInstance();
        pwdExpiryDate.setTime(niraCredentials.getLastModifiedTime());
        pwdExpiryDate.add(Calendar.DATE, niraCredentials.getNoOfDaysLeft());
        long diff =  pwdExpiryDate.getTimeInMillis() - Calendar.getInstance().getTimeInMillis();
        int diffDays = (int) (diff / (24 * 60 * 60 * 1000)); // convert difference to Days
        System.out.println("password about to expire in "+diffDays+" days");
        return (diffDays <= resetPwdLimitInDays);
    }
}
