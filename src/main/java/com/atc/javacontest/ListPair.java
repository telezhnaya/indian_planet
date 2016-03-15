package com.atc.javacontest;

import org.apache.commons.math3.util.Pair;

import javax.annotation.Nullable;
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

/**
 * Класс, предназначеный для хранения двух списков
 * В данной задаче используется для хранения двух рядов чисел
 */
public class ListPair extends CollectionPair {
    List<Double> first;
    List<Double> second;

    /**
     * Строит ListPair из двух списков
     * @param first Первый список из которого будут взяты элементы
     * @param second Вторый список из которого будут взяты элементы
     */
    public ListPair(List<Double> first, List<Double> second) {
        this.first = first;
        this.second = second;
    }

    /**
     * Строит ListPair из CollectionPair
     * @param pair Пара из которой будут взяты элементы
     */
    public ListPair(CollectionPair pair) {
        this.first = new ArrayList<>(pair.getFirst());
        this.second = new ArrayList<>(pair.getSecond());
    }

    /**
     * Конструктор, создающий пустую пару списков
     */
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

    /**
     * Метод возвращающий PairList на заданном участке
     * @param start Начало участка(Включенное в интервал)
     * @param finish Конец участка(Исключеный из интервала)
     * @return Возвращает ListPair, сформированный из элементов на заданном участке
     */
    public ListPair subList(int start, int finish) {
        return new ListPair(first.subList(start, finish), second.subList(start, finish));
    }

    /**
     * Возвращает корреляцию для ListPair на ограниченном участке
     * @param start Начало участка(Включеноное в участок)
     * @param finish Конец участка(Исключенный из участка)
     * @return Возвращает корреляцию между хранимыми списками на заданном участке
     */
    public double getCorrelation(int start, int finish) {
        return Correlation.getMaxCorrelation(
                first.subList(start, finish),
                second.subList(start, finish)
        );
    }

    /**
     * Возвращает корреляцию для ListPair
     * @return Возвращает корреляцию для ListPair
     */
    public double getCorrelation() {
        return Correlation.getMaxCorrelation(first, second);
    }

    /**
     * Возвращает корреляцию по модулю для ListPair на ограниченном участке
     * @param start Начало участка(Включеноное в участок)
     * @param finish Конец участка(Исключенный из участка)
     * @return Возвращает модуль корреляции между хранимыми списками на заданном участке
     */
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

    /**
     * Получает пару элементов по заданному индексу
     * @param index индекс элемента
     * @return Возвращает пару элементов(первый элемент из первого списка, второй - из второго)
     */
    public Pair<Double, Double> get(int index) {
        return Pair.create(first.get(index), second.get(index));
    }

    /**
     * Метод вычисляющий промежутки корреляции на всем протежении ListPair.
     * Метод пытается найти куски наиболее возможного размера на которых сохраняется похожая корреляция.
     * Чем меньше изначальный кусок и приемлимая разница, тем лучше получаемый результат(при этом падает скорость вычислений)
     * @param piece Изначальный размер куска на котором считается корреляция
     * @param correlationDiff Максимально приемлимая разница между корреляциями на куске
     * @param correlationBound Приемлимый уровень корреляции, при котором участок должен быть сохранен
     * @return Возвращает список блоков корреляции(специальный класс для представления корреляции на промежутке)
     */
    private List<CorrelationBlock> getBlocks(int piece, double correlationDiff, double correlationBound) {
        List<CorrelationBlock> result = new ArrayList<>();
        DequePair ascending = new DequePair(subList(0, piece));
        double currentCorrelation = ascending.getAbsCorrelation();
        int startBlock = 0;
        for (int i = piece; i < size(); i++) {
            ascending.pollFirst(); // На каждом шаге меняем первый элемент куска
            ascending.addLast(get(i)); // На последний
            double correlation = ascending.getAbsCorrelation(); // Считаем корреляцию с новым элементом
            if (Math.abs(correlation) - Math.abs(currentCorrelation) > correlationDiff) { // Проверяем разницу
                double blockCorrelation = getCorrelation(startBlock, i);
                // Если корреляция лучше заданной, добавляем участок в результат
                if (Math.abs(blockCorrelation) >= correlationBound) {
                    CorrelationBlock block = new CorrelationBlock(blockCorrelation, startBlock, i);
                    result.add(block);
                    block.printlnStats();
                }
                // Смещаем кусок
                startBlock = i;
                ascending.clear();
                i = i + piece < size() ? i + piece : size();
                ascending.addAll(subList(i - 6, i - 1));
            } else { // Обновляем корреляцию если разница была достаточно маленькой
                currentCorrelation = correlation;
            }
        }
        // Добавляем последний оставщийся кусок
        result.add(new CorrelationBlock(ascending.getCorrelation(), startBlock, size()));
        return result;
    }

