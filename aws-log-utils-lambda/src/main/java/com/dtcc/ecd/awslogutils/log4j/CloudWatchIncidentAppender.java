package com.dtcc.ecd.awslogutils.log4j;

import org.apache.log4j.spi.LoggingEvent;

import com.amazonaws.services.lambda.runtime.log4j.LambdaAppender;
import com.dtcc.ecd.awslogutils.LoggerFactory;
import com.dtcc.ecd.awslogutils.cloudwatch.CloudWatchConnector;
import com.dtcc.ecd.awslogutils.cloudwatch.LoggingUtils;
import com.dtcc.ecd.awslogutils.cloudwatch.Stream;
import com.dtcc.ecd.awslogutils.cloudwatch.StreamManager;
import com.dtcc.ecd.awslogutils.exception.LogException;
import com.dtcc.ecd.awslogutils.exception.ResiliencyException;

/**
 * A special incident appender. This is currently unused, but it may be revived in a future
 * version of the logger
 * 
 * @author mhanegra
 *
 */
public class CloudWatchIncidentAppender extends LambdaAppender {
	
	private CloudWatchConnector cwClientInterface;
	private StreamManager streamManager;
	final private static String SHARED_INCIDENT_LOG_GROUP = "SharedApplicationIncidentLogs"; 
	
	public CloudWatchIncidentAppender()
	{
		super();
		cwClientInterface = null;
		streamManager = new StreamManager();
	}
	
	public StreamManager getStreamManager()
	{
		return streamManager;
	}
	
	public void setCloudWatchClient(CloudWatchConnector cwClientInterface)
	{
		this.cwClientInterface = cwClientInterface;
	}
	
	@Override
	protected void append(LoggingEvent event)
	{
		String logMessage = getLayout().format(event);
		try {
			Stream s = streamManager.getStream(cwClientInterface, getGroupName(), getStreamName());
			s.publishEvent(cwClientInterface, logMessage);
		} catch (LogException | ResiliencyException e) {
			System.out.println(logMessage + " : " + e.getMessage());
			return;
		}
	}
	
	
	protected static String getGroupName()
	{
		return SHARED_INCIDENT_LOG_GROUP;
	}
	
	protected static String getStreamName()
	{
		String streamName = String.format("%s.%s.%s.%s.incident", LoggingUtils.getUTCDate(), LoggerFactory.getSYSID(), LoggerFactory.getApplicationName(), LoggingUtils.getUUID()).toLowerCase();
		return streamName;
	}

}
