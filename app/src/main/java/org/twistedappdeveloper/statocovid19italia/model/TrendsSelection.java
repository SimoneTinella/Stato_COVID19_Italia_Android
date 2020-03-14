package org.twistedappdeveloper.statocovid19italia.model;

import org.twistedappdeveloper.statocovid19italia.utils.TrendUtils;

public class TrendsSelection implements Comparable<TrendsSelection> {
    private TrendInfo trendInfo;
    private boolean selected;

    public TrendsSelection(TrendInfo trendInfo, boolean selected) {
        this.trendInfo = trendInfo;
        this.selected = selected;
    }

    public TrendInfo getTrendInfo() {
        return trendInfo;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    @Override
    public int compareTo(TrendsSelection o) {
        return TrendUtils.getPositionByTrendKey(this.getTrendInfo().getKey()).compareTo(TrendUtils.getPositionByTrendKey(o.getTrendInfo().getKey()));
    }
}
