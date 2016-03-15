package com.atc.javacontest;

import edu.mines.jtk.dsp.LocalCorrelationFilter;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created by Sergey on 14.03.16.
 */
public class CorrelationList {
    private ArrayList<Double> first;
    private ArrayList<Double> second;

    public CorrelationList(URL resource, String separator) {
        first = new ArrayList<>(1024);
        second = new ArrayList<>(1024);
        try {
            List<String> lines = Files.readAllLines(Paths.get(resource.toURI()), StandardCharsets.UTF_8);
            for (String line : lines) {
                String[] split_line = line.split(separator);
                first.add(Double.parseDouble(split_line[1].replace(',', '.')));
                second.add(Double.parseDouble(split_line[2].replace(',', '.')));
            }
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
        }
    }

    public CorrelationList(URL resource) {
        this(resource, ";");
    }

    static final int BLOCK_SIZE = 8;

    public List<CorrelationResultIndex> getCorrelationResultIndexes() {
        ArrayList<CorrelationResultIndex> result = new ArrayList<>();
        CorrelationResultIndex last_index = null;
        ArrayDeque<Double> first_block = new ArrayDeque<>(first.subList(0, BLOCK_SIZE));
        ArrayDeque<Double> second_block = new ArrayDeque<>(second.subList(0, BLOCK_SIZE));
        Double block_correlation = Correlation.getCorrelation(first_block, second_block);
        Integer correlation_start = 0;
        for (int i = BLOCK_SIZE; i < first.size(); i++) {
            first_block.pollFirst(); second_block.pollFirst();
            first_block.addLast(first.get(i)); second_block.addLast(second.get(i));
            double new_block_correlation = Correlation.getCorrelation(first_block, second_block);
            if (isBadCorrelationDifference(block_correlation, new_block_correlation)) {
                CorrelationResultIndex index = buildCorrelationResultIndex(correlation_start, i);
                last_index = unionOrPush(last_index, index, result);
                correlation_start = i;
                i += BLOCK_SIZE;
                if (i > first.size()) {
                    i = first.size();
                }
                first_block.clear(); second_block.clear();
                first_block.addAll(first.subList(correlation_start, i));
                second_block.addAll(second.subList(correlation_start, i));
                block_correlation = Correlation.getCorrelation(first_block, second_block);
            } else {
                block_correlation = new_block_correlation;
            }
        }
        if (last_index != null) result.add(last_index);
        return result;
    }

    public CorrelationResultIndex getCorrelationResultIndex() {
        double correlation = Correlation.getCorrelation(first, second);
        return getLagIndex(buildCorrelationResultIndex(correlation, 0, first.size()));
    }

    private CorrelationResultIndex unionOrPush(CorrelationResultIndex first, CorrelationResultIndex second, List<CorrelationResultIndex> pushing) {
        if (first == null) {
            return second;
        } else if (second == null) {
            return first;
        } else {
            if (getStart(second) <= getEnd(first)) {
                return buildCorrelationResultIndex(getStart(first), getEnd(second));
            } else {
                pushing.add(getLagIndex(first));
                return second;
            }
        }
    }

    private boolean isBadCorrelationDifference(double first_correlation, double second_correlation) {
        return Math.abs(first_correlation) - Math.abs(second_correlation) >= 0.3;
    }

    private boolean isGoodCorrelation(double correlation) {
        return Math.abs(correlation) >= 0.3;
    }

    private CorrelationResultIndex buildCorrelationResultIndex(int start, int end) {
        double correlation = Correlation.getCorrelation(first.subList(start, end), second.subList(start, end));
        if (isGoodCorrelation(correlation)) {
            return buildCorrelationResultIndex(correlation, start, end);
        } else {
            return null;
        }
    }

    private CorrelationResultIndex buildCorrelationResultIndex(double correlation, int start, int end) {
        CorrelationResultIndex index = new CorrelationResultIndex();
        index.correlation = correlation;
        index.startIndex = String.valueOf(start);
        index.endIndex = String.valueOf(end);
        return index;
    }

    private CorrelationResultIndex getLagIndex(CorrelationResultIndex index) {
        int start = getStart(index);
        int end = getEnd(index);
        LocalCorrelationFilter filter = new LocalCorrelationFilter(
                LocalCorrelationFilter.Type.SIMPLE,
                LocalCorrelationFilter.Window.GAUSSIAN,
                2
        );
        filter.setInputs(
                Utility.toFloatArray(first.subList(start, end)),
                Utility.toFloatArray(second.subList(start, end))
        );
        double bestCorrelation = index.correlation;
        for (int i = 0; i < end - start; i += ((end - start)/30) + 1) {
            float[] fl = new float[end - start];
            filter.correlate(i, fl);
            filter.normalize(i, fl);
            double corr = Correlation.getCorrelation(first.subList(start, end), Utility.toDoubleList(fl));
            if (corr > bestCorrelation) {
                bestCorrelation = corr;
                index.correlationLagIndex = i;
            }
        }
        return index;
    }

    private List<CorrelationResultIndex> getLagIndexes(List<CorrelationResultIndex> indexes) {
        for (CorrelationResultIndex index : indexes) {
            index = getLagIndex(index);
        }
        return indexes;
    }

    private int getStart(CorrelationResultIndex index) {
        return Integer.parseInt(index.startIndex);
    }

    private int getEnd(CorrelationResultIndex index) {
        return Integer.parseInt(index.endIndex);
    }
}