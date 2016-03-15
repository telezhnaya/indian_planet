package com.atc.javacontest;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class CompareRowImpl implements ICompareRow {

    /**
     * Метод для вычисления первых тестов
     * /offtop: Для последних двух также возвращает приемлимый результат
     * @param resource Данные
     * @return Возвращает промежуток корреляции
     */
    private CorrelationResultIndex executeFirstTests(URL resource) {
        CorrelationBlock block = ListPair.fromResource(resource).getListCorrelation();
        block.printStats();
        return block.toResultIndex();
    }

    /**
     * Метод для вычисления последних двух тестов
     * @param resource Данные
     * @param separator Разделитель в данных
     * @return Возвращает список промежутков корреляции
     */
    private List<CorrelationResultIndex> executeLatterTests(URL resource, String separator) {
        List<CorrelationBlock> blocks = ListPair.fromResource(resource, separator).toListPair().getListCorrelations();
        List<CorrelationResultIndex> indices = new ArrayList<>(blocks.size());
        for (CorrelationBlock b : blocks) {
            b.printStats();
            indices.add(b.toResultIndex());
        }
        return indices;
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
		return executeFirstTests(resource);
	}

	public CorrelationResultIndex executeTest2(URL resource) {
        return executeFirstTests(resource);
	}

	public CorrelationResultIndex executeTest3(URL resource) {
        return executeFirstTests(resource);
	}

	public CorrelationResultIndex executeTest4(URL resource) {
        return executeFirstTests(resource);
	}

	public CorrelationResultIndex executeTest5(URL resource) {
        return executeFirstTests(resource);
	}

	public CorrelationResultIndex executeTest6(URL resource) {
        return executeFirstTests(resource);
	}

	public CorrelationResultIndex executeTest7(URL resource) {
        return executeFirstTests(resource);
	}

	public CorrelationResultIndex executeTest8(URL resource) {
        return executeFirstTests(resource);
	}

	public CorrelationResultIndex executeTest9(URL resource) {
        return executeFirstTests(resource);
	}

	public List<CorrelationResultIndex> executeTest10(URL resource) {
        return executeLatterTests(resource, ";");
	}

	public List<CorrelationResultIndex> executeTest11(URL resource) {
		return executeLatterTests(resource, ",");
	}


}
