/**
 * Implements the Cold Fusion Function datecompare
 */
package railo.runtime.functions.dateTime;

import java.util.Calendar;
import java.util.TimeZone;

import railo.runtime.PageContext;
import railo.runtime.engine.ThreadLocalPageContext;
import railo.runtime.exp.ExpressionException;
import railo.runtime.exp.FunctionException;
import railo.runtime.ext.function.Function;
import railo.runtime.type.dt.DateTime;

public final class DateCompare implements Function {

	public static double call(PageContext pc , DateTime left, DateTime right) throws ExpressionException {
		return call(pc , left, right,"s");
	}
	
	
	public static double call(PageContext pc , DateTime left, DateTime right, String datepart) throws ExpressionException {
		datepart=datepart.toLowerCase().trim();
		TimeZone tz=ThreadLocalPageContext.getTimeZone(pc);
		Calendar cLeft=Calendar.getInstance();
		cLeft.setTimeZone(tz);
		cLeft.setTime(left);
		
		Calendar cRight=Calendar.getInstance();
		cRight.setTimeZone(tz);
		cRight.setTime(right);
		
		// TODO WEEEK
		
		int type=0;
		if(datepart.equals("s")) 		type=Calendar.SECOND;		
		else if(datepart.equals("n"))	type=Calendar.MINUTE;		
		else if(datepart.equals("h"))	type=Calendar.HOUR;		
		else if(datepart.equals("d"))	type=Calendar.DATE;	
		else if(datepart.equals("m"))	type=Calendar.MONTH;
		else if(datepart.equals("y"))	type=Calendar.DATE;
		else if(datepart.equals("yyyy"))type=Calendar.YEAR;
		else {
			throw new FunctionException(pc,"dateCompare",3,"datePart","invalid value ["+datepart+"], valid values has to be [s,n,h,d,m,y,yyyy]");
		}
		
		// Year
		int value = cLeft.get(Calendar.YEAR)-cRight.get(Calendar.YEAR);
		if(value!=0) return value>0?1:-1;
		if(Calendar.YEAR==type) return 0;
		if(Calendar.YEAR==type) return 0;
		
		// Month
		value = cLeft.get(Calendar.MONTH)-cRight.get(Calendar.MONTH);
		if(value!=0) return value>0?1:-1;
		if(Calendar.MONTH==type) return 0;
		
		// Day
		value = cLeft.get(Calendar.DATE)-cRight.get(Calendar.DATE);
		if(value!=0) return value>0?1:-1;
		if(Calendar.DATE==type) return 0;
		
		// Hour
		//print.out(cLeft.get(Calendar.HOUR_OF_DAY)+"-"+cRight.get(Calendar.HOUR_OF_DAY));
		value = cLeft.get(Calendar.HOUR_OF_DAY)-cRight.get(Calendar.HOUR_OF_DAY);
		if(value!=0) return value>0?1:-1;
		if(Calendar.HOUR==type) return 0;
		
		// Minute
		value = cLeft.get(Calendar.MINUTE)-cRight.get(Calendar.MINUTE);
		if(value!=0) return value>0?1:-1;
		if(Calendar.MINUTE==type) return 0;
		
		// Second
		value = cLeft.get(Calendar.SECOND)-cRight.get(Calendar.SECOND);
		if(value!=0) return value>0?1:-1;
		return 0;
			
	}

}