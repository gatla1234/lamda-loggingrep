package com.dtcc.ecd.awslogutils;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.dtcc.ecd.awslogutils.cloudwatch.CloudWatchConnector;
import com.dtcc.ecd.awslogutils.exception.LogException;
import com.dtcc.ecd.awslogutils.exception.ResiliencyException;


/**
 * CloudWatchClientUtils is a convenience class used to generate CloudWatchConnector implementations.
 * ClientConfigurations are used to configure things like a proxy connection.
 * 
 * 
 * @author mhanegra
 *
 */
public class CloudWatchClientUtils {
	
	/**
	 * Creates a properly constructed CloudWatchConnector with all the correct settings
	 * @return
	 */
	public static CloudWatchConnector getDefaultConnector(){		
			return new DefaultConnector(null);
		}
	
	
	
	/**
	 * Creates a new CloudWatchConnector using a ClientConfiguration object supplied by the user. 
	 * Typically used when connecting to AWS (CloudWatch) using a proxy.
	 * 
	 * @param clientConfig pre-configured ClientConfiguration object
	 * @return
	 * @throws LogException if clientConfig
	 */
	public static CloudWatchConnector getDefaultConnector(ClientConfiguration clientConfig)
			throws LogException {
			
			if (clientConfig == null)
				throw new LogException("clientConfig must be supplied");

			return new DefaultConnector(clientConfig);
		}

}
