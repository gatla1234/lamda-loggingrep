package com.dtcc.ecd.awslogutils;


import org.apache.log4j.Logger;
import org.apache.log4j.MDC;

import com.amazonaws.services.lambda.runtime.Context;
import com.dtcc.ecd.awslogutils.cloudwatch.CloudWatchConnector;
import com.dtcc.ecd.awslogutils.cloudwatch.LoggingConstants;
import com.dtcc.ecd.awslogutils.cloudwatch.Validator;
import com.dtcc.ecd.awslogutils.exception.LogException;
import com.dtcc.ecd.awslogutils.log4j.CloudWatchAppender;

/**
 * This is the main class used to properly initialize log4j and 
 * create new logger objects.
 * 
 * @author mhanegra
 *
 */
public class LoggerFactory {
	
	private static Context functionContext;
	private static CloudWatchConnector cwClientInterface;
	
	private static String SYSID;
	private static String applicationName;
	
	private static boolean initialized = false;
	
	
	// name of the appender that needs to be reconfigured
	private final static String DTCC_CLOUDWATCH_APPENDER_NAME = "DTCC_CLOUDWATCH_APPENDER";
	
	
	/**
	 * Configures Log4j custom appenders and populates the MDC
	 * with all proper values.
	 * 
	 * @param sysID Application SYSID used to determine target
	 *        log group.
	 * @param appName Application Name
	 * @param ctx Lambda Context
	 * @param cwClientOverride optional, user supplied connector
	 * @throws LogException if it cannot validate the parameters.
	 */
	public static void configureLog4j(String sysID, String appName, Context ctx,
			CloudWatchConnector cwClientOverride)
		throws LogException
	{
		if (initialized)
			return;
		
		SYSID = sysID;
		applicationName = appName;
		
		Validator.validateSYSID(SYSID);
		Validator.validateApplicationName(appName);	
			
		functionContext = ctx;
		
		if (cwClientOverride == null)
		{			
			cwClientInterface = new DefaultConnector(null);
		}
		else
			cwClientInterface = cwClientOverride;
		
		configureDTCCCWLogAppender();
		populateMDC();
		
		initialized = true;
	}
	
	/**
	 * Returns a log4j logger given the logger name
	 * @param loggerName Name of the logger to return. If null then "BuiltinLoggers.DTCC_CLOUDWATCH_LOGGER"
	 * 		  will be returned.
	 * @return A log4j logger
	 */
	public static Logger getLogger(String loggerName)
	{
		populateMDC();
		
		if (!initialized)
			return Logger.getLogger(BuiltinLoggers.DEFAULT_LAMBDA_LOGGER);
		
		// return the built-in one.
		if (loggerName == null)
			return Logger.getLogger(BuiltinLoggers.DTCC_CLOUDWATCH_LOGGER);
		
		return Logger.getLogger(loggerName);
	}
	
	/**
	 * Returns a logger using the AWS supplied, console based appender.
	 * @return The AWS built log4j logger
	 */
	public static Logger getConsoleLambdaLogger()
	{
		return Logger.getLogger(BuiltinLoggers.DEFAULT_LAMBDA_LOGGER);
	}
	
	/**
	 * Returns the SYSID associated with the application using
	 * this logger
	 * @return application's SYSID
	 */
	public static String getSYSID()
	{
		return SYSID;
	}
	
	/**
	 * Returns the name of the application using this logger
	 * @return application name
	 */
	public static String getApplicationName()
	{
		return applicationName;
	}
	
	/**
	 * Populates the MCD with a set of Logger defined values.
	 */
	private static void populateMDC()
	{
		//add basic information to the MDC
		MDC.put(LoggingConstants.SYSID, SYSID);
		MDC.put(LoggingConstants.APPLICATION_NAME, applicationName);
		
		if (functionContext != null)
		{
			MDC.put(LoggingConstants.AWS_LAMBDA_FUNCTION_NAME, functionContext.getFunctionName());
			MDC.put(LoggingConstants.AWS_LAMBDA_FUNCTION_VERSION, functionContext.getFunctionVersion());
			MDC.put(LoggingConstants.AWS_REQUEST_ID, functionContext.getAwsRequestId());
		}
	}
	
	
	
	/**
	 * returns the Lambda Context object supplied to this class during initialization
	 * @return Lambda Context object
	 */
	public static Context getContext()
	{
		return functionContext;
	}
	
	
	/**
	 * Private helper function used to configure the DTCC CloudWatch appender
	 * @throws LogException in case of a log4j configuration error
	 */
	private static void configureDTCCCWLogAppender()
		throws LogException
	{
		CloudWatchAppender cwAppender = null;
		
		// configure the Simple CloudWatch Appender
		try
		{
			cwAppender = (CloudWatchAppender) Logger.getLogger(BuiltinLoggers.DTCC_CLOUDWATCH_LOGGER).getAppender(DTCC_CLOUDWATCH_APPENDER_NAME);
		}
		catch(Throwable t)
		{
			//catch runtime exceptions
			throw new LogException(t.getMessage());
		}
		
		if (cwAppender == null)
			throw new LogException(DTCC_CLOUDWATCH_APPENDER_NAME + " appender was not found in log4j.properties");
		
		cwAppender.customInit(cwClientInterface, "simple_fragment");
		
		
		// clear the stream manager, since it will may be reused by subsequent calls to this function
		cwAppender.getStreamManager().removeStreams();
	}
}
