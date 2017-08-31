package com.dtcc.ecd.awslogutils.cloudwatch;

import java.util.Collection;

import com.amazonaws.services.logs.model.InputLogEvent;
import com.dtcc.ecd.awslogutils.exception.LogException;
import com.dtcc.ecd.awslogutils.exception.ResiliencyException;

/**
 * An interface to a CloudWatch Logs connector. Despite there 
 * being one real implementation of this interface, other
 * implementations will allow for failures to be injected during
 * testing.
 * 
 * @author mhanegra
 *
 */
public interface CloudWatchConnector {
	
	/**
	 * Publishes a new batch of log events to a log group and creates the group if not there.
	 * 
	 * @param groupName Name of the destination log group
	 * @param streamName Name of the destination stream
	 * @param eventLogs batch of log events.
	 * @param sequenceToken
	 * @return
	 * @throws LogException in case of pilot errors
	 * @throws ResiliencyException if the service is unavailable
	 */
	public String publishLogEvents(String groupName, 
								   String streamName, 
								   Collection<InputLogEvent> eventLogs,
								   String sequenceToken)
			throws LogException, ResiliencyException;
	
	
	/**
	 * Creates a new log group.
	 * 
	 * @param groupName Name of the group
	 * @throws LogException in case of pilot errors
	 * @throws ResiliencyException if the service is unavailable
	 */
	public void createLogGroup(String groupName)
			throws LogException, ResiliencyException;
	
	
	
	/**
	 * Deletes a log group
	 * 
	 * @param groupName Name of the group
	 * @throws LogException in case of pilot errors
	 * @throws ResiliencyException if the service is unavailable
	 */
	public void deleteLogGroup(String groupName)
			throws LogException, ResiliencyException;
	
	
	/**
	 * Returns true if the log group exists.
	 * 
	 * @param groupName Name of the group
	 * @return boolean indicating whether the group exists
	 * @throws LogException in case of pilot errors
	 * @throws ResiliencyException if the service is unavailable
	 */
	public boolean logGroupExists(String groupName)
			throws LogException, ResiliencyException;
	
	/**
	 * Creates a new log stream.
	 * 
	 * @param groupName Name of the group
	 * @param logStream Name of the log stream
	 * @return the next token in case the stream already exists
	 * @throws LogException in case of pilot errors
	 * @throws ResiliencyException if the service is unavailable
	 */
	public String createLogStream(String groupName, String logStream)
			throws LogException, ResiliencyException;
	
	
	/**
	 * Deletes a log stream
	 * 
	 * @param groupName Name of the group
	 * @param logStream Name of the log stream
	 * @return boolean indicating whether the group exists
	 * @throws LogException in case of pilot errors
	 * @throws ResiliencyException if the service is unavailable
	 */
	public void deleteLogStream(String groupName, String logStream)
			throws LogException, ResiliencyException;
	
	
	/**
	 * Returns true if the log stream exists.
	 * 
	 * @param groupName Name of the group
	 * @param logStream Name of the log stream
	 * @return boolean indicating whether the stream exists
	 * @throws LogException in case of pilot errors
	 * @throws ResiliencyException if the service is unavailable
	 */
	public boolean logStreamExists(String groupName, String streamName)
			throws LogException, ResiliencyException;
	
	
	/**
	 * @param groupName
	 * @param streamName
	 * @return String with next token, or null if the stream has no events in it
	 * @throws LogException in case of pilot errors
	 * @throws ResiliencyException if the service is unavailable
	 */
	public String getStreamNextToken(String groupName, String streamName)
			throws LogException, ResiliencyException;
	
}
