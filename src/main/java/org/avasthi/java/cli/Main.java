package org.avasthi.java.cli;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Main {
    public static void main(String[] args) throws ParseException {

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MONTH, 9);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        calendar.set(Calendar.YEAR, 2025);
        System.out.println(calendar.getTime());
        calendar.add(Calendar.MONTH, -3);
        while(calendar.get(Calendar.DAY_OF_MONTH) != 1) {

            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        System.out.println(calendar.getTime());
    }
}