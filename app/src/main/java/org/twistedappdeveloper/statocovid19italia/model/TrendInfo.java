package org.twistedappdeveloper.statocovid19italia.model;

import java.util.ArrayList;

public class TrendInfo{
    private String name, key;
    private ArrayList<Integer> values;

    public TrendInfo(String name, String key, ArrayList<Integer> values) {
        this.name = name;
        this.key = key;
        this.values = values;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public ArrayList<Integer> getValues() {
        return values;
    }

    public void setValues(ArrayList<Integer> values) {
        this.values = values;
    }

}
