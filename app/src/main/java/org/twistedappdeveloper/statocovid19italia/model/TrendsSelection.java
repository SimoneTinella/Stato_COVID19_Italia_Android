package org.twistedappdeveloper.statocovid19italia.model;

import androidx.annotation.NonNull;
import org.twistedappdeveloper.statocovid19italia.utils.TrendUtils;

public class TrendsSelection implements Cloneable, Comparable<TrendsSelection> {
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

    public void setTrendInfo(TrendInfo trendInfo) {
        this.trendInfo = trendInfo;
    }

    @NonNull
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