    /**
     * Функция, соединяющая два рядом стоящих куска, если уровень корреляции выше приемлемого
     * @param f Первый соединяемый кусок
     * @param s Второй соединяемый кусок
     * @param correlationBound Приемлимый уровень корреляции
     * @return Возвращает новый блок, если соединение удалось, иначе возвращает null
     */
    @Nullable
    private CorrelationBlock mergeBlocks(CorrelationBlock f, CorrelationBlock s, double correlationBound) {
        if (f.end >= s.start && f.end < s.end) {
            double correlation = getCorrelation(f.start, s.end);
            return Math.abs(correlation) >= 0.3 ? new CorrelationBlock(correlation, f.start, s.end) : null;
        }
        return null;
    }

    /**
     * Функция, соединяющая как можно больше передаваемых кусков.
     * @param blocks Куски, которые требуется соединить
     * @param correlationBound Приемлимый уровень корреляции
     * @return Возвращает список кусков
     */
    private List<CorrelationBlock> merge(List<CorrelationBlock> blocks, double correlationBound) {
        List<CorrelationBlock> merged = new ArrayList<>();
        CorrelationBlock merging = null;
        for (CorrelationBlock block : blocks) {
            if (merging == null) {
                merging = block;
            } else {
                CorrelationBlock nresult = mergeBlocks(merging, block, correlationBound);
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

    /**
     * Функция, возвращающая промежуточный результат для первых девяти тестов
     * @return Возвращает блок корреляции для всего участка
     */
    public CorrelationBlock getListCorrelation() {
        CorrelationBlock block = new CorrelationBlock(getCorrelation(), 0, size());
        return getLagIndex(block);
    }

    /**
     * Функция, возвращающая промежуточный результат для 10 и 11 теста
     * @return Возвращает список блоков корреляции
     */
    public List<CorrelationBlock> getListCorrelations() {
        int piece = 5;
        double diff = 0.5;
        double bound = 0.3;
        return (getLagIndices(merge(getBlocks(piece, diff, bound), bound)));
    }

    /**
     * Функция, вычисляющая линейную прибавку разницы значений между рядами для заданного блока
     * @param block Блок, для которого считается линейная прибавка(Блок изменяется)
     * @return Возвращает передаваемый блок с заданной линейной прибавкой
     */
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

    /**
     *  Функция, вычисляющая линейную прибавку разницы значений между рядами для списка блоков
     * @param blocks Блоки, для которых считается линейная прибавка(блоки изменяются)
     * @return Возвращает переданные блоки с заданными линейными прибавками
     */
    private List<CorrelationBlock> getLagIndices(List<CorrelationBlock> blocks) {
        for (CorrelationBlock block : blocks) {
            block = getLagIndex(block);
        }
        return blocks;
    }

    /**
     * Функция, строящая ListPair из данных по заданному URL
     * @param resource Данные по которым будет строится ListPair
     * @param separator Разделитель данных
     * @return Возвращает построенный и заполненный ListPair
     */
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
    /**
     * Функция, строящая ListPair из данных по заданному URL с разделителем ";"
     * @param resource Данные по которым будет строится ListPair
     * @return Возвращает построенный и заполненный ListPair
     */
    public static ListPair fromResource(URL resource) {
        return fromResource(resource, ";");
    }
}
