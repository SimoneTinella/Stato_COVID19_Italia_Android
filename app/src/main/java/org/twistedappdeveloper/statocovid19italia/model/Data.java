package org.twistedappdeveloper.statocovid19italia.model;

public class Data implements Comparable<Data> {
    private String Name, Value, key;
    private Integer position, Color;

    public Data(String name, String value, int color, int position, String key) {
        Name = name;
        Value = value;
        Color = color;
        this.position = position;
        this.key = key;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getValue() {
        return Value;
    }

    public int getColor() {
        return Color;
    }

    public void setColor(int color) {
        Color = color;
    }

    public String getKey() {
        return key;
    }

    @Override
    public int compareTo(Data o) {
        return this.position.compareTo(o.position);
    }
}
