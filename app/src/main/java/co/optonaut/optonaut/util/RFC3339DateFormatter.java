package co.optonaut.optonaut.util;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.chrono.ISOChronology;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Locale;

/**
 * @author Nilan Marktanner
 * @date 2015-11-30
 */
public class RFC3339DateFormatter {
    private static final DateTimeFormatter rfc3339Formatter1 = DateTimeFormat
                                                        .forPattern("yyyy'-'MM'-'dd'T'HH':'mm':'ss.SSSZ")
//                                                        .withZone(DateTimeZone.getDefault())
                                                        .withZone(DateTimeZone.UTC)
                                                        .withLocale(Locale.US)
                                                        .withChronology(ISOChronology.getInstance());
    private static final DateTimeFormatter rfc3339Formatter2 = DateTimeFormat
                                                        .forPattern("yyyy'-'MM'-'dd'T'HH':'mm':'ssZ")
//                                                        .withZone(DateTimeZone.getDefault())
                                                        .withZone(DateTimeZone.UTC)
                                                        .withLocale(Locale.US)
                                                        .withChronology(ISOChronology.getInstance());


    public static DateTime fromRFC3339String(String string) {
        DateTime dateTime = null;
        try {
             dateTime = rfc3339Formatter1.parseDateTime(string);
        } catch (IllegalArgumentException ex1) {
            try {
                dateTime = rfc3339Formatter2.parseDateTime(string);
            } catch (IllegalArgumentException ex2) {
                ex1.printStackTrace();
                ex2.printStackTrace();
            }
        }

        return dateTime;
    }

    public static String toRFC3339String(DateTime dateTime) {
        return dateTime.toString(rfc3339Formatter1);
    }
}
