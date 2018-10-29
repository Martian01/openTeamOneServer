package com.opencommunity.openTeamOneServer.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class TimeUtil {

	protected static DateTimeFormatter dtfFull = null;
	protected static DateFormat dfIsoUtc = null;
	protected static DateFormat dfDateOnly = null;

	public static String toFullDateString(long millis) {
			if (dtfFull == null)
				dtfFull = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.FULL).withLocale(Locale.getDefault()).withZone(ZoneId.systemDefault());
			return dtfFull.format(Instant.ofEpochMilli(millis));
	}

	public static String toIsoDateString(long millis) {
			return ZonedDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.systemDefault()).toString();
	}

	public static long parseIsoDateTimeToMillis(String dateTimeString) { // TODO: Java 8
		if (dateTimeString != null)
			try {
				if (dfIsoUtc == null) {
					dfIsoUtc = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
					dfIsoUtc.setTimeZone(TimeZone.getTimeZone("UTC"));
				}
				return dfIsoUtc.parse(dateTimeString).getTime();
			} catch (Exception ignored) { }
		return 0L;
	}

	public static String getDate(long millis) { // TODO: Java 8
		if (dfDateOnly == null)
			dfDateOnly = new SimpleDateFormat("yyyyMMdd");
		return dfDateOnly.format(new Date(millis));
	}

}


