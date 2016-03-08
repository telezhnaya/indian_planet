package com.atc.javacontest;

import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;

/**
 * Created by Sergey on 08.03.16.
 */

public abstract class CollectionPair<T> {

    public abstract Collection<T> getFirst();

    public abstract Collection<T> getSecond();

    public int size() {
        return getFirst().size();
    }
}
