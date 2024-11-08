package org.avasthi.java.cli;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Result {
    public static List<Integer> gradingStudents(List<Integer> grades) {
        return grades.stream().map(v -> {

            if ( v < 40) {
                return v;
            }
            else {

                if (v % 5 < 2) {
                    return ((v / 5) * 5) + 5;
                }
                else {
                    return v;
                }
            }
        }).collect(Collectors.toList());
    }
    public static void countApplesAndOranges(int s, int t, int a, int b, List<Integer> apples, List<Integer> oranges) {
        if (s > t) {
            int temp = s;
            s = t;
            t = s;
        }
        final int spos = s;
        final int tpos = t;
        int appleCount = apples.stream().filter(new Predicate<Integer>() {
            @Override
            public boolean test(Integer v) {
                int ap = v + a;
                return (ap >= spos && ap <= tpos);
            }
        }).collect(Collectors.toList()).size();
        int orangeCount = oranges.stream().filter(new Predicate<Integer>() {
            @Override
            public boolean test(Integer v) {
                int bp = v + b;
                return (bp >= spos && bp <= tpos);
            }
        }).collect(Collectors.toList()).size();
        System.out.println(appleCount);
        System.out.println(orangeCount);
    }
}
