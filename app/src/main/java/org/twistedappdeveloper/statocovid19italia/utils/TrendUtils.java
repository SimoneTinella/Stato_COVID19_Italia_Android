package org.twistedappdeveloper.statocovid19italia.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;

import org.twistedappdeveloper.statocovid19italia.datastorage.DataStorage;
import org.twistedappdeveloper.statocovid19italia.R;

public class TrendUtils {

    public static int getColorByTrendKey(Context context, String key) {
        switch (key) {
            case DataStorage.TOTALE_CASI_KEY:
                return context.getResources().getColor(R.color.orangeSecondary);
            case DataStorage.C_NUOVI_POSITIVI:
                return context.getResources().getColor(R.color.orangeLight);
            case DataStorage.TOTALE_ATTUALMENTE_POSITIVI_KEY:
                return context.getResources().getColor(R.color.brown);
            case DataStorage.NUOVI_ATTUALMENTE_POSITIVI_KEY:
                return context.getResources().getColor(R.color.orange);

            case DataStorage.C_NUOVI_DIMESSI_GUARITI:
                return Color.GREEN;
            case DataStorage.TOTALE_DIMESSI_GUARITI_KEY:
                return context.getResources().getColor(R.color.green);

            case DataStorage.C_NUOVI_DECEDUTI:
                return Color.RED;
            case DataStorage.TOTALE_DECEDUTI_KEY:
                return context.getResources().getColor(R.color.brownDark);

            case DataStorage.TOTALE_OSPEDALIZZAZIONI_KEY:
                return context.getResources().getColor(R.color.blue);
            case DataStorage.TERAPIA_INTENSIVA_KEY:
                return context.getResources().getColor(R.color.blueDark);
            case DataStorage.RICOVERATI_SINTOMI_KEY:
                return context.getResources().getColor(R.color.blueLight);
            case DataStorage.ISOLAMENTO_DOMICILIARE_KEY:
                return context.getResources().getColor(R.color.violet);

            case DataStorage.TAMPONI_KEY:
            default:
                return Color.BLACK;
        }
    }

    public static Integer getPositionByTrendKey(String key) {
        switch (key) {
            case DataStorage.C_NUOVI_POSITIVI:
                return 1;
            case DataStorage.C_NUOVI_DIMESSI_GUARITI:
                return 2;
            case DataStorage.C_NUOVI_DECEDUTI:
                return 3;
            case DataStorage.TOTALE_CASI_KEY:
                return 4;
            case DataStorage.TOTALE_ATTUALMENTE_POSITIVI_KEY:
                return 0;
            case DataStorage.TOTALE_DIMESSI_GUARITI_KEY:
                return 5;
            case DataStorage.TOTALE_DECEDUTI_KEY:
                return 6;

//            case DataStorage.NUOVI_ATTUALMENTE_POSITIVI_KEY:
//                return 7;
            case DataStorage.TOTALE_OSPEDALIZZAZIONI_KEY:
                return 7;
            case DataStorage.TERAPIA_INTENSIVA_KEY:
                return 8;
            case DataStorage.RICOVERATI_SINTOMI_KEY:
                return 9;
            case DataStorage.ISOLAMENTO_DOMICILIARE_KEY:
                return 10;
            case DataStorage.TAMPONI_KEY:
                return 11;

            default:
                return Integer.MAX_VALUE;
        }
    }

    /**
     * Is used to override the name that can be retrieved using the words available in the key (default case)
     **/
    public static String getTrendNameByTrendKey(Resources resources, String key) {
        switch (key) {
            case DataStorage.TOTALE_CASI_KEY:
                return resources.getString(R.string.totale_casi_name);
            case DataStorage.C_NUOVI_POSITIVI:
                return resources.getString(R.string.c_nuovi_positivi_name);
            case DataStorage.TOTALE_ATTUALMENTE_POSITIVI_KEY:
                return resources.getString(R.string.totale_attualmente_positivi_name);
            case DataStorage.NUOVI_ATTUALMENTE_POSITIVI_KEY:
                return resources.getString(R.string.nuovi_attualmente_positivi_name);
            case DataStorage.C_NUOVI_DIMESSI_GUARITI:
                return resources.getString(R.string.c_nuovi_dimessi_guariti_name);
            case DataStorage.TOTALE_DIMESSI_GUARITI_KEY:
                return resources.getString(R.string.dimessi_guariti_name);
            case DataStorage.C_NUOVI_DECEDUTI:
                return resources.getString(R.string.c_nuovi_deceduti_name);
            case DataStorage.TOTALE_DECEDUTI_KEY:
                return resources.getString(R.string.deceduti_name);
            case DataStorage.TOTALE_OSPEDALIZZAZIONI_KEY:
                return resources.getString(R.string.totale_ospedalizzati_name);
            case DataStorage.TERAPIA_INTENSIVA_KEY:
                return resources.getString(R.string.terapia_intensiva_name);
            case DataStorage.RICOVERATI_SINTOMI_KEY:
                return resources.getString(R.string.ricoverati_con_sintomi_name);
            case DataStorage.ISOLAMENTO_DOMICILIARE_KEY:
                return resources.getString(R.string.isolamento_domiciliare_name);
            case DataStorage.TAMPONI_KEY:
                return resources.getString(R.string.tamponi_name);

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

    public static String getTrendDescriptionByTrendKey(Context context, String key) {
        switch (key) {
            case DataStorage.TOTALE_CASI_KEY:
                return context.getResources().getString(R.string.totale_casi_desc);
            case DataStorage.C_NUOVI_POSITIVI:
                return context.getResources().getString(R.string.c_nuovi_positivi_desc);
            case DataStorage.TOTALE_ATTUALMENTE_POSITIVI_KEY:
                return context.getResources().getString(R.string.totale_attualmente_positivi_desc);
            case DataStorage.NUOVI_ATTUALMENTE_POSITIVI_KEY:
                return context.getResources().getString(R.string.nuovi_attualmente_positivi_desc);
            case DataStorage.C_NUOVI_DIMESSI_GUARITI:
                return context.getResources().getString(R.string.c_nuovi_dimessi_guariti_desc);
            case DataStorage.TOTALE_DIMESSI_GUARITI_KEY:
                return context.getResources().getString(R.string.dimessi_guariti_desc);
            case DataStorage.C_NUOVI_DECEDUTI:
                return context.getResources().getString(R.string.c_nuovi_deceduti_desc);
            case DataStorage.TOTALE_DECEDUTI_KEY:
                return context.getResources().getString(R.string.deceduti_desc);
            case DataStorage.TOTALE_OSPEDALIZZAZIONI_KEY:
                return context.getResources().getString(R.string.totale_ospedalizzati_desc);
            case DataStorage.TERAPIA_INTENSIVA_KEY:
                return context.getResources().getString(R.string.terapia_intensiva_desc);
            case DataStorage.RICOVERATI_SINTOMI_KEY:
                return context.getResources().getString(R.string.ricoverati_con_sintomi_desc);
            case DataStorage.ISOLAMENTO_DOMICILIARE_KEY:
                return context.getResources().getString(R.string.isolamento_domiciliare_desc);
            case DataStorage.TAMPONI_KEY:
                return context.getResources().getString(R.string.tamponi_desc);

            default:
                return "Nessuna Descrizione Disponibile";
        }
    }

}
