package app.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {

    public static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
    public static final SimpleDateFormat BRIEF_STAMP_FORMAT = new SimpleDateFormat("dd.MM.yy HH:mm");

    public static String dateToISO(Date date) {
        return SIMPLE_DATE_FORMAT.format(date);
    }

    public static String getCurrentDateString() {
        return SIMPLE_DATE_FORMAT.format(new Date());
    }

    public static Date integerToDate(long timeInteger) {
        return new Date(timeInteger);
    }

    public static String dateToBriefStamp(Date date) {
        return BRIEF_STAMP_FORMAT.format(date);
    }
}
