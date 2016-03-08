package com.atc.javacontest;

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

        public double getCorrelation(int start, int finish) {
            return Correlation.getMaxCorrelation(first.subList(start, finish), second.subList(start, finish));
        }

        public double getAbsCorrelation(int start, int finish) {
            return Math.abs(getCorrelation(start, finish));
        }

		final int piece = 2;

		public List<CorrelationResultIndex> getCorrelations() {
			List<CorrelationResultIndex> result = new ArrayList<>();
			Deque<Double> ascendingFirst = new ArrayDeque<>(first.subList(0, piece));
			Deque<Double> ascendingSecond = new ArrayDeque<>(second.subList(0, piece));
			double currentCorrelation = Correlation.getMaxCorrelation(ascendingFirst, ascendingSecond);
			int startBlock = 0;
			for (int i = 5; i < first.size(); i++) {
				ascendingFirst.removeFirst();
				ascendingSecond.removeFirst();
				ascendingFirst.addLast(first.get(i));
				ascendingSecond.addLast(second.get(i));
				double correlation = Correlation.getMaxCorrelation(ascendingFirst, ascendingSecond);
				if (Math.abs(currentCorrelation - correlation) > 0.4) { // Fucking magic!
					double blockCorrelation = Correlation.getMaxCorrelation(first.subList(startBlock, i), second.subList(startBlock, i));
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
					ascendingFirst.addAll(first.subList(i - 1, i + piece <= 1000 ? i + piece : 1000));
					ascendingSecond.addAll(second.subList(i - 1, i + piece <= 1000 ? i + piece : 1000));
					i += piece;
				} else {
					currentCorrelation = correlation;
				}
			}
			CorrelationResultIndex lastIndex = new CorrelationResultIndex();
			lastIndex.correlation = Correlation.getMaxCorrelation(first.subList(startBlock, first.size()), second.subList(startBlock, second.size()));
			lastIndex.startIndex = String.valueOf(startBlock);
			lastIndex.endIndex = String.valueOf(first.size() - 1);
			result.add(lastIndex);
			return mergef(result);
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
					double correlation = Correlation.getMaxCorrelation(
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
				double ncorrelation = Correlation.getMaxCorrelation(first.subList(start, end), second.subList(start, end));
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
				double ncorrelation = Correlation.getMaxCorrelation(first.subList(start, end), second.subList(start, end));
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
        List<CorrelationResultIndex> result = pair.getCorrelations();
        for (CorrelationResultIndex t : result) {
            System.out.println("start: " + t.startIndex);
            System.out.println("end: " + t.endIndex);
            System.out.println("corr: " + t.correlation);
            System.out.println("===================================");
        }
        return null;
	}

	public CorrelationResultIndex executeTest5(URL resource) {
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

	public CorrelationResultIndex executeTest6(URL resource) {
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

	public CorrelationResultIndex executeTest7(URL resource) {
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

	public CorrelationResultIndex executeTest8(URL resource) {
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

	public CorrelationResultIndex executeTest9(URL resource) {
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

	public List<CorrelationResultIndex> executeTest10(URL resource) {
		ArrayPair pair = getNumbers(resource);
		List<CorrelationResultIndex> result = pair.getCorrelations();
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
