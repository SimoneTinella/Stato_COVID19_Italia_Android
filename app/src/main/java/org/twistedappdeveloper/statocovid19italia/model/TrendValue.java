package org.twistedappdeveloper.statocovid19italia.model;

public class TrendValue {
    private double value, delta, precValue, deltaPercentage;
    private String date;

    public TrendValue(double value, String date, double deltaPercentage, double delta, double precValue) {
        this.value = value;
        this.date = date;
        this.deltaPercentage = deltaPercentage;
        this.delta = delta;
        this.precValue = precValue;
    }

    public TrendValue(double value, String date) {
        this.value = value;
        this.date = date;
        this.deltaPercentage = 0f;
        this.delta = 0;
        this.precValue = 0;
    }

    public double getValue() {
        return value;
    }

    public String getDate() {
        return date;
    }

    public double getDeltaPercentage() {
        return deltaPercentage;
    }

    public double getDelta() {
        return delta;
    }

    public double getPrecValue() {
        return precValue;
    }
}
