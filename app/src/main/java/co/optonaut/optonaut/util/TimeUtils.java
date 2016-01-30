package co.optonaut.optonaut.util;

import android.util.Log;

import org.joda.time.DateTime;
import org.joda.time.Minutes;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.joda.time.format.PeriodFormat;
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
                    .appendSuffix(" year", " years");
        } else if (period.getMonths() >= 1) {
            periodFormatterBuilder
                    .appendMonths()
                    .appendSuffix(" month", " months");
        } else if (period.getWeeks() >= 1) {
            periodFormatterBuilder
                    .appendWeeks()
                    .appendSuffix(" week", " weeks");
        } else if (period.getDays() >= 1) {
            periodFormatterBuilder
                    .appendDays()
                    .appendSuffix(" day", " days");
        } else if (period.getHours() >= 1) {
            periodFormatterBuilder
                    .appendHours()
                    .appendSuffix(" hour", " hours");
        } else if (period.getMinutes() >= 1) {
            periodFormatterBuilder
                    .appendMinutes()
                    .appendSuffix(" minute", " minutes");
        } else {
            throw new RuntimeException("Unexpected creation time!");
        }

        periodFormatterBuilder.appendLiteral(" ago");
        PeriodFormatter formatter = periodFormatterBuilder.toFormatter();

        return formatter.print(period);
    }
}
