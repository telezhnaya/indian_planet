package com.atc.javacontest;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.math3.stat.correlation.KendallsCorrelation;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;

import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Sergey on 08.03.16.
 */
class Correlation {

    static KendallsCorrelation kendallsCorrelation = new KendallsCorrelation();
    static SpearmansCorrelation spearmansCorrelation = new SpearmansCorrelation();
    static PearsonsCorrelation pearsonsCorrelation = new PearsonsCorrelation();

    static double getCorrelation(Collection<Double> first, Collection<Double> second) {
        int size = first.size();
        double[] firstArray = new double[size];
        double[] secondArray = new double[size];
        Iterator<Double> first_iter = first.iterator();
        Iterator<Double> second_iter = second.iterator();
        for (int i = 0; i < size; i++) {
            firstArray[i] = first_iter.next();
            secondArray[i] = second_iter.next();
        }
        return getCorrelation(firstArray, secondArray);
    }

    static double getCorrelation(double[] first, double[] second) {
        return Math.max(kendallsCorrelation.correlation(first, second),
                Math.max(spearmansCorrelation.correlation(first, second),
                        pearsonsCorrelation.correlation(first, second)
                )
        );
    }
}
