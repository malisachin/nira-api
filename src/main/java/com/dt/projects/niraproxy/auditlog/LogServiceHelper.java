package com.dt.projects.niraproxy.auditlog;

import org.springframework.scheduling.annotation.Async;

public class LogServiceHelper {

    @Async
    static void logTokenObtained(boolean tokenObtained, String suid){
        try{
////            post the auditLog to service queue
//            String correlationID = null, transactionID = null,subTransactionID = null;
//            LocalDateTime timestamp = null;
//            String startTime = null,  endTime = null,  geoLocation = null,  callStack = null;
//            AuditLog.ServiceName serviceName = null;
//            AuditLog.TransactionType transactionType = null;
//            String transactionSubType = null;
//            AuditLog.LogMessageType logMessageType = null;
//            String logMessage = null,  serviceProviderName = null, serviceProviderAppName = null;
//            AuditLog.SignatureType signatureType = null;
//            Boolean eSealUsed = false;
//            String checksum = null;
//            AuditLog auditLog = new AuditLog(
//                    suid,
//                    correlationID,
//                    transactionID,
//                    subTransactionID,
//                    timestamp,
//                    startTime,
//                    endTime,
//                    geoLocation,
//                    callStack,
//                    serviceName,
//                    transactionType,
//                    transactionSubType,
//                    logMessageType,
//                    logMessage,
//                    serviceProviderName,
//                    serviceProviderAppName,
//                    signatureType,
//                    eSealUsed,
//                    checksum
//            );
//            if(tokenObtained){
//                auditLog.setLogMessage("NIRA Token obtained");
//            } else {
//                auditLog.setLogMessage("NIRA Token login failed");
//            }
       } catch (Exception e) {
            System.out.println("Exception Occurred in Log Service Helper : "+e.getMessage());
        }
    }
}
