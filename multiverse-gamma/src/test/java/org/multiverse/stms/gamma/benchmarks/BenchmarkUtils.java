package org.multiverse.stms.gamma.benchmarks;

import java.text.NumberFormat;
import java.util.*;

public class BenchmarkUtils {

    public static int[] generateProcessorRange() {
        return generateProcessorRange(Runtime.getRuntime().availableProcessors());
    }

    public static int[] generateProcessorRange(int maxProcessors) {
        List<Integer> list = new LinkedList<Integer>();

        for (int k = 1; k <= 16; k++) {
            if (k <= maxProcessors) {
                list.add(k);
            }
        }

        list.add(maxProcessors);

        int k = 16;
        while (k < maxProcessors) {
            k = (int) (k * 1.07);
            if (k <= maxProcessors) {
                list.add(k);
            }
        }

        //remove all bigger than maxProcessors
        for (Integer value : list) {
            if (value > maxProcessors) {
                list.remove(value);
            }
        }

        //remove all duplicates
        list = new LinkedList(new HashSet(list));
        //sort them
        Collections.sort(list);

        Integer[] integerArray = new Integer[list.size()];
        integerArray = list.toArray(integerArray);

        int[] result = new int[integerArray.length];
        for (int l = 0; l < integerArray.length; l++) {
            result[l] = integerArray[l];
        }

        return result;
    }

    public static void toGnuplot(Result[] result) {
        println("---------------------------------------------");
        println("------------------ GNUPLOT ------------------");
        println("---------------------------------------------");
        println("set terminal png");
        println("set output \"result.png\"");
        println("set xlabel \"threads\"");
        println("set origin 0,0");
        println("set ylabel \"transactions/second\"");
        println("plot '-' using 1:2 with lines");
        for (Result aResult : result) {
            println("%s %s", aResult.processorCount, aResult.performance);
        }
        println("e");
        println("");
    }

    public static void println(String s, Object... args) {
        System.out.printf(s + "\n", args);
    }

    public static String transactionsPerSecondAsString(long count, long timeMs) {
        double performance = (1000 * count) / timeMs;
        return format(performance);
    }

    public static String transactionsPerSecondPerThreadAsString(long transactionsPerThread, long totalTimeMs, int threads) {
        return format(transactionsPerSecondPerThread(transactionsPerThread, totalTimeMs, threads));
    }

    public static double transactionsPerSecondPerThread(long transactionsPerThread, long totalTimeMs, int threads) {
        long totalTransactions = transactionsPerThread * threads;

        return (1000d * totalTransactions) / totalTimeMs;
    }

    public static double transactionsPerSecond(long transactionsPerThread, long totalTimeMs, int threads) {
        return threads * transactionsPerSecondPerThread(transactionsPerThread, totalTimeMs, threads);
    }

    public static String transactionsPerSecondAsString(long transactionsPerThread, long totalTimeMs, int threads) {
        return format(transactionsPerSecond(transactionsPerThread, totalTimeMs, threads));
    }

    public static String format(double value) {
        return NumberFormat.getInstance(Locale.US).format(value);
    }
}