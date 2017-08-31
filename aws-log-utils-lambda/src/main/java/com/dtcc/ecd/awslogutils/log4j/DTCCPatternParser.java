package com.dtcc.ecd.awslogutils.log4j;

import java.util.Enumeration;

import org.apache.log4j.MDC;
import org.apache.log4j.helpers.FormattingInfo;
import org.apache.log4j.helpers.PatternConverter;
import org.apache.log4j.helpers.PatternParser;
import org.apache.log4j.spi.LoggingEvent;

import com.dtcc.ecd.awslogutils.cloudwatch.LoggingUtils;

public class DTCCPatternParser extends PatternParser {
	
   static final char CUSTOM_MDC = 'Y';
   static final char UTC_DATE 	= 'U';

   public DTCCPatternParser(String pattern) {
		super(pattern);
   }
	
   public void finalizeConverter(char formatChar) 
   {
      PatternConverter pc = null;
      switch( formatChar )
      {
         case CUSTOM_MDC:
            pc = new CustomMDCPatternConverter( formattingInfo );
            currentLiteral.setLength(0);
            addConverter( pc );
            break;
         case UTC_DATE:
             pc = new UTCDateConverter( formattingInfo );
             currentLiteral.setLength(0);
             addConverter( pc );
             break;
         default:
            super.finalizeConverter( formatChar );
      }
   }
   
   /**
     * A Custom MDC converter y
     * 
	 * @author mhanegra
	 *
	 */
   private static class CustomMDCPatternConverter extends PatternConverter 
   {
	  CustomMDCPatternConverter(FormattingInfo formattingInfo) 
      {
         super(formattingInfo);     
      }

      public String convert( LoggingEvent event )
      {
    	  String MDCString = "";
    	  
    	  Enumeration<?> mdcEnum = MDC.getContext().keys();
    	  
    	  while(mdcEnum.hasMoreElements())
    	  {
    		  String key = (String) mdcEnum.nextElement();    		  
    		  
    		  if (!key.startsWith("__LOG4J_MDC_"))
    		  {    		  
    			  String value = MDC.get(key).toString().trim();
	    		  //remove any trailing single quotes
	    		  value = value.replace("'", " ");
	    		  
	    		  if (value.contains(" "))
	    		  	value = "'" + value + "'";
	    		  
	    		  
	    		  MDCString += key + "=" + value;
	    		  if (mdcEnum.hasMoreElements())
	    			  MDCString += " ";
    		  }
    	  }    	 
    	      	  
    	  return MDCString.trim();
      }
   }
   
   
   private static class UTCDateConverter extends PatternConverter 
   {
	  UTCDateConverter(FormattingInfo formattingInfo) 
      {
         super(formattingInfo);     
      }

      public String convert( LoggingEvent event )
      {
    	  return LoggingUtils.getUTCTimeStamp();
      }
   }


}
