package com.dtcc.ecd.awslogutils.log4j;

import org.apache.log4j.spi.LoggingEvent;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.log4j.LambdaAppender;
import com.dtcc.ecd.awslogutils.LoggerFactory;
import com.dtcc.ecd.awslogutils.cloudwatch.CloudWatchConnector;
import com.dtcc.ecd.awslogutils.cloudwatch.LoggingConstants;
import com.dtcc.ecd.awslogutils.cloudwatch.LoggingUtils;
import com.dtcc.ecd.awslogutils.cloudwatch.Stream;
import com.dtcc.ecd.awslogutils.cloudwatch.StreamManager;
import com.dtcc.ecd.awslogutils.exception.LogException;
import com.dtcc.ecd.awslogutils.exception.ResiliencyException;

public class CloudWatchAppender extends LambdaAppender {
	
	private CloudWatchConnector cwClientInterface;
	private StreamManager streamManager;
	private String streamSuffix;
	private String AWSRequestID;
	boolean initialized;
	
	public CloudWatchAppender()
	{
		super();
		cwClientInterface = null;
		streamManager = new StreamManager();
		streamSuffix = "";
		initialized = false;
	}
	
	public StreamManager getStreamManager()
	{
		return streamManager;
	}
	
	public void customInit(CloudWatchConnector cwClientInterface, String streamSuffix)
	{
		this.cwClientInterface = cwClientInterface;
		this.streamSuffix = streamSuffix;
		
		// when running locally make up the request ID. this will reduce the number of streams
		// produced by the logger
		if (LoggerFactory.getContext() == null)
			AWSRequestID = LoggingUtils.getUUID();
		
		initialized = true;
	}
	
	@Override
	protected void append(LoggingEvent event)
	{
		String logMessage = getLayout().format(event);
		
		logMessage += " " + LoggingConstants.LOGGER_VERSION;
		
		if (!initialized)
		{
			System.out.println(logMessage + " : (uninitialized logger)");
			return;
		}
		
		try {
			Stream s = streamManager.getStream(cwClientInterface, getGroupName(), getStreamName());
			s.publishEvent(cwClientInterface, logMessage);
		} catch (LogException | ResiliencyException e) {
			System.out.println(logMessage + " : (exception) " + e.getMessage());
			return;
		} 
	}
	
	
	protected String getGroupName()
	{
		String groupName = String.format("/DTCC/%s", LoggerFactory.getSYSID()).toUpperCase();
		return groupName;
	}
	
	protected String getStreamName()
	{
		Context context = LoggerFactory.getContext();
		
		String utcDate = LoggingUtils.getUTCDate(); 
		
		if (context == null)
		{
			String streamName = String.format("%s.%s.%s.%s.%s.%s", utcDate, LoggerFactory.getSYSID(), LoggerFactory.getApplicationName(), AWSRequestID, Thread.currentThread().getName(), streamSuffix).toLowerCase();
			return streamName;
		}
		else
		{
			String functionName    = context.getFunctionName();			
			String functionVersion = context.getFunctionVersion();
			String requestID 	   = context.getAwsRequestId();
			
			String streamName = String.format("%s.%s.%s.%s.%s.%s", utcDate, functionName, functionVersion, Thread.currentThread().getName(), requestID, streamSuffix).toLowerCase();
			return streamName;
		}			
	}

}
