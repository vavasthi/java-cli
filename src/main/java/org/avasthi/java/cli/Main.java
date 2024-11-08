package org.avasthi.java.cli;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Main {
    public static void main(String[] args) throws ParseException {

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR, 16);
        calendar.set(Calendar.MINUTE, 10);
        calendar.set(Calendar.AM_PM, Calendar.PM);
        System.out.println(calendar.getTime().toString());
        SimpleDateFormat dateFormat = new SimpleDateFormat("K:mm a");
        String d1 = "04:00 PM";
        System.out.println(dateFormat.parse(d1).toString());
        System.out.println(dateFormat.format(new Date()));
    }
}