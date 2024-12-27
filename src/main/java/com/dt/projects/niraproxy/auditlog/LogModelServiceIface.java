package com.dt.projects.niraproxy.auditlog;

import java.text.ParseException;
import java.util.Date;

public interface LogModelServiceIface {
	
	public void setLogModel(Date startTime, Date endTime, String Identifier, String serviceName, String correlationID, String logMessage, String logMessageType, String niraApiFailureResponse) throws ParseException;

}
