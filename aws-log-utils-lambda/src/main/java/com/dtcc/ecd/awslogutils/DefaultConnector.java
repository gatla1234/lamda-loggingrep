package com.dtcc.ecd.awslogutils;

import java.util.Collection;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.services.logs.AWSLogs;
import com.amazonaws.services.logs.AWSLogsClientBuilder;
import com.amazonaws.services.logs.model.CreateLogGroupRequest;
import com.amazonaws.services.logs.model.CreateLogStreamRequest;
import com.amazonaws.services.logs.model.DeleteLogGroupRequest;
import com.amazonaws.services.logs.model.DeleteLogStreamRequest;
import com.amazonaws.services.logs.model.DescribeLogGroupsResult;
import com.amazonaws.services.logs.model.DescribeLogStreamsRequest;
import com.amazonaws.services.logs.model.DescribeLogStreamsResult;
import com.amazonaws.services.logs.model.InputLogEvent;
import com.amazonaws.services.logs.model.LogGroup;
import com.amazonaws.services.logs.model.PutLogEventsRequest;
import com.amazonaws.services.logs.model.PutLogEventsResult;
import com.amazonaws.services.logs.model.ResourceAlreadyExistsException;
import com.amazonaws.services.logs.model.ServiceUnavailableException;
import com.amazonaws.services.logs.model.ResourceNotFoundException;
import com.dtcc.ecd.awslogutils.cloudwatch.CloudWatchConnector;
import com.dtcc.ecd.awslogutils.exception.LogException;
import com.dtcc.ecd.awslogutils.exception.ResiliencyException;

public class DefaultConnector implements CloudWatchConnector
{	
	private AWSLogs awsLogs;
	
	/*
	 * Constructors 
	 */
		
	public DefaultConnector(ClientConfiguration clientConfig)
	{	
		if (clientConfig != null)
			awsLogs = AWSLogsClientBuilder.standard().
				withClientConfiguration(clientConfig).build();
		else
			awsLogs = AWSLogsClientBuilder.defaultClient();
	}
	
	public DefaultConnector(AWSCredentialsProvider credProvider, ClientConfiguration clientConfig)
	{		
		this(credProvider.getCredentials(), clientConfig);		
	}
	
	public DefaultConnector(AWSCredentials creds, ClientConfiguration clientConfig)
	{
		
		awsLogs = null;		
		
		if (clientConfig != null)
			awsLogs = AWSLogsClientBuilder.standard().withCredentials(
					new AWSStaticCredentialsProvider(creds))
						.withClientConfiguration(clientConfig).build();		
		else
			awsLogs = AWSLogsClientBuilder.standard().withCredentials(
					new AWSStaticCredentialsProvider(creds)).build();			
	}
	
	
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
	@Override
	public String publishLogEvents(String groupName,
								   String streamName,
								   Collection<InputLogEvent> eventLogs,
								   String sequenceToken)
			throws LogException, ResiliencyException
	{

		PutLogEventsRequest logEvtRequest = new PutLogEventsRequest();
		
		logEvtRequest.setLogGroupName(groupName);
		logEvtRequest.setLogStreamName(streamName);
		logEvtRequest.setSequenceToken(sequenceToken);
		
		logEvtRequest.setLogEvents(eventLogs);
		
		try
		{
			PutLogEventsResult results = awsLogs.putLogEvents(logEvtRequest);
			
			return results.getNextSequenceToken();
		}
		catch(ServiceUnavailableException sue)
		{
			throw new ResiliencyException(sue);
		}
		catch(ResourceNotFoundException rnfe)
		{
			// if the stream or group was deleted externally, then attempt to fix it
			// rather than fail right away. Only do this once. if subsequest exceptions
			// are throws then fail
			
			createLogGroup(groupName);
			createLogStream(groupName, streamName);
			
			logEvtRequest.setSequenceToken(null);
			
			try
			{
				PutLogEventsResult results = awsLogs.putLogEvents(logEvtRequest);			
				return results.getNextSequenceToken();
			}
			catch(Throwable t)
			{
				throw new ResiliencyException(t);
			}
			
		}
		catch(Throwable t)
		{
			throw new LogException(t);
		}
	}

