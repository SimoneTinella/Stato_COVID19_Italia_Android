package org.twistedappdeveloper.statocovid19italia.model;

import java.util.ArrayList;
import java.util.List;

public class RowData implements Comparable<RowData> {
    private String name, value, key;
    private Integer position, color;
    private List<RowData> subItems;

    public RowData(String name, String value, int color, int position, String key) {
        this.name = name;
        this.value = value;
        this.color = color;
        this.position = position;
        this.key = key;
        subItems = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
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

    public void addSubItem(RowData subItem) {
        this.subItems.add(subItem);
    }

    @Override
    public int compareTo(RowData o) {
        return this.position.compareTo(o.position);
    }
}
