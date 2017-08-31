package com.dtcc.ecd.awslogutils.cloudwatch;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;


/**
 * Various utility and convenience methods used by the logger.
 * @author mhanegra
 *
 */
public class LoggingUtils {
	
	/**
	 * Generates the current date using the UTC (GMT) time zone
	 * This is typically used to prefix log streams.
	 * 
	 * @return String representing a UTC date formatted as "yyyy-mm-dd"
	 */
	static public String getUTCDate()
	{		
		SimpleDateFormat dateFormatGmt = new SimpleDateFormat("yyyy-MM-dd");
		dateFormatGmt.setTimeZone(TimeZone.getTimeZone("GMT"));
		
		return dateFormatGmt.format(new Date());
	}
	
	/**
	 * A string representing a timestamp using the UTM (GMT) Timezone
	 * This is typically used to generate a timestamp for a log event
	 * 
	 * @return String representing a UTC date formatted as "yyyy/MM/dd HH:mm:ss"
	 */
	static public String getUTCTimeStamp()
	{		
		SimpleDateFormat dateFormatGmt = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		dateFormatGmt.setTimeZone(TimeZone.getTimeZone("GMT"));
		return dateFormatGmt.format(new Date()) + " GMT";
	}
	
	/**
	 * Purges the Log4j MDC while preserving the logger supplied values
	 */
	static public void purgeLog4jMDC()
	{
		//TODO: Implement this
	}
	
	/**
	 * Generates a UUID String.
	 * 
	 * @return String representing a UUID
	 */
	static public String getUUID()
	{
		UUID uuid = UUID.randomUUID();		
		return uuid.toString().replace("-", "");
	}
}
