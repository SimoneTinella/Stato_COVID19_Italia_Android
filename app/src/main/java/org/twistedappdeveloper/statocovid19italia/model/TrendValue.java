package org.twistedappdeveloper.statocovid19italia.model;

public class TrendValue {
    private Integer value;
    private String date;

    public TrendValue(Integer value, String date) {
        this.value = value;
        this.date = date;
    }

    public Integer getValue() {
        return value;
    }

    public String getDate() {
        return date;
    }
}
