package com.dtcc.ecd.awslogutils.lambda.Testing;

import org.apache.log4j.Logger;
import org.apache.log4j.MDC;

import com.dtcc.ecd.awslogutils.BuiltinLoggers;
import com.dtcc.ecd.awslogutils.DefaultConnector;
import com.dtcc.ecd.awslogutils.LoggerFactory;
import com.dtcc.ecd.awslogutils.exception.LogException;
import com.dtcc.ecd.awsutils.auth.AWSAuthUtils;
import com.dtcc.ecd.awsutils.log.AWSAuthSimpleLogger;

public class LogTester {
	
	/**
	 * @param args
	 */
	
	public static void initializeLogger()
	{
		AWSAuthUtils awsAuthUtils = new AWSAuthUtils(new AWSAuthSimpleLogger(true));		      
		DefaultConnector cwConnector = null;	
		
		if (awsAuthUtils.isUseProxy()) 	            	             
          cwConnector = new DefaultConnector(awsAuthUtils.getCredentials(), 
        		  						  awsAuthUtils.getClientConfiguration());	             
        else 
      	  cwConnector = new DefaultConnector(awsAuthUtils.getCredentials(), null);
		
		
		try {
			LoggerFactory.configureLog4j("ECD", "LambdaTester", null, cwConnector);			
		} catch (LogException e) {
			System.out.println("Could not initialize Logger because: " + e);
		}

	}
	
	public static void main(String[] args) {		
		
		initializeLogger();
		
		// put some values in the MDC. These should be preserved everywhere
		MDC.put("CORRELATION_ID", "ABC");
		MDC.put("TID", "XYZ");
		MDC.put("CUSIP", "value 3");
		
		Logger appLogger = LoggerFactory.getLogger(BuiltinLoggers.DTCC_CLOUDWATCH_LOGGER);
		appLogger.debug("this is a debug message");
		appLogger.info("this is a informational message");		
		appLogger.warn("this is a warning message");
		appLogger.error("this is an error message");
		appLogger.fatal("this is an fatal message");
		
	}

}
