package org.twistedappdeveloper.statocovid19italia.model;

import java.util.ArrayList;
import java.util.List;

public class RowData implements Comparable<RowData> {
    private String name, key;
    private int position, color, value, delta, precValue;
    private List<RowData> subItems;
    private float deltaPercentage;

    public RowData(String name, int value, int color, int position, String key) {
        this.name = name;
        this.value = value;
        this.color = color;
        this.position = position;
        this.key = key;
        subItems = new ArrayList<>();
    }

    public RowData(String name, int value, int color, int position, String key, float deltaPercentage, int delta, int precValue) {
        this.name = name;
        this.value = value;
        this.color = color;
        this.position = position;
        this.key = key;
        this.deltaPercentage = deltaPercentage;
        this.delta = delta;
        this.precValue = precValue;
        subItems = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getValue() {
        return this.value;
    }

    public int getColor() {
        return this.color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public String getKey() {
        return key;
    }

    public List<RowData> getSubItems() {
        return subItems;
    }

    public float getDeltaPercentage() {
        return deltaPercentage;
    }

    public int getDelta() {
        return delta;
    }

    public int getPrecValue() {
        return precValue;
    }

    public void addSubItem(RowData subItem) {
        this.subItems.add(subItem);
    }

    @Override
    public int compareTo(RowData o) {
        return Integer.valueOf(this.position).compareTo(o.position);
    }
}
