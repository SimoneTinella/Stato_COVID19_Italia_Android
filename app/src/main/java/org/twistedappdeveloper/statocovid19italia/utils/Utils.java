package org.twistedappdeveloper.statocovid19italia.utils;

import com.github.mikephil.charting.data.BarEntry;

import java.util.List;

public class Utils {
    public static final String CURSORE_KEY = "cursore";
    public static final String DATACONTEXT_KEY = "dataContext";
    public static final String TREND_KEY = "trendKey";
    public static final String PROVINCE_ARRAY_KEY = "provincie";


    static int compare(BarEntry u1, BarEntry u2) {
        // assume null checks as required
        return Float.valueOf(u1.getY()).compareTo(u2.getY());
    }

    static int partition(List<BarEntry> users, int lo, int hi)
    {
        int i = lo, j = hi;
        int pivotIdx = lo + (hi-lo)/2;
        BarEntry pivot = users.get(pivotIdx);
        BarEntry tmp;

        while (i <= j) {
            while (compare(users.get(i),pivot) < 0)	i++;
            while (compare(users.get(j),pivot) > 0)	j--;
            if (i <= j) {
                tmp = users.get(i);
                users.get(j).setX(i);;
                tmp.setX(j);
                users.set(i++, users.get(j));
                users.set(j--, tmp);
            }
        }
        return i;
    }

    public static void quickSort(List<BarEntry> users, int lo, int hi) {
        int index = partition(users, lo, hi);
        if (lo < index - 1)
            quickSort(users, lo, index - 1);
        if (index < hi)
            quickSort(users, index, hi);
    }


}
