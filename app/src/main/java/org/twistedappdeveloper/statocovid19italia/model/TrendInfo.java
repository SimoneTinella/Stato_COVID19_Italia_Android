package org.twistedappdeveloper.statocovid19italia.model;

import java.util.ArrayList;

/*** This class is used to store trend information and values
 * ***/
public class TrendInfo {
    private String name, key;
    private ArrayList<TrendValue> values;

    public TrendInfo(String name, String key, ArrayList<TrendValue> values) {
        this.name = name;
        this.key = key;
        this.values = values;
    }

    public String getName() {
        return name;
    }

    public String getKey() {
        return key;
    }

    public ArrayList<TrendValue> getTrendValues() {
        return values;
    }

    public TrendValue getTrendValueByIndex(int index) {
        return values.get(index);
    }

}
