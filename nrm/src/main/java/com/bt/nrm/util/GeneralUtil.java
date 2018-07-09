package com.bt.nrm.util;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static com.bt.rsqe.utils.AssertObject.isNotNull;

public class GeneralUtil {

    public static java.sql.Timestamp getCurrentTimeStamp() {
        Date utilDate = new Date();
        Timestamp sqlDate = new Timestamp(utilDate.getTime());
        return  sqlDate;
    }

    public static String formatDate(Date date){
        if(isNotNull(date)) {
            return new SimpleDateFormat("dd-MMM-yyyy").format(date);
        }
        return null;
    }

    public static boolean between(Date date, Date dateStart, Date dateEnd) {
        if (date != null && dateStart != null && dateEnd != null) {
            if(date.after(dateStart) && date.before(dateEnd)) {
                return true;
            }else{
                return false;
            }
        }
        return false;
    }

    public static Date addDaysToDate(Date date, Long Days){
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.DATE, Days.intValue());
        return c.getTime();
    }
}
