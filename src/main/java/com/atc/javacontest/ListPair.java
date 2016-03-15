package com.atc.javacontest;

import edu.mines.jtk.dsp.LocalCorrelationFilter;
import org.apache.commons.configuration.SystemConfiguration;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;
import org.apache.commons.math3.util.Pair;

import java.io.IOException;
import java.nio.file.Files;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
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

    private List<CorrelationBlock> getLagIndices(List<CorrelationBlock> blocks) {
        for (CorrelationBlock block : blocks) {
            int bestLag = 0;
            List<Double> diffs = new ArrayList<>(block.end - block.start);
            for (int i = 0; i < block.end - block.start; i++) {
                diffs.add(i, 0.0);
            }
            for (int i = 0; i < block.end - block.start; i++) {
                for (int k = block.start; k < block.end; k++) {
                    diffs.add(i, diffs.get(i) + first.get(k) * (second.get((i + k) % (block.end - block.start))));
                }
                if (diffs.get(bestLag) < diffs.get(i)) {
                    bestLag = i;
                }
            }
            block.correlationLagIndex = bestLag;
        }
        return blocks;
    }

    public CorrelationBlock getListCorrelation() {
        CorrelationBlock block = new CorrelationBlock(getCorrelation(), 0, size());
        block.correlationLagIndex = getCorrLagManual(0, block.end);
        return block;
    }

    public List<CorrelationBlock> getListCorrelations() {
        int piece = 5;
        double diff = 0.5;
        double bound = 0.3;
        return (getManualBlocks(merge(getBlocks(piece, diff, bound))));
    }

    private double getCorrLagManual(int start, int end) {
        List<Double> firstSub = first.subList(start, end);
        int bestLag = 0;
        double bestCorrelation = Correlation.getMaxAbsCorrelation(firstSub, second.subList(start, end));
        for (int i = 0; i < end - start; i += (end - start)/50 + 1) {
            List<Double> secondSub = second.subList(i + start, i + end > size() ? size() : i + end);
            for (int j = secondSub.size(); j < firstSub.size(); j++) {
                secondSub.add(0.0);
            }
            double correlation = Correlation.getMaxAbsCorrelation(firstSub, secondSub);
            if (correlation > bestCorrelation) {
                bestCorrelation = correlation;
                bestLag = i;
            }
        }
        return bestLag;
    }

    private List<CorrelationBlock> getManualBlocks(List<CorrelationBlock> blocks) {
        for (CorrelationBlock block : blocks) {
            block.correlationLagIndex = getCorrLagManual(block.start, block.end);
        }
        return blocks;
    }

    private double getAutoCorrLag(int start, int end) {
        LocalCorrelationFilter filter = new LocalCorrelationFilter(
                LocalCorrelationFilter.Type.SIMPLE,
                LocalCorrelationFilter.Window.GAUSSIAN,
                2);
        filter.setInputs(
                toFloatArray(first, start, end),
                toFloatArray(second, start, end));
        double bestCorrelation = getCorrelation(start, end);
        int bestLag = 0;
        for (int i = 0; i < end - start; i += ((end - start)/50) + 1) {
            float[] fl = new float[end - start];
            filter.correlate(i, fl);
            filter.normalize(i, fl);
            double corr = Correlation.getMaxAbsCorrelation(first.subList(start, end), toDoubleList(fl));
            if (corr > bestCorrelation) {
                bestCorrelation = corr;
                bestLag = i;
            }
        }
        return bestLag;
    }

    private List<CorrelationBlock> getAutoBlocks(List<CorrelationBlock> blocks) {
        for (CorrelationBlock block : blocks) {
            block.correlationLagIndex = getAutoCorrLag(block.start, block.end);
        }
        return blocks;
    }

    private List<Double> toDoubleList(float[] array) {
        ArrayList<Double> result = new ArrayList<>(array.length);
        for (float a : array) {
            result.add((double)a);
        }
        return result;
    }

    private float[] toFloatArray(List<Double> list, int start, int end) {
        float[] result = new float[end - start];
        for (int i = 0; i < end - start; i++) {
            result[i] = list.get(i + start).floatValue();
        }
        return result;
    }

    private Pair<Integer, Integer> getMax(int start, int end) {
        double max1 = Double.MIN_VALUE, max2 = Double.MIN_VALUE;
        int maxIndex1 = -1, maxIndex2 = -1;
        for (int i = start; i < end - 1; i++) {
            if (first.get(i) > max1) max1 = first.get(i); maxIndex1 = i;
            if (second.get(i) > max2) max2 = second.get(i); maxIndex2 = i;
        }
        return Pair.create(maxIndex1, maxIndex2);
    }

    private Pair<Integer, Integer> getMin(int start, int end) {
        double min1 = Double.MAX_VALUE, min2 = Double.MAX_VALUE;
        int minIndex1 = -1, minIndex2 = -1;
        for (int i = start; i < end - 1; i++) {
            if (first.get(i) < min1) min1 = first.get(i); minIndex1 = i;
            if (second.get(i) < min2) min2 = second.get(i); minIndex2 = i;
        }
        return Pair.create(minIndex1, minIndex2);
    }

    private Pair<Double, Double> getRoughMaxAndMin(List<Double> sublist, int neededNumbers) {
        Collections.sort(sublist);
        Double roughMax = 0.0, roughMin = 0.0;
        for (int i = 0, j = sublist.size(); i < neededNumbers; i++, j--) {
            roughMax += sublist.get(i);
            roughMin += sublist.get(i);
        }
        return Pair.create(roughMax / neededNumbers, roughMin / neededNumbers);
    }

    private Pair<List<Integer>, List<Integer>> findSome(CorrelationBlock block) {
        int localMaxPoint = -1, localMinPoint = -1;
        List<Integer> maxs = new ArrayList<>();
        List<Integer> mins = new ArrayList<>();
        int analyticPiece = ((block.end - block.start)/(size() / (block.end - block.start))) + 1;
        Pair<Double, Double> firstRough = getRoughMaxAndMin(first.subList(block.start, block.end), 3);
        Pair<Double, Double> secondRough = getRoughMaxAndMin(second.subList(block.start, block.end), 3);
        for (int i = block.start; i < block.end; i += analyticPiece) {
            Pair<Integer, Integer> localMax = getMax(i, i + analyticPiece);
            Pair<Integer, Integer> localMin = getMin(i, i + analyticPiece);
            if (firstRough.getFirst() <= first.get(localMax.getFirst())) {
                maxs.add(localMax.getFirst());
            }
            if (secondRough.getFirst() <= second.get(localMax.getSecond())) {
                maxs.add(localMax.getSecond());
            }
            if (firstRough.getSecond() >= first.get(localMin.getFirst())) {
                mins.add(localMin.getFirst());
            }
            if (secondRough.getSecond() >= first.get(localMin.getSecond())) {
                mins.add(localMin.getSecond());
            }
        }

        return Pair.create(maxs, mins);
    }

    public List<CorrelationBlock> getMultiIndices(List<CorrelationBlock> blocks) {
        for (CorrelationBlock block : blocks) {
            Pair<List<Integer>, List<Integer>> pair = findSome(block);
            double maxPeriod = 0.0;
            double minPeriod = 0.0;
            for (int i = 1; i < pair.getFirst().size(); i++) {
                maxPeriod += pair.getFirst().get(i) - pair.getFirst().get(i - 1);
            }
            for (int i = 1; i < pair.getSecond().size(); i++) {
                minPeriod += pair.getSecond().get(i) - pair.getSecond().get(i - 1);
            }
            maxPeriod /= pair.getFirst().size();
            minPeriod /= pair.getSecond().size();
            double averagePeriod = (maxPeriod + minPeriod) / 2;
            block.printStats();
            System.out.println("period: " + averagePeriod);
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
