package com.atc.javacontest;

import org.apache.commons.math3.util.Pair;

import java.io.IOException;
import java.nio.file.Files;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sergey on 08.03.16.
 */
public class ListPair extends CollectionPair {
    List<Double> first;
    List<Double> second;

    public ListPair(List<Double> first, List<Double> second) {
        this.first = first;
        this.second = second;
    }

    public ListPair(CollectionPair pair) {
        this.first = new ArrayList<>(pair.getFirst());
        this.second = new ArrayList<>(pair.getSecond());
    }

    public ListPair() {
        this.first = new ArrayList<>();
        this.second = new ArrayList<>();
    }

    @Override
    public List<Double> getFirst() {
        return first;
    }

    @Override
    public List<Double> getSecond() {
        return second;
    }

    public ListPair subList(int start, int finish) {
        return new ListPair(first.subList(start, finish), second.subList(start, finish));
    }

    public double getCorrelation(int start, int finish) {
        return Correlation.getMaxCorrelation(
                first.subList(start, finish),
                second.subList(start, finish)
        );
    }

    public double getAbsCorrelation(int start, int finish) {
        return Correlation.getMaxAbsCorrelation(
                first.subList(start, finish),
                second.subList(start, finish)
        );
    }

    @Override
    public ListPair toListPair() {
        return this;
    }

    @Override
    public DequePair toDequePair() {
        return new DequePair(this);
    }

    public Pair<Double, Double> get(int index) {
        return Pair.create(first.get(index), second.get(index));
    }

    private List<CorrelationBlock> getBlocks(int piece, int correlationDiff, int correlationBound) {
        List<CorrelationBlock> result = new ArrayList<>();
        DequePair ascending = new DequePair(subList(0, piece));
        double currentCorrelation = ascending.getAbsCorrelation();
        int startBlock = 0;
        for (int i = piece; i < size(); i++) {
            ascending.pollFirst();
            ascending.addLast(get(i));
            double correlation = ascending.getAbsCorrelation();
            if (Math.abs(correlation - currentCorrelation) > correlationDiff) {
                double blockCorrelation = getAbsCorrelation(startBlock, i);
                if (blockCorrelation >= correlationBound) {
                    CorrelationBlock block = new CorrelationBlock(blockCorrelation, startBlock, i);
                    result.add(block);
                    block.printlnStats();
                }
                startBlock = i;
                ascending.clear();
                i = i + piece < size() ? i + piece : size();
                ascending.addAll(subList(i - 6, i - 1));
            } else {
                currentCorrelation = correlation;
            }
        }
        result.add(new CorrelationBlock(ascending.getAbsCorrelation(), startBlock, size()));
        return result;
    }

    

    public static CollectionPair fromResource(URL resource) {
        try {
            List<String> lines = Files.readAllLines(Paths.get(resource.toURI()), StandardCharsets.UTF_8);
            List<Double> first = new ArrayList<>();
            List<Double> second = new ArrayList<>();
            for (String line : lines) {
                String[] splatted = line.split(";");
                first.add(Double.parseDouble(splatted[1]));
                second.add(Double.parseDouble(splatted[2]));
            }
            return new ListPair(first, second);
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
        }
        return new ListPair();
    }
}
