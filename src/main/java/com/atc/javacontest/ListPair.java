package com.atc.javacontest;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sergey on 08.03.16.
 */
public class ListPair<T> extends CollectionPair<T> {
    List<T> first;
    List<T> second;

    public ListPair(List<T> first, List<T> second) {
        this.first = first;
        this.second = second;
    }

    public ListPair(CollectionPair<T> pair) {
        this.first = new ArrayList<>(pair.getFirst());
        this.second = new ArrayList<>(pair.getSecond());
    }

    @Override
    public List<T> getFirst() {
        return first;
    }

    @Override
    public List<T> getSecond() {
        return second;
    }
}
