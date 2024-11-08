package org.avasthi.java.cli;
import java.io.*;
import java.util.*;


public class Sfsg {
    static Map<Integer, Integer> gMap = new HashMap<>();
    static Map<Long, Long> sfnMap = new HashMap<>();
    static int[] gArray = { 0, 1,2,5,15,25,3,13,23,6,16,26,44,144,256,36,136,236,67,167,267,349,1349,2349,49,149,249,9,19,29,129,229,1229,39,139,239,1239,13339,23599,4479,14479,344479};
    public static void main(String[] args) {

        for (long i = 1; i <= 1000000000; ++i) {
            long sfv = sf(i);
            System.out.println(String.format("sf(%d) = %d", i, sfv));
            if (sfnMap.get(sfv) == null) {
                sfnMap.put(sfv, i);
            }
        }
        for (Map.Entry<Long, Long> e : sfnMap.entrySet()) {
            System.out.println(String.format("%d = %d", e.getKey(), e.getValue()));
        }
        System.out.println(String.format("sf(%d) = %d", 4500, sf(4500)));
        Scanner kb = new Scanner(System.in);
        int q = kb.nextInt();
        for(int i = 0; i < q; ++i) {
            int n = kb.nextInt();
            int m = kb.nextInt();
            long sum = 0;
            for (int j = 1; j <= n; ++j) {
                sum += sg(j);
            }
            System.out.println(sum % m);
        }
    }

    static long f(long n) {
        int sum = 0;
        while (n > 0) {
            sum += fact((int)(n % 10));
            n = n / 10;
        }
        return sum;
    }
    static long sf(long n) {
        return sumOfDigits(f(n));
    }
    static long sumOfDigits(long n) {
        long sum = 0;
        while(n > 0) {
            sum += (n % 10);
            n /= 10;
        }
        return sum;
    }
    static int g(int i) {
        return gArray[i];
    }
    static long sg(int i) {
        return sumOfDigits(g(i));
    }
    static long fact(int n) {
        switch(n) {
            case 0:
            case 1:
                return 1;
            case 2:
                return 2;
            case 3:
                return 6;
            case 4:
                return 24;
            case 5:
                return 120;
            case 6:
                return 720;
            case 7:
                return 5040;
            case 8:
                return 40320;
            case 9:
                return 362880;
        }
        if ( n <= 1) {
            return 1;
        }
        else {
            return n * fact(n - 1);
        }
    }
}

