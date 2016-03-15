package com.atc.javacontest;

/**
 * Created by Sergey on 08.03.16.
 */

/**
 * Класс для представления блока(промежутка) корреляции
 * Используется, так как предназначенный для этого класс CorrelationResultIndex не является достаточно удобным
 */
public class CorrelationBlock {
    /*
	 * коэффициент корреляции между рядами от -1 до 1
	 */
    public double correlation = 0.0;

    /*
     * линейный множитель разницы значений между рядами
     */
    public double correlationMutipleIndex = 0.0;

    /*
     * линейная прибавка разницы значений между рядами
     */
    public double correlationLagIndex = 0.0;

    /*
     * другой индекс разницы значений между рядами (на усмотрение разработчика)
     */
    public String anotherIndex = "";
    /*
     * описание сути другого индекса разницы значений между рядами (на усмотрение разработчика)
     */
    public String anotherIndexDesc = "";

    public int start;

    public int end;

    public CorrelationBlock(double correlation, int start, int end) {
        this.correlation = correlation;
        this.start = start;
        this.end = end;
    }

    public CorrelationResultIndex toResultIndex() {
        CorrelationResultIndex index = new CorrelationResultIndex();
        index.correlation = correlation;
        index.startIndex = String.valueOf(start);
        index.endIndex = String.valueOf(end);
        return index;
    }

    public void printlnStats() {
        System.out.println("start: " + start);
        System.out.println("end: " + end);
        System.out.println("corr: " + correlation);
        System.out.println("===================================");
    }

    public void printStats() {
        System.out.print("start: " + start);
        System.out.print(", end: " + end);
        System.out.print(", lag: " + correlationLagIndex);
        System.out.print(", mult: " + correlationMutipleIndex);
        System.out.println(", corr: " + correlation);
    }
}
