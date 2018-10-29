package com.opencommunity.openTeamOneServer.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class TimeUtil { // TODO: Java 8

	protected static DateFormat dfIsoUtc;

	private static DateFormat getDfIsoUtc() {
		if (dfIsoUtc == null) {
			dfIsoUtc = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
			dfIsoUtc.setTimeZone(TimeZone.getTimeZone("UTC"));
		}
		return dfIsoUtc;
	}

	public static String toIsoDateString(long millis) {
		return getDfIsoUtc().format(new Date(millis));
	}

	public static long parseIsoDateTimeToMillis(String dateTimeString) {
		if (dateTimeString != null)
			try {
				return getDfIsoUtc().parse(dateTimeString).getTime();
			} catch (Exception ignored) { }
		return 0L;
	}

}


