package org.twistedappdeveloper.statocovid19italia.utils;

import android.content.Context;
import android.graphics.Color;

import org.twistedappdeveloper.statocovid19italia.R;

public class TrendUtils {

    public static int getColorByTrendKey(Context context, String key) {
        switch (key) {
            case "deceduti":
                return Color.RED;
            case "totale_casi":
                return context.getResources().getColor(R.color.orange);
            case "dimessi_guariti":
                return Color.GREEN;
            case "totale_attualmente_positivi":
                return context.getResources().getColor(R.color.orangeLight);
            case "nuovi_attualmente_positivi":
                return context.getResources().getColor(R.color.orangeDark);

            case "totale_ospedalizzati":
                return context.getResources().getColor(R.color.blue);
            case "terapia_intensiva":
                return context.getResources().getColor(R.color.blueDark);
            case "ricoverati_con_sintomi":
                return context.getResources().getColor(R.color.blueLight);
            case "isolamento_domiciliare":
                return context.getResources().getColor(R.color.violet);

            case "tamponi":
            default:
                return Color.BLACK;
        }
    }

    public static Integer getPositionByTrendKey(String key) {
        switch (key) {
            case "totale_casi":
                return 0;
            case "dimessi_guariti":
                return 1;
            case "deceduti":
                return 2;
            case "totale_attualmente_positivi":
                return 3;
            case "nuovi_attualmente_positivi":
                return 4;
            case "totale_ospedalizzati":
                return 5;
            case "terapia_intensiva":
                return 6;
            case "ricoverati_con_sintomi":
                return 7;
            case "isolamento_domiciliare":
                return 8;
            case "tamponi":
                return 9;

            default:
                return Integer.MAX_VALUE;
        }
    }

}
