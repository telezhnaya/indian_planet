package com.atc.javacontest;

import java.util.ArrayList;
import java.util.List;

public class Utility {
    public static float[] toFloatArray(List<Double> list) {
        float[] array = new float[list.size()];
        for (int i = 0; i < list.size(); i++) {
            array[i] = list.get(i).floatValue();
        }
        return array;
    }

    public static List<Double> toDoubleList(float[] array) {
        List<Double> list = new ArrayList<>(array.length);
        for (float e : array) {
            list.add((double) e);
        }
        return list;
    }
}
