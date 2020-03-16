package org.twistedappdeveloper.statocovid19italia.utils;

import android.content.Context;
import android.graphics.Color;

import org.twistedappdeveloper.statocovid19italia.DataStorage.NationalDataStorage;
import org.twistedappdeveloper.statocovid19italia.R;

public class TrendUtils {

    public static int getColorByTrendKey(Context context, String key) {
        switch (key) {
            case NationalDataStorage.TOTALE_CASI_KEY:
                return context.getResources().getColor(R.color.orangeSecondary);
            case NationalDataStorage.C_NUOVI_POSITIVI:
                return context.getResources().getColor(R.color.orangeLight);
            case NationalDataStorage.TOTALE_ATTUALMENTE_POSITIVI_KEY:
                return context.getResources().getColor(R.color.brown);
            case NationalDataStorage.NUOVI_ATTUALMENTE_POSITIVI_KEY:
                return context.getResources().getColor(R.color.orange);

            case NationalDataStorage.C_NUOVI_DIMESSI_GUARITI:
                return Color.GREEN;
            case NationalDataStorage.TOTALE_DIMESSI_GUARITI_KEY:
                return context.getResources().getColor(R.color.green);

            case NationalDataStorage.C_NUOVI_DECEDUTI:
                return Color.RED;
            case NationalDataStorage.TOTALE_DECEDUTI_KEY:
                return context.getResources().getColor(R.color.brownDark);

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
            case NationalDataStorage.C_NUOVI_POSITIVI:
                return 0;
            case NationalDataStorage.C_NUOVI_DIMESSI_GUARITI:
                return 1;
            case NationalDataStorage.C_NUOVI_DECEDUTI:
                return 2;
            case NationalDataStorage.TOTALE_CASI_KEY:
                return 3;
            case NationalDataStorage.TOTALE_ATTUALMENTE_POSITIVI_KEY:
                return 4;
            case NationalDataStorage.TOTALE_DIMESSI_GUARITI_KEY:
                return 5;
            case NationalDataStorage.TOTALE_DECEDUTI_KEY:
                return 6;

            case NationalDataStorage.NUOVI_ATTUALMENTE_POSITIVI_KEY:
                return 7;
            case NationalDataStorage.TOTALE_OSPEDALIZZAZIONI_KEY:
                return 8;
            case NationalDataStorage.TERAPIA_INTENSIVA_KEY:
                return 9;
            case NationalDataStorage.RICOVERATI_SINTOMI_KEY:
                return 10;
            case NationalDataStorage.ISOLAMENTO_DOMICILIARE_KEY:
                return 11;
            case NationalDataStorage.TAMPONI_KEY:
                return 12;

            default:
                return Integer.MAX_VALUE;
        }
    }

    /**
     * Is used to override the name that can be retrieved using the words available in the key
     **/
    public static String getTrendNameByTrendKey(String key) {
        switch (key) {
            case NationalDataStorage.NUOVI_ATTUALMENTE_POSITIVI_KEY:
                return "Netto Nuovi Positivi";
            case NationalDataStorage.C_NUOVI_POSITIVI:
                return "Nuovi Positivi";
            case NationalDataStorage.TOTALE_DIMESSI_GUARITI_KEY:
                return "Totale Dimessi Guariti";
            case NationalDataStorage.TOTALE_DECEDUTI_KEY:
                return "Totale Deceduti";
            case NationalDataStorage.C_NUOVI_DIMESSI_GUARITI:
                return "Nuovi Dimessi Guariti";
            case NationalDataStorage.C_NUOVI_DECEDUTI:
                return "Nuovi Deceduti";

            default:
                String[] strings = key.split("_");
                String name = "";
                for (String string : strings) {
                    name = String.format(
                            "%s %s",
                            name,
                            String.format("%s%s", string.substring(0, 1).toUpperCase(), string.substring(1))
                    );
                }
                return name.trim();
        }
    }

}
