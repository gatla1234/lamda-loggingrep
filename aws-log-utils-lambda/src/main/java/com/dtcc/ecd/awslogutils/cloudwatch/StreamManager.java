package com.dtcc.ecd.awslogutils.cloudwatch;

import java.util.HashMap;
import java.util.Map;

import com.dtcc.ecd.awslogutils.exception.LogException;
import com.dtcc.ecd.awslogutils.exception.ResiliencyException;

/**
 * The Stream manager keeps track of multiple open streams and facilitates writing to them.
 * The actual streams are stored in a map.
 * 
 * All interactions with Stream objects are done through this class.
 * @author mhanegra
 *
 */
public class StreamManager {
	
	private Map<String, Stream> streamMap;
	private final String KEY_DELIMITER = "%%@!!@%%";	
	
	/**
	 * Default constructor
	 */
	public StreamManager()
	{
		streamMap = new HashMap<String, Stream>();		
	}

	
	/**
	 * Test Constructor used by JUnit test cases.
	 * @param testMap
	 */
	public StreamManager(Map<String, Stream> testMap)
	{
		streamMap = testMap;	
	}
	
	
	/**
	 * Clears all streams from memory
	 */
	public void removeStreams()
	{
		streamMap.clear();
	}
	
	
	/**
	 * Given a group and a stream name, generates a key for the stream map
	 * @param groupName Name of the group
	 * @param streamName Name of the stream
	 * @return the Map key where to store the actual stream object
	 */
	private String getMapKey(String groupName, String streamName)
	{
		return groupName + KEY_DELIMITER + streamName;
	}
		
	/**
	 * Finds a stream in the map and returns it. If the stream does not exist, it will
	 * create a new one first.
	 * 
	 * @param cwClient CloudWatch Connector
	 * @param groupName Name of the group where this stream  will live
	 * @param streamName Name of the Stream
	 * @return Either a new or existing Stream object
	 * @throws LogException in case of pilot error
	 * @throws ResiliencyException
	 */
	synchronized public Stream getStream(CloudWatchConnector cwClient, String groupName, String streamName)
		throws LogException, ResiliencyException
	{	
		String key = getMapKey(groupName, streamName);
		
		// look for the stream in the map. if it's not there, then create it.
		Stream s = streamMap.get(key);
		
		if (s == null)
		{
			String nextToken = cwClient.createLogStream(groupName, streamName);
			
			s = new Stream(groupName, streamName, nextToken);			
			streamMap.put(key, s);
		}
		
		return s;
	}
	
	
	/**
	 * Updates a stream with the latest sequence token.
	 * @param groupName Name of the log group (used to identify the key)
	 * @param streamName Name of the log stream (used to identify the key)
	 * @param nextToken Next sequence token
	 * @throws LogException in case the stream cannot be found.
	 */
	public synchronized void updateStreamToken(String groupName, String streamName, String nextToken)
		throws LogException
	{
		String key = getMapKey(groupName, streamName);
		
		Stream s = streamMap.get(key);
		
		if (s == null)
			throw new LogException("Log Group/Stream combination does not exist: " 
							+ groupName + "/" + streamName);	
	}
	
	
}
