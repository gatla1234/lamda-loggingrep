package com.dtcc.ecd.awslogutils.cloudwatch;

import com.dtcc.ecd.awslogutils.exception.LogException;

/**
 * A general purpose validation class class
 * @author mhanegra
 *
 */
public class Validator {
	
	/**
	 * Validate a SYSID String and ensures it's in a proper format
	 * @param SYSID
	 * @throws LogException
	 */
	public static void validateSYSID(String SYSID)
			throws LogException
	{
		if (SYSID == null || SYSID.equals(""))
			throw new LogException("SYSID is null or empty");
		
		if (SYSID.length() != 3)
			throw new LogException("SYSID is loo long. Expecting 3 character string");
		
		if (!SYSID.matches("[a-zA-Z0-9]+"))
			throw new LogException("SYSID failed validation. Expecting 'a-zA-Z0-9'");
	}
	
	
	
	/**
	 * Validates an application name.
	 * @param appName
	 * @throws LogException
	 */
	public static void validateApplicationName(String appName)
			throws LogException
	{
		if (appName == null || appName.equals(""))
			throw new LogException("ApplicationName is null or empty");
		
		if (appName.length() > 100)
			throw new LogException("ApplicationName is loo long, it exceeds 100 characters");
		
		if (!appName.matches("[a-zA-Z0-9_-]+"))
			throw new LogException("ApplicationName failed validation. 'a-zA-Z0-9_-'");
	}
}
