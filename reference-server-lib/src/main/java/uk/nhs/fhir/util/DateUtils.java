package uk.nhs.fhir.util;

import java.text.DateFormat;
import java.util.Date;

public class DateUtils {
	
	public static String printCurrentDateTime() {
		DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
		Date now = new Date();
		return df.format(now);
	}
}
