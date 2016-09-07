package com.iam360.dscvr.util;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

/**
 * @author Nilan Marktanner
 * @date 2016-01-26
 */
public class TimeUtils {
    public static String getTimeAgo(DateTime createdAt) {
        Period period = new Period(createdAt, DateTime.now());

        if (period.getMinutes() == 0
                && period.getHours() == 0
                && period.getDays() == 0
                && period.getWeeks() == 0
                && period.getMonths() == 0
                && period.getYears() == 0) {
            return "just now";
        }

        PeriodFormatterBuilder periodFormatterBuilder = new PeriodFormatterBuilder();
        if (period.getYears() >= 1) {
            periodFormatterBuilder
                    .appendYears()
//                    .appendSuffix(" year", " years");
                    .appendSuffix("y", "y");
        } else if (period.getMonths() >= 1) {
            periodFormatterBuilder
                    .appendMonths()
//                    .appendSuffix(" month", " months");
                    .appendSuffix("mo", "mo");
        } else if (period.getWeeks() >= 1) {
            periodFormatterBuilder
                    .appendWeeks()
//                    .appendSuffix(" week", " weeks");
                    .appendSuffix("w", "w");
        } else if (period.getDays() >= 1) {
            periodFormatterBuilder
                    .appendDays()
//                    .appendSuffix(" day", " days");
                    .appendSuffix("d", "d");
        } else if (period.getHours() >= 1) {
            periodFormatterBuilder
                    .appendHours()
//                    .appendSuffix(" hour", " hours");
                    .appendSuffix("h", "h");
        } else if (period.getMinutes() >= 1) {
            periodFormatterBuilder
                    .appendMinutes()
//                    .appendSuffix(" minute", " minutes");
                    .appendSuffix("m", "m");
        } else {
            throw new RuntimeException("Unexpected creation time!");
        }

//        periodFormatterBuilder.appendLiteral(" ago");
        PeriodFormatter formatter = periodFormatterBuilder.toFormatter();

        return formatter.print(period);
    }
}
