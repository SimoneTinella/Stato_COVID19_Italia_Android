package org.twistedappdeveloper.statocovid19italia.model;

public class TrendValue {
    private int value, delta, precValue;
    private String date;
    private float deltaPercentage;

    public TrendValue(Integer value, String date, float deltaPercentage, int delta, int precValue) {
        this.value = value;
        this.date = date;
        this.deltaPercentage = deltaPercentage;
        this.delta = delta;
        this.precValue = precValue;
    }

    public TrendValue(Integer value, String date) {
        this.value = value;
        this.date = date;
        this.deltaPercentage = 0f;
        this.delta = 0;
        this.precValue = 0;
    }

    public int getValue() {
        return value;
    }

    public String getDate() {
        return date;
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
}
