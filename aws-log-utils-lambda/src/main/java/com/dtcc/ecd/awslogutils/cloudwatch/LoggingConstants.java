package com.dtcc.ecd.awslogutils.cloudwatch;
/**
 * Various internal constants used by the logger. Most of them define preset MDC values
 * @author mhanegra
 *
 */
public class LoggingConstants {

	private static final String MDC_PREFIX = "__";
	private static final String MDC_SUFFIX = "__";
	
	public static final String LOGGER_VERSION = String.format("%sLOGGER_VERSION%s=1.0.2", MDC_PREFIX, MDC_SUFFIX) ;
	
	public static final String SYSID = String.format("%sSYSID%s", MDC_PREFIX, MDC_SUFFIX);
	public static final String APPLICATION_NAME = String.format("%sAPPLICATION_NAME%s", MDC_PREFIX, MDC_SUFFIX);
	public static final String AWS_LAMBDA_FUNCTION_NAME = String.format("%sAWS_LAMBDA_FUNCTION_NAME%s", MDC_PREFIX, MDC_SUFFIX);
	public static final String AWS_LAMBDA_FUNCTION_VERSION = String.format("%sAWS_LAMBDA_FUNCTION_VERSION%s", MDC_PREFIX, MDC_SUFFIX);
	public static final String AWS_REQUEST_ID = String.format("%sAWS_REQUEST_ID%s", MDC_PREFIX, MDC_SUFFIX);
	
}