	/**
	 * Creates a new log group.
	 * 
	 * @param groupName Name of the group
	 * @throws LogException in case of pilot errors
	 * @throws ResiliencyException if the service is unavailable
	 */
	@Override
	public void createLogGroup(String groupName)
			throws LogException, ResiliencyException
	{
		CreateLogGroupRequest logGroupRequest = new CreateLogGroupRequest();
		
		logGroupRequest.setLogGroupName(groupName);
		
		try
		{
			awsLogs.createLogGroup(logGroupRequest);
		}		
		catch(ResourceAlreadyExistsException exc)
		{
			return;
		}
		catch(ServiceUnavailableException sue)
		{
			throw new ResiliencyException(sue);
		}
		catch(Throwable t)
		{
			System.out.println("Exception while creating a log group");
			throw new LogException(t);
		}
	}
	
	
	/**
	 * Deletes a log group
	 * 
	 * @param groupName Name of the group
	 * @throws LogException in case of pilot errors
	 * @throws ResiliencyException if the service is unavailable
	 */
	@Override
	public void deleteLogGroup(String groupName) throws LogException,
			ResiliencyException {
		
		DeleteLogGroupRequest req = new DeleteLogGroupRequest();
		
		req.setLogGroupName(groupName);
		
		try
		{
			awsLogs.deleteLogGroup(req);			
		}
		catch(ServiceUnavailableException sue)
		{
			throw new ResiliencyException(sue);
		}
		catch(ResourceNotFoundException rnfe)
		{
			return;
		}
		catch(Throwable t)
		{
			throw new LogException(t);
		}			
	}
	
	
	/**
	 * Returns true if the log group exists.
	 * 
	 * @param groupName Name of the group
	 * @return boolean indicating whether the group exists
	 * @throws LogException in case of pilot errors
	 * @throws ResiliencyException if the service is unavailable
	 */
	@Override
	public boolean logGroupExists(String groupName) throws LogException,
			ResiliencyException {
		
		DescribeLogGroupsResult result = null;
		
		try
		{
			result = awsLogs.describeLogGroups();
		}
		catch(ServiceUnavailableException sue)
		{
			throw new ResiliencyException(sue);
		}
		catch(Throwable t)
		{
			throw new LogException(t);
		}
		 
		for (LogGroup nextGroup : result.getLogGroups())
			if (nextGroup.getLogGroupName().equals(groupName))
				return true;
		
		return false;
	}

	/**
	 * Creates a new log stream.
	 * 
	 * @param groupName Name of the group
	 * @param logStream Name of the log stream
	 * @return the next token in case the stream already exists
	 * @throws LogException in case of pilot errors
	 * @throws ResiliencyException if the service is unavailable
	 */
	@Override
	public String createLogStream(String groupName, String logStream)
			throws LogException, ResiliencyException
	{
		CreateLogStreamRequest logStreamRequest = new CreateLogStreamRequest();
		
		logStreamRequest.setLogGroupName(groupName);
		logStreamRequest.setLogStreamName(logStream);
		
		try
		{
			awsLogs.createLogStream(logStreamRequest);
			return null;
		}
		catch(ResourceNotFoundException rnfe)
		{
			// this would indicate that the log group does not yet exist, so create it
			createLogGroup(groupName);
			
			awsLogs.createLogStream(logStreamRequest);
			return null;
		}
		catch(ResourceAlreadyExistsException exc)
		{
			return getStreamNextToken(groupName, logStream);
		}
		catch(ServiceUnavailableException sue)
		{
			throw new ResiliencyException(sue);
		}
		catch(Throwable t)
		{
			throw new LogException(t);
		}
	}
	
	/**
	 * Deletes a log stream
	 * 
	 * @param groupName Name of the group
	 * @param logStream Name of the log stream
	 * @return boolean indicating whether the group exists
	 * @throws LogException in case of pilot errors
	 * @throws ResiliencyException if the service is unavailable
	 */
	@Override
	public void deleteLogStream(String groupName, String logStream)
			throws LogException, ResiliencyException {
		
		DeleteLogStreamRequest req = new DeleteLogStreamRequest();
				
		req.setLogGroupName(groupName);
		req.setLogStreamName(logStream);
		
		try
		{
			awsLogs.deleteLogStream(req);			
		}
		catch(ServiceUnavailableException sue)
		{
			throw new ResiliencyException(sue);
		}
		catch(ResourceNotFoundException rnfe)
		{
			return;
		}
		catch(Throwable t)
		{
			throw new LogException(t);
		}		
	}
	
	/**
	 * Returns true if the log stream exists.
	 * 
	 * @param groupName Name of the group
	 * @param logStream Name of the log stream
	 * @return boolean indicating whether the stream exists
	 * @throws LogException in case of pilot errors
	 * @throws ResiliencyException if the service is unavailable
	 */
	@Override
	public boolean logStreamExists(String groupName, String streamName)
			throws LogException, ResiliencyException {
		DescribeLogStreamsRequest req = new DescribeLogStreamsRequest();
		req.setLogGroupName(groupName);
		req.setLogStreamNamePrefix(streamName);
		
		try
		{
			DescribeLogStreamsResult results = awsLogs.describeLogStreams(req);

			if (results.getLogStreams().size() == 1)
				return true;
			else
				return false;
		}
		catch(ServiceUnavailableException sue)
		{
			throw new ResiliencyException(sue);
		}
		catch(Throwable t)
		{
			throw new LogException(t);
		}
	}

	/**
	 * @param groupName
	 * @param streamName
	 * @return String with next token, or null if the stream has no events in it
	 * @throws LogException in case of pilot errors
	 * @throws ResiliencyException if the service is unavailable
	 */
	@Override
	public String getStreamNextToken(String groupName, String streamName) 
			throws LogException, ResiliencyException
	{
		DescribeLogStreamsRequest req = new DescribeLogStreamsRequest();
		req.setLogGroupName(groupName);
		req.setLogStreamNamePrefix(streamName);
		
		try
		{
			DescribeLogStreamsResult results = awsLogs.describeLogStreams(req);

			return results.getLogStreams().get(0).getUploadSequenceToken();
		}
		catch(ServiceUnavailableException sue)
		{
			throw new ResiliencyException(sue);
		}
		catch(Throwable t)
		{
			throw new LogException(t);
		}	
	}

}
