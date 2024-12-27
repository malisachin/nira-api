package com.dt.projects.niraproxy.auditlog;

import com.dt.projects.niraproxy.controller.AppUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ug.daes.DAESService;
import ug.daes.Result;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
//import java.util.UUID;

@Service
public class LogModelServiceImpl implements LogModelServiceIface {
	@Autowired
	RabbitMQSender mqSender;

	@Override
	public void setLogModel(Date startTime, Date endTime, String Identifier, String serviceName, String correlationID,String logMessage,String logMessageType, String niraApiFailureResponse) throws ParseException  {
		LogModelDTO logModel = new LogModelDTO();
		logModel.setIdentifier(Identifier);
		logModel.setCorrelationID(correlationID);
		logModel.setTransactionID(correlationID);
//		logModel.setTimestamp(AppUtil.getTimeStampString(new Date()));
		logModel.setStartTime(AppUtil.getTimeStampString(startTime));
		logModel.setEndTime(AppUtil.getTimeStampString(endTime));
		logModel.setServiceName(serviceName);
		logModel.setTransactionType(TransactionType.SYSTEM_ACTIVITY.toString());
		logModel.setGeoLocation(null);
		logModel.seteSealUsed(false);
		logModel.setSignatureType(null);
		logModel.setCallStack(niraApiFailureResponse);
		logModel.setLogMessageType(logMessageType);
		logModel.setLogMessage(logMessage);
		logModel.setChecksum(null);
        try {
        	ObjectMapper objectMapper = new ObjectMapper();
        	String json = objectMapper.writeValueAsString(logModel);
        	Result checksumResult = DAESService.addChecksumToTransaction(json);
        	String push = new String(checksumResult.getResponse());
        	LogModelDTO logModelWithChecksum = objectMapper.readValue(push, LogModelDTO.class);
        	mqSender.send(logModelWithChecksum);
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
}
