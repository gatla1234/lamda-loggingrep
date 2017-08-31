package com.dtcc.ecd.awslogutils.log4j;


import org.apache.log4j.helpers.PatternParser;
import org.apache.log4j.EnhancedPatternLayout;

/**
 * Custom Layout for DTCC format characters, like the
 * custom MCD or UTC Date ones.
 * 
 * @author mhanegra
 *
 */
public class DTCCPatternLayout extends EnhancedPatternLayout {
	
	public PatternParser createPatternParser(String pattern) 
	   {
	      PatternParser result;
	      if ( pattern == null )
	         result = new DTCCPatternParser( DEFAULT_CONVERSION_PATTERN );
	      else
	         result = new DTCCPatternParser ( pattern );

	      return result;
	  }

}
