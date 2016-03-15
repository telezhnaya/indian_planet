package com.atc.javacontest;

import java.util.Collection;

/**
 * Created by Sergey on 08.03.16.
 */

/**
 * Общий предок ListPair и DequePair.
 * В данной задаче на данный момент является бесполезным.
 * При дальнейшем расширении может быть полезным.
 */
public abstract class CollectionPair {

    public abstract Collection<Double> getFirst();

    public abstract Collection<Double> getSecond();

    public int size() {
        return getFirst().size();
    }

    public void clear() {
        getFirst().clear();
        getSecond().clear();
    }

    public void addAll(CollectionPair pair) {
        getFirst().addAll(pair.getFirst());
        getSecond().addAll(pair.getSecond());
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
