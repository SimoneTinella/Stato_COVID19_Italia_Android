package org.twistedappdeveloper.statocovid19italia.utils;

import android.content.Context;
import android.graphics.Color;

import org.twistedappdeveloper.statocovid19italia.DataStorage.NationalDataStorage;
import org.twistedappdeveloper.statocovid19italia.R;

public class TrendUtils {

    public static int getColorByTrendKey(Context context, String key) {
        switch (key) {
            case NationalDataStorage.DECEDUTI_KEY:
                return Color.RED;
            case NationalDataStorage.TOTALE_CASI_KEY:
                return context.getResources().getColor(R.color.orange);
            case NationalDataStorage.GUARITI_KEY:
                return Color.GREEN;
            case NationalDataStorage.TOTALE_ATTUALMENTE_POSITIVI_KEY:
                return context.getResources().getColor(R.color.orangeLight);
            case NationalDataStorage.NUOVI_POSITIVI_KEY:
                return context.getResources().getColor(R.color.orangeDark);

            case NationalDataStorage.TOTALE_OSPEDALIZZAZIONI_KEY:
                return context.getResources().getColor(R.color.blue);
            case NationalDataStorage.TERAPIA_INTENSIVA_KEY:
                return context.getResources().getColor(R.color.blueDark);
            case NationalDataStorage.RICOVERATI_SINTOMI_KEY:
                return context.getResources().getColor(R.color.blueLight);
            case NationalDataStorage.ISOLAMENTO_DOMICILIARE_KEY:
                return context.getResources().getColor(R.color.violet);

            case NationalDataStorage.TAMPONI_KEY:
            default:
                return Color.BLACK;
        }
    }

    public static Integer getPositionByTrendKey(String key) {
        switch (key) {
            case NationalDataStorage.TOTALE_CASI_KEY:
                return 0;
            case NationalDataStorage.GUARITI_KEY:
                return 1;
            case NationalDataStorage.DECEDUTI_KEY:
                return 2;
            case NationalDataStorage.TOTALE_ATTUALMENTE_POSITIVI_KEY:
                return 3;
            case NationalDataStorage.NUOVI_POSITIVI_KEY:
                return 4;
            case NationalDataStorage.TOTALE_OSPEDALIZZAZIONI_KEY:
                return 5;
            case NationalDataStorage.TERAPIA_INTENSIVA_KEY:
                return 6;
            case NationalDataStorage.RICOVERATI_SINTOMI_KEY:
                return 7;
            case NationalDataStorage.ISOLAMENTO_DOMICILIARE_KEY:
                return 8;
            case NationalDataStorage.TAMPONI_KEY:
                return 9;

            default:
                return Integer.MAX_VALUE;
        }
    }

}
