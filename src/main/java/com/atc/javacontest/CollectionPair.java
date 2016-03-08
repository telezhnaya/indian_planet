package com.atc.javacontest;

import java.util.Collection;

/**
 * Created by Sergey on 08.03.16.
 */

public abstract class CollectionPair {

    public abstract Collection<Double> getFirst();

    public abstract Collection<Double> getSecond();

    public int size() {
        return getFirst().size();
    }

    public double getCorrelation() {
        return Correlation.getMaxCorrelation(getFirst(), getSecond());
    }

    public double getAbsCorrelation() {
        return Correlation.getMaxAbsCorrelation(getFirst(), getSecond());
    }

    public abstract ListPair toListPair();

    public abstract DequePair toDequePair();

}
