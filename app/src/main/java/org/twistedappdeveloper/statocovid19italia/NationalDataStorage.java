package org.twistedappdeveloper.statocovid19italia;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.twistedappdeveloper.statocovid19italia.model.TrendInfo;
import org.twistedappdeveloper.statocovid19italia.model.TrendValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/***
 * Singleton Class used to share the data between activities
 * **/
public class NationalDataStorage {
    private static final String DATA_KEY= "data";
    public static final String GUARITI_KEY= "dimessi_guariti";
    public static final String DECEDUTI_KEY= "deceduti";
    public static final String TOTALE_CASI_KEY= "totale_casi";
    public static final String TOTALE_ATTUALMENTE_POSITIVI_KEY= "totale_attualmente_positivi";
    public static final String NUOVI_POSITIVI_KEY= "nuovi_attualmente_positivi";
    public static final String TOTALE_OSPEDALIZZAZIONI_KEY= "totale_ospedalizzazioni";
    public static final String TERAPIA_INTENSIVA_KEY= "terapia_intensiva";
    public static final String RICOVERATI_SINTOMI_KEY= "ricoverati_con_sintomi";
    public static final String ISOLAMENTO_DOMICILIARE_KEY= "isolamento_domiciliare";
    public static final String TAMPONI_KEY= "tamponi";

    private static NationalDataStorage istance;

    private JSONArray datiNazionaliJson;

    private Map<String, TrendInfo> trendsMap;

    private NationalDataStorage() {
        trendsMap = new HashMap<>();
    }

    static NationalDataStorage getIstance() {
        if (istance == null) {
            istance = new NationalDataStorage();
        }
        return istance;
    }

    int getDatiNazionaliLength(){
        return datiNazionaliJson.length();
    }

    String getDateByIndex(int index) {
        String date;
        try {
            date = datiNazionaliJson.getJSONObject(index).getString(DATA_KEY);
        } catch (JSONException e) {
            e.printStackTrace();
            date = "NoData";
        }
        return date;
    }

    void setDatiNazionaliJson(JSONArray datiNazionaliJson) {
        this.datiNazionaliJson = datiNazionaliJson;

        try {
            JSONObject obj = datiNazionaliJson.getJSONObject(0);
            for (Iterator<String> keys = obj.keys(); keys.hasNext(); ) {
                String key = keys.next();

                if (key.equalsIgnoreCase(DATA_KEY) ||
                        key.equalsIgnoreCase("stato")
                ) {
                    continue;
                }
                String[] strings = key.split("_");
                String name = "";
                for (String string : strings) {
                    name = String.format(
                            "%s %s",
                            name,
                            String.format("%s%s", string.substring(0, 1).toUpperCase(), string.substring(1))
                    );
                }

                ArrayList<TrendValue> values = new ArrayList<>();
                for (int i = 0; i < datiNazionaliJson.length(); i++) {
                    JSONObject jsonObject = datiNazionaliJson.getJSONObject(i);
                    String date = jsonObject.getString(DATA_KEY);
                    Integer value = jsonObject.getInt(key);
                    values.add(new TrendValue(value, date));
                }
                trendsMap.put(key, new TrendInfo(name, key, values));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    List<TrendInfo> getTrendsList() {
        return new ArrayList<>(trendsMap.values());
    }

    TrendInfo getTrendByKey(String trendKey) {
        return trendsMap.get(trendKey);
    }

}
