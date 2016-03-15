package com.atc.javacontest;

import org.apache.commons.math3.util.Pair;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Created by Sergey on 08.03.16.
 */

/**
 * Класс для представления двух деков
 *
 */
public class DequePair extends CollectionPair {

    public Deque<Double> first;
    public Deque<Double> second;

    public DequePair(Deque<Double> first, Deque<Double> second) {
        this.first = first;
        this.second = second;
    }

    public DequePair(CollectionPair pair) {
        this.first = new ArrayDeque<>(pair.getFirst());
        this.second = new ArrayDeque<>(pair.getSecond());
    }

    @Override
    public Deque<Double> getFirst() {
        return first;
    }

    @Override
    public Deque<Double> getSecond() {
        return second;
    }

    @Override
    public ListPair toListPair() {
        return new ListPair(this);
    }

    @Override
    public DequePair toDequePair() {
        return this;
    }

    public void addLast(Double f, Double s) {
        first.addLast(f);
        second.addLast(s);
    }

    public void addLast(Pair<Double, Double> pair) {
        addLast(pair.getFirst(), pair.getSecond());
    }

    public void addFirst(Double f, Double s) {
        first.addFirst(f);
        second.addFirst(s);
    }

    public void addFirst(Pair<Double, Double> pair) {
        addFirst(pair.getFirst(), pair.getSecond());
    }

    public Pair<Double, Double> pollFirst() {
        return Pair.create(first.pollFirst(), second.pollFirst());
    }

    public Pair<Double, Double> pollLast() {
        return Pair.create(first.pollLast(), second.pollLast());
    }


}
