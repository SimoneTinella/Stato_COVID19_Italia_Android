package org.twistedappdeveloper.statocovid19italia.model;

public class Data implements Comparable<Data>{
    private String Name, Value, Color;
    private Integer position;

    public Data(String name, String value, String color, int position) {
        Name = name;
        Value = value;
        Color = color;
        this.position = position;
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

    public void setValue(String value) {
        Value = value;
    }

    public String getColor() {
        return Color;
    }

    public void setColor(String color) {
        Color = color;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    @Override
    public int compareTo(Data o) {
        return this.position.compareTo(o.position);
    }
}
