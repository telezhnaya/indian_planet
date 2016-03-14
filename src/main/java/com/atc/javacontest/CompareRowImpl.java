package com.atc.javacontest;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class CompareRowImpl implements ICompareRow {

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
		CorrelationResultIndex result = new CorrelationList(resource).getCorrelationResultIndex();
		System.out.println("start: " + result.startIndex);
		System.out.println("end: " + result.endIndex);
		System.out.println("corr: " + result.correlation);
		System.out.println("lag: " + result.correlationLagIndex);
		System.out.println("===================================");
		return result;
	}

	public CorrelationResultIndex executeTest2(URL resource) {
		CorrelationResultIndex result = new CorrelationList(resource).getCorrelationResultIndex();
		System.out.println("start: " + result.startIndex);
		System.out.println("end: " + result.endIndex);
		System.out.println("corr: " + result.correlation);
		System.out.println("===================================");
		return result;
	}

	public CorrelationResultIndex executeTest3(URL resource) {
		CorrelationResultIndex result = new CorrelationList(resource).getCorrelationResultIndex();
		System.out.println("start: " + result.startIndex);
		System.out.println("end: " + result.endIndex);
		System.out.println("corr: " + result.correlation);
		System.out.println("===================================");
		return result;
	}

	public CorrelationResultIndex executeTest4(URL resource) {
		CorrelationResultIndex result = new CorrelationList(resource).getCorrelationResultIndex();
		System.out.println("start: " + result.startIndex);
		System.out.println("end: " + result.endIndex);
		System.out.println("corr: " + result.correlation);
		System.out.println("===================================");
		return result;
	}

	public CorrelationResultIndex executeTest5(URL resource) {
		CorrelationResultIndex result = new CorrelationList(resource).getCorrelationResultIndex();
		System.out.println("start: " + result.startIndex);
		System.out.println("end: " + result.endIndex);
		System.out.println("corr: " + result.correlation);
		System.out.println("===================================");
		return result;
	}

	public CorrelationResultIndex executeTest6(URL resource) {
		CorrelationResultIndex result = new CorrelationList(resource).getCorrelationResultIndex();
		System.out.println("start: " + result.startIndex);
		System.out.println("end: " + result.endIndex);
		System.out.println("corr: " + result.correlation);
		System.out.println("===================================");
		return result;
	}

	public CorrelationResultIndex executeTest7(URL resource) {
		CorrelationResultIndex result = new CorrelationList(resource).getCorrelationResultIndex();
		System.out.println("start: " + result.startIndex);
		System.out.println("end: " + result.endIndex);
		System.out.println("corr: " + result.correlation);
		System.out.println("===================================");
		return result;
	}

	public CorrelationResultIndex executeTest8(URL resource) {
		CorrelationResultIndex result = new CorrelationList(resource).getCorrelationResultIndex();
		System.out.println("start: " + result.startIndex);
		System.out.println("end: " + result.endIndex);
		System.out.println("corr: " + result.correlation);
		System.out.println("===================================");
		return result;
	}

	public CorrelationResultIndex executeTest9(URL resource) {
		CorrelationResultIndex result = new CorrelationList(resource).getCorrelationResultIndex();
			System.out.println("start: " + result.startIndex);
			System.out.println("end: " + result.endIndex);
			System.out.println("corr: " + result.correlation);
			System.out.println("===================================");
		return result;
	}

	public List<CorrelationResultIndex> executeTest10(URL resource) {
		List<CorrelationResultIndex> result = new CorrelationList(resource).getCorrelationResultIndexes();
		for (CorrelationResultIndex t : result) {
			System.out.println("start: " + t.startIndex);
			System.out.println("end: " + t.endIndex);
			System.out.println("corr: " + t.correlation);
            System.out.println("lag: " + t.correlationLagIndex);
			System.out.println("===================================");
		}
		return result;
	}

	public List<CorrelationResultIndex> executeTest11(URL resource) {
		List<CorrelationResultIndex> result = new CorrelationList(resource, ",").getCorrelationResultIndexes();
		for (CorrelationResultIndex t : result) {
			System.out.println("start: " + t.startIndex);
			System.out.println("end: " + t.endIndex);
			System.out.println("corr: " + t.correlation);
            System.out.println("lag: " + t.correlationLagIndex);
			System.out.println("===================================");
		}
		return result;
	}


}
