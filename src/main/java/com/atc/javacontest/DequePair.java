package com.atc.javacontest;

import com.sun.xml.bind.annotation.OverrideAnnotationOf;
import org.apache.commons.math3.util.Pair;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Created by Sergey on 08.03.16.
 */
public class DequePair<T> extends CollectionPair<T> {

    public Deque<T> first;
    public Deque<T> second;

    public DequePair(Deque<T> first, Deque<T> second) {
        this.first = first;
        this.second = second;
    }

    public DequePair(CollectionPair<T> pair) {
        this.first = new ArrayDeque<>(pair.getFirst());
        this.second = new ArrayDeque<>(pair.getSecond());
    }

    @Override
    public Deque<T> getFirst() {
        return first;
    }

    @Override
    public Deque<T> getSecond() {
        return second;
    }

    public void addLast(T f, T s) {
        first.addLast(f);
        second.addLast(s);
    }

    public void addFirst(T f, T s) {
        first.addFirst(f);
        second.addFirst(s);
    }

    public Pair<T, T> pollFirst() {
        return Pair.create(first.pollFirst(), second.pollFirst());
    }

    public Pair<T, T> pollLast() {
        return Pair.create(first.pollLast(), second.pollLast());
    }


}
