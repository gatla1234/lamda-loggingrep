package com.dtcc.ecd.awslogutils.cloudwatch;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import com.amazonaws.services.logs.model.InputLogEvent;
import com.dtcc.ecd.awslogutils.exception.LogException;
import com.dtcc.ecd.awslogutils.exception.ResiliencyException;
import com.google.common.util.concurrent.RateLimiter;

/**
 * The stream class models a CloudWatch Stream and its characteristics.
 * 
 * @author mhanegra
 *
 */
public class Stream
{
	private String groupName;
	private String streamName;
	private String sequenceToken;
	
	// Used to automatically throttle calls to cloudwatch
	private RateLimiter cwAPIThrottle;
	
	Collection<InputLogEvent> queuedEvents;
	
	/**
	 * Creates a new Stream object
	 * 
	 * @param groupName Name of the group where this stream will be written to
	 * @param streamName Stream name
	 * @param nextToken current sequence token associated with this stream.
	 */
	public Stream(String groupName, String streamName, String nextToken)
	{
		this.groupName = groupName;
		this.streamName = streamName;
		this.sequenceToken = nextToken;
		
		queuedEvents = new ArrayList<InputLogEvent>();
		
		cwAPIThrottle = RateLimiter.create(5.0);
	}
	
	/**
	 * Updates the Stream's next token
	 * @param nextToken Next sequence token associated with the Stream
	 */
	public void updateSequenceToken(String nextToken)
	{
		this.sequenceToken = nextToken;
	}
	
		
	
	/**
	 * Publishes an event to CloudWatch logs
	 * 
	 * @param cwClient CloudWatch Connector
	 * @param eventText log event text
	 * @throws LogException in case of a setup or pilot error
	 * @throws ResiliencyException in case the service is not available
	 */
	public synchronized void publishEvent(CloudWatchConnector cwClient, String eventText)
		throws LogException, ResiliencyException
	{
		Collection<InputLogEvent> eventLogs = new ArrayList<InputLogEvent>();
		InputLogEvent logEvent = new InputLogEvent();
		logEvent.setMessage(eventText);
		logEvent.setTimestamp(new Date().getTime());
		
		cwAPIThrottle.acquire(1);
				
		eventLogs.add(logEvent);
				
		String nextToken = cwClient.publishLogEvents(groupName, streamName, eventLogs, sequenceToken);
		updateSequenceToken(nextToken);		
	}
	
}
