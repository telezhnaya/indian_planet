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

    public double getCorrelation() {
        return Correlation.getMaxCorrelation(first, second);
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

    private List<CorrelationBlock> getBlocks(int piece, double correlationDiff, double correlationBound) {
        List<CorrelationBlock> result = new ArrayList<>();
        DequePair ascending = new DequePair(subList(0, piece));
        double currentCorrelation = ascending.getAbsCorrelation();
        int startBlock = 0;
        for (int i = piece; i < size(); i++) {
            ascending.pollFirst();
            ascending.addLast(get(i));
            double correlation = ascending.getAbsCorrelation();
            if (Math.abs(correlation - currentCorrelation) > correlationDiff) {
                double blockCorrelation = getCorrelation(startBlock, i);
                if (Math.abs(blockCorrelation) >= correlationBound) {
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
        result.add(new CorrelationBlock(ascending.getCorrelation(), startBlock, size()));
        return result;
    }

    private CorrelationBlock mergeBlocks(CorrelationBlock f, CorrelationBlock s) {
        if (f.end >= s.start && f.end < s.end) {
            double correlation = getCorrelation(f.start, s.end);
            return Math.abs(correlation) >= 0.3 ? new CorrelationBlock(correlation, f.start, s.end) : null;
        }
        return null;
    }

    private List<CorrelationBlock> merge(List<CorrelationBlock> blocks) {
        List<CorrelationBlock> merged = new ArrayList<>();
        CorrelationBlock merging = null;
        for (CorrelationBlock block : blocks) {
            if (merging == null) {
                merging = block;
            } else {
                CorrelationBlock nresult = mergeBlocks(merging, block);
                if (nresult == null) {
                    merged.add(merging);
                    merging = block;
                } else {
                    merging = nresult;
                }
            }
        }
        merged.add(merging);
        return merged;
    }

    public CorrelationBlock getListCorrelation() {
        CorrelationBlock block = new CorrelationBlock(getCorrelation(), 0, size());
        return getLagIndex(block);
    }

    public List<CorrelationBlock> getListCorrelations() {
        int piece = 5;
        double diff = 0.5;
        double bound = 0.3;
        return (getLagIndices(merge(getBlocks(piece, diff, bound))));
    }

    private CorrelationBlock getLagIndex(CorrelationBlock block) {
        List<Double> firstSub = first.subList(block.start, block.end);
        double bestCorrelation = block.correlation;
        for (int i = 0; i < block.end - block.start; i += (block.end - block.start)/50 + 1) {
            List<Double> secondSub = second.subList(i + block.start, i + block.end > size() ? size() : i + block.end);
            for (int j = secondSub.size(); j < firstSub.size(); j++) {
                secondSub.add(0.0);
            }
            double correlation = Correlation.getMaxAbsCorrelation(firstSub, secondSub);
            if (correlation > bestCorrelation) {
                bestCorrelation = correlation;
                block.correlationLagIndex = i;
            }
        }
        return block;
    }

    private List<CorrelationBlock> getLagIndices(List<CorrelationBlock> blocks) {
        for (CorrelationBlock block : blocks) {
            block = getLagIndex(block);
        }
        return blocks;
    }

    public static ListPair fromResource(URL resource, String separator) {
        try {
            List<String> lines = Files.readAllLines(Paths.get(resource.toURI()), StandardCharsets.UTF_8);
            List<Double> first = new ArrayList<>();
            List<Double> second = new ArrayList<>();
            for (String line : lines) {
                String[] splatted = line.split(separator);
                first.add(Double.parseDouble(splatted[1].replace(',', '.')));
                second.add(Double.parseDouble(splatted[2].replace(',', '.')));
            }
            return new ListPair(first, second);
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
        }
        return new ListPair();
    }

    public static ListPair fromResource(URL resource) {
        return fromResource(resource, ";");
    }
}
