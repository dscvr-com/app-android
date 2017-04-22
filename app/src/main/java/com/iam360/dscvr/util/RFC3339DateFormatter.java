package com.iam360.dscvr.util;

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
                                                        .forPattern("yyyy'-'MM'-'dd'T'HH':'mm':'ss.SSSSSSZ")
//                                                        .withZone(DateTimeZone.getDefault())
                                                        .withZone(DateTimeZone.UTC)
                                                        .withLocale(Locale.US)
                                                        .withChronology(ISOChronology.getInstance());


    public static DateTime fromRFC3339String(String string) {
        DateTime dateTime = null;
        DateTimeFormatter formatter = getFormatter(string);
        try {
             dateTime = formatter.parseDateTime(string);
        } catch (Exception ex1) {
//            try {
//                dateTime = rfc3339Formatter2.parseDateTime(string);
//                Log.d("myTag"," timeAgo: try2 dateTime: "+dateTime.toString());
//            } catch (IllegalArgumentException ex2) {
//                Log.d("myTag"," timeAgo: catch2 "+ex2.getMessage());
//                ex1.printStackTrace();
//                ex2.printStackTrace();
//            }
        }

        return dateTime;
    }

    private static DateTimeFormatter getFormatter(String dateString) {
        DateTimeFormatter formatter = rfc3339Formatter1;

        if(dateString == null) return formatter;

        String s = "";
        dateString.lastIndexOf(".");
        for (int i=0;i<(dateString.length()-(dateString.lastIndexOf(".")+2));i++) {
            s+="S";
        }
//        Log.d("myTag"," timeAgo: dateString: "+dateString+" lastIndex: "+dateString.lastIndexOf(".")+
//                " length: "+dateString.length()+" numberOfDecimal: "+(dateString.length()-(dateString.lastIndexOf(".")+2))+
//                " s: "+s);
        formatter = DateTimeFormat
                .forPattern("yyyy'-'MM'-'dd'T'HH':'mm':'ss."+s+"Z")
//              .withZone(DateTimeZone.getDefault())
                .withZone(DateTimeZone.UTC)
                .withLocale(Locale.US)
                .withChronology(ISOChronology.getInstance());
        
        return formatter;
    }

    public static String toRFC3339String(DateTime dateTime) {
        return dateTime.toString(rfc3339Formatter1);
    }
}
