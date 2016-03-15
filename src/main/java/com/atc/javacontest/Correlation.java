package com.atc.javacontest;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.math3.stat.correlation.KendallsCorrelation;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;

import java.util.Collection;

/**
 * Created by Sergey on 08.03.16.
 */

/**
 * Перечесление, представляющее удобный доступ к различным способам корреляции.
 * Для вычисления корреляции в задаче используется максимальная корреляция из всех методов, так как в задании
 * сказано подсчитать максимальную корреляцию
 */
public enum Correlation {
    KENDALL {
        @Override
        public double getCorrelation(Collection<Double> first, Collection<Double> second) {
            return new KendallsCorrelation().correlation(toPrimitive(first), toPrimitive(second));
        }
    },
    SPEARMAN {
        @Override
        public double getCorrelation(Collection<Double> first, Collection<Double> second) {
            return new SpearmansCorrelation().correlation(toPrimitive(first), toPrimitive(second));
        }
    },
    PEARSON {
        @Override
        public double getCorrelation(Collection<Double> first, Collection<Double> second) {
            return new PearsonsCorrelation().correlation(toPrimitive(first), toPrimitive(second));
        }
    };

    public abstract double getCorrelation(Collection<Double> first, Collection<Double> second);

    private static double[] toPrimitive(Collection<Double> list) {
        return ArrayUtils.toPrimitive(list.toArray(new Double[list.size()]));
    }

    public static double getMaxCorrelation(Collection<Double> first, Collection<Double> second) {
        double a = KENDALL.getCorrelation(first, second);
        double b = SPEARMAN.getCorrelation(first, second);
        double c = PEARSON.getCorrelation(first, second);
        return Math.max(a, Math.max(b, c));
    }

    public static double getMaxAbsCorrelation(Collection<Double> first, Collection<Double> second) {
        double a = Math.abs(KENDALL.getCorrelation(first, second));
        double b = Math.abs(SPEARMAN.getCorrelation(first, second));
        double c = Math.abs(PEARSON.getCorrelation(first, second));
        return Math.max(a, Math.max(b, c));
    }
}
