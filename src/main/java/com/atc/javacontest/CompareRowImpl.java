package com.atc.javacontest;

import org.apache.commons.collections.ArrayStack;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;
import org.apache.commons.math3.stat.correlation.KendallsCorrelation;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class CompareRowImpl implements ICompareRow {

	private class ArrayPair {
		public final ArrayList<Double> first;
		public final ArrayList<Double> second;

		public ArrayPair(ArrayList<Double> first, ArrayList<Double> second) {
			this.first = first;
			this.second = second;
		}

		private double[] toPrimitive(Collection<Double> list) {
			return ArrayUtils.toPrimitive(list.toArray(new Double[list.size()]));
		}

		public double[] firstToArray() {
			return toPrimitive(first);
		}

		public double[] secondToArray() {
			return toPrimitive(second);
		}

		public double getPearsonsCorrelation(Collection<Double> f, Collection<Double> s) {
			return new PearsonsCorrelation().correlation(toPrimitive(f), toPrimitive(s));
		}

		public double getSpearmansCorrelation(Collection<Double> f, Collection<Double> s) {
			return new SpearmansCorrelation().correlation(toPrimitive(f), toPrimitive(s));
		}

		public double getKendallsCorrelation(Collection<Double> f, Collection<Double> s) {
			return new KendallsCorrelation().correlation(toPrimitive(f), toPrimitive(s));
		}

		public double getPearsonsCorrelation() {
			return new PearsonsCorrelation().correlation(firstToArray(), secondToArray());
		}

		public double getSpearmansCorrelation() {
			return new SpearmansCorrelation().correlation(firstToArray(), secondToArray());
		}

		public double getKendallsCorrelation() {
			return new KendallsCorrelation().correlation(firstToArray(), secondToArray());
		}

		public double getMaxCorrelation(Collection<Double> f, Collection<Double> s) {
			double a = getKendallsCorrelation(f, s);
			double b = getKendallsCorrelation(f, s);
			double c = getKendallsCorrelation(f, s);
			return Math.max(a, Math.max(b, c));
		}

		public void printStats() {
			System.out.println(getKendallsCorrelation());
			System.out.println(getPearsonsCorrelation());
			System.out.println(getSpearmansCorrelation());
		}

		public List<CorrelationResultIndex> getCorrelations() {
			List<CorrelationResultIndex> result = new ArrayList<>();
			Deque<Double> ascendingFirst = new ArrayDeque<>(first.subList(0, 5));
			Deque<Double> ascendingSecond = new ArrayDeque<>(second.subList(0, 5));
			double currentCorrelation = getMaxCorrelation(ascendingFirst, ascendingSecond);
			int startBlock = 0;
			for (int i = 5; i < first.size(); i++) {
				ascendingFirst.removeFirst();
				ascendingSecond.removeFirst();
				ascendingFirst.addLast(first.get(i));
				ascendingSecond.addLast(second.get(i));
				double correlation = getMaxCorrelation(ascendingFirst, ascendingSecond);
				if (Math.abs(currentCorrelation - correlation) > 0.4) { // Fucking magic!
					double blockCorrelation = getMaxCorrelation(first.subList(startBlock, i), second.subList(startBlock, i));
					if (Math.abs(blockCorrelation) >= 0.3) {
						CorrelationResultIndex index = new CorrelationResultIndex();
						index.correlation = blockCorrelation;
						index.startIndex = String.valueOf(startBlock);
						index.endIndex = String.valueOf(i);
						result.add(index);
						System.out.println("start: " + index.startIndex);
						System.out.println("end: " + index.endIndex);
						System.out.println("corr: " + index.correlation);
						System.out.println("===================================");
					}
					startBlock = i;
					ascendingFirst.clear();
					ascendingSecond.clear();
					ascendingFirst.addAll(first.subList(i - 1, i + 5));
					ascendingSecond.addAll(second.subList(i - 1, i + 5));
					i += 5;
				} else {
					currentCorrelation = correlation;
				}
			}
			CorrelationResultIndex lastIndex = new CorrelationResultIndex();
			lastIndex.correlation = getMaxCorrelation(first.subList(startBlock, first.size()), second.subList(startBlock, second.size()));
			lastIndex.startIndex = String.valueOf(startBlock);
			lastIndex.endIndex = String.valueOf(first.size() - 1);
			result.add(lastIndex);
			return grow(mergef(result));
		}

		public List<CorrelationResultIndex> merge(List<CorrelationResultIndex> draftCorrelations) {
			List<CorrelationResultIndex> result = new ArrayList<>();
			for (int i = 0; i < draftCorrelations.size() - 1; i++) {
				CorrelationResultIndex firstIndex = draftCorrelations.get(i);
				CorrelationResultIndex secondIndex = draftCorrelations.get(i + 1);
				int firstStartIndex = Integer.parseInt(firstIndex.startIndex);
				int secondStartIndex = Integer.parseInt(secondIndex.startIndex);
				int firstEndIndex = Integer.parseInt(firstIndex.endIndex);
				int secondEndIndex = Integer.parseInt(secondIndex.endIndex);
				if (firstEndIndex >= secondStartIndex) {
					double correlation = getMaxCorrelation(
							first.subList(firstStartIndex, secondEndIndex),
							second.subList(firstStartIndex, secondEndIndex)
					);
					if (Math.abs(correlation) >= 0.3) {
						CorrelationResultIndex index = new CorrelationResultIndex();
						index.correlation = correlation;
						index.startIndex = String.valueOf(firstStartIndex);
						index.endIndex = String.valueOf(secondEndIndex);
						result.add(index);
					} else {
						result.add(firstIndex);
					}
				}
			}
			return result;
		}

		public List<CorrelationResultIndex> mergef(List<CorrelationResultIndex> draft) {
			Deque<CorrelationResultIndex> temprary = new ArrayDeque<>(draft);
			Deque<CorrelationResultIndex> result = new ArrayDeque<>();
			while(temprary.size() >= 2) {
				CorrelationResultIndex firstIndex = temprary.pollFirst();
				CorrelationResultIndex secondIndex = temprary.pollFirst();
				int firstStartIndex = Integer.parseInt(firstIndex.startIndex);
				int secondStartIndex = Integer.parseInt(secondIndex.startIndex);
				int firstEndIndex = Integer.parseInt(firstIndex.endIndex);
				int secondEndIndex = Integer.parseInt(secondIndex.endIndex);
				if (firstEndIndex >= secondStartIndex) {
					double correlation = getMaxCorrelation(
							first.subList(firstStartIndex, secondEndIndex),
							second.subList(firstStartIndex, secondEndIndex)
					);
					if (Math.abs(correlation) >= 0.3) {
						CorrelationResultIndex index = new CorrelationResultIndex();
						index.correlation = correlation;
						index.startIndex = String.valueOf(firstStartIndex);
						index.endIndex = String.valueOf(secondEndIndex);
						temprary.addFirst(index);
					} else {
						result.add(firstIndex);
						temprary.addFirst(secondIndex);
					}
				} else {
					result.add(firstIndex);
					temprary.addFirst(secondIndex);
				}
			}
			if (!temprary.isEmpty()) {
				result.add(temprary.pollFirst());
			}
			return new ArrayList<>(result);
		}

		public List<CorrelationResultIndex> grow(List<CorrelationResultIndex> draft) {
			for (CorrelationResultIndex index : draft) {
				index = growLeft(index);
				index = growRight(index);
			}

			return draft;
		}

		public CorrelationResultIndex growLeft(CorrelationResultIndex index) {
			int start = Integer.parseInt(index.startIndex);
			int end = Integer.parseInt(index.endIndex);
			double correlation = 0.0;
			for (; start > 0; start--) {
				double ncorrelation = getMaxCorrelation(first.subList(start, end), second.subList(start, end));
				if (Math.abs(ncorrelation) < 0.3) { // FIXME: 05.03.16
					start += 1;
					break;
				} else {
					correlation = ncorrelation;
				}
			}
			index.correlation = correlation;
			index.startIndex = String.valueOf(start);
			index.endIndex = String.valueOf(end);

			return index;
		}

		public CorrelationResultIndex growRight(CorrelationResultIndex index) {
			int start = Integer.parseInt(index.startIndex);
			int end = Integer.parseInt(index.endIndex);
			double correlation = 0.0;
			for (; end < first.size(); end++) {
				double ncorrelation = getMaxCorrelation(first.subList(start, end), second.subList(start, end));
				if (Math.abs(ncorrelation) < 0.3) { // FIXME: 05.03.16
					end -= 1;
					break;
				} else {
					correlation = ncorrelation;
				}
			}
			index.correlation = correlation;
			index.startIndex = String.valueOf(start);
			index.endIndex = String.valueOf(end);

			return index;
		}

	}

	private ArrayPair getNumbers(URL resource) {
		try {
			List<String> lines = Files.readAllLines(Paths.get(resource.toURI()), StandardCharsets.UTF_8);

			ArrayList<Double> first =  new ArrayList<>(lines.size());
			ArrayList<Double> second = new ArrayList<>(lines.size());
			for (String line : lines) {
				String[] splatted = line.split(";");
				first.add(Double.parseDouble(splatted[1].replace(',', '.')));
				second.add(Double.parseDouble(splatted[2].replace(',', '.')));
			}
			return new ArrayPair(first, second);
		} catch (URISyntaxException | IOException e) {
			e.printStackTrace();
		}
		return new ArrayPair(new ArrayList<Double>(), new ArrayList<Double>());
	}

	public CorrelationResultIndex executeTest0(URL resource) {
		CorrelationResultIndex index = new CorrelationResultIndex();
		try {
			List<String> lines = Files.readAllLines(Paths.get(resource.toURI()), StandardCharsets.UTF_8);
			int count = 0;
			int equals = 0;
			for(String line:lines){
				String[] values = line.split(";");
				if(Double.parseDouble(values[1].replace(',', '.')) == Double.parseDouble(values[2].replace(',', '.')) ){
					equals++;
				}
				count++;
			}
			index.correlation = equals/count;
			index.correlationLagIndex = 0;
			index.correlationMutipleIndex = 0;
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return index;
	}

	public CorrelationResultIndex executeTest1(URL resource) {
		ArrayPair pair = getNumbers(resource);
		List<CorrelationResultIndex> result = pair.getCorrelations();
		for (CorrelationResultIndex t : result) {
			System.out.println("start: " + t.startIndex);
			System.out.println("end: " + t.endIndex);
			System.out.println("corr: " + t.correlation);
			System.out.println("===================================");
		}
		return null;
	}

	public CorrelationResultIndex executeTest2(URL resource) {
		ArrayPair pair = getNumbers(resource);
		List<CorrelationResultIndex> result = pair.getCorrelations();
		for (CorrelationResultIndex t : result) {
			System.out.println("start: " + t.startIndex);
			System.out.println("end: " + t.endIndex);
			System.out.println("corr: " + t.correlation);
			System.out.println("===================================");
		}
		return null;
	}

	public CorrelationResultIndex executeTest3(URL resource) {
		ArrayPair pair = getNumbers(resource);
		List<CorrelationResultIndex> result = pair.getCorrelations();
		for (CorrelationResultIndex t : result) {
			System.out.println("start: " + t.startIndex);
			System.out.println("end: " + t.endIndex);
			System.out.println("corr: " + t.correlation);
			System.out.println("===================================");
		}
		return null;
	}

	public CorrelationResultIndex executeTest4(URL resource) {
		ArrayPair pair = getNumbers(resource);
		pair.printStats();
		return null;
	}

	public CorrelationResultIndex executeTest5(URL resource) {
		ArrayPair pair = getNumbers(resource);
		pair.printStats();
		return null;
	}

	public CorrelationResultIndex executeTest6(URL resource) {
		ArrayPair pair = getNumbers(resource);
		pair.printStats();
		return null;
	}

	public CorrelationResultIndex executeTest7(URL resource) {
		ArrayPair pair = getNumbers(resource);
		pair.printStats();
		return null;
	}

	public CorrelationResultIndex executeTest8(URL resource) {
		ArrayPair pair = getNumbers(resource);
		pair.printStats();
		return null;
	}

	public CorrelationResultIndex executeTest9(URL resource) {
		ArrayPair pair = getNumbers(resource);
		pair.printStats();
		return null;
	}

	public List<CorrelationResultIndex> executeTest10(URL resource) {
		ArrayPair pair = getNumbers(resource);
		List<CorrelationResultIndex> result = pair.getCorrelations();
		for (CorrelationResultIndex t : result) {
			System.out.println("start: " + t.startIndex);
			System.out.println("end: " + t.endIndex);
			System.out.println("corr: " + t.correlation);
			System.out.println("===================================");
		}
		return null;
	}

	public List<CorrelationResultIndex> executeTest11(URL resource) {
		ArrayPair pair = getNumbers(resource);
		List<CorrelationResultIndex> result = pair.getCorrelations();
		for (CorrelationResultIndex t : result) {
			System.out.println("start: " + t.startIndex);
			System.out.println("end: " + t.endIndex);
			System.out.println("corr: " + t.correlation);
			System.out.println("===================================");
		}
		return null;
	}


}
