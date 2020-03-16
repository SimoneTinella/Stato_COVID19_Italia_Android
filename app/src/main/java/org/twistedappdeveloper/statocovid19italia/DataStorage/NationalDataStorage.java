package org.twistedappdeveloper.statocovid19italia.DataStorage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.twistedappdeveloper.statocovid19italia.model.TrendInfo;
import org.twistedappdeveloper.statocovid19italia.model.TrendValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.twistedappdeveloper.statocovid19italia.utils.TrendUtils.getTrendNameByTrendKey;

/***
 * Singleton Class used to share the data between activities
 * **/
public class NationalDataStorage {
    private static final String DATA_KEY = "data";
    public static final String TOTALE_DIMESSI_GUARITI_KEY = "dimessi_guariti";
    public static final String TOTALE_DECEDUTI_KEY = "deceduti";
    public static final String TOTALE_CASI_KEY = "totale_casi";
    public static final String TOTALE_ATTUALMENTE_POSITIVI_KEY = "totale_attualmente_positivi";
    public static final String NUOVI_ATTUALMENTE_POSITIVI_KEY = "nuovi_attualmente_positivi";
    public static final String TOTALE_OSPEDALIZZAZIONI_KEY = "totale_ospedalizzati";
    public static final String TERAPIA_INTENSIVA_KEY = "terapia_intensiva";
    public static final String RICOVERATI_SINTOMI_KEY = "ricoverati_con_sintomi";
    public static final String ISOLAMENTO_DOMICILIARE_KEY = "isolamento_domiciliare";
    public static final String TAMPONI_KEY = "tamponi";
    private static final String STATO_KEY = "stato";

    //Computed trends
    public static final String C_NUOVI_POSITIVI = "c_nuovi_positivi";
    public static final String C_NUOVI_DIMESSI_GUARITI= "c_nuovi_dimessi_guariti";
    public static final String C_NUOVI_DECEDUTI = "c_nuovi_deceduti";

    private final Set<String> discardedInfo = new HashSet<>();

    private static NationalDataStorage istance;

    private JSONArray datiNazionaliJson;

    private Map<String, TrendInfo> trendsMap;

    private NationalDataStorage() {
        trendsMap = new HashMap<>();
        discardedInfo.add(DATA_KEY);
        discardedInfo.add(STATO_KEY);
        discardedInfo.add(NUOVI_ATTUALMENTE_POSITIVI_KEY);
    }

    public static NationalDataStorage getIstance() {
        if (istance == null) {
            istance = new NationalDataStorage();
        }
        return istance;
    }

    public int getDatiNazionaliLength() {
        if (datiNazionaliJson != null) {
            return datiNazionaliJson.length();
        }
        return 0;
    }

    public String getDateByIndex(int index) {
        String date;
        try {
            date = datiNazionaliJson.getJSONObject(index).getString(DATA_KEY);
        } catch (JSONException e) {
            e.printStackTrace();
            date = "NoData";
        }
        return date;
    }

    public void setDatiNazionaliJson(JSONArray datiNazionaliJson) {
        this.datiNazionaliJson = datiNazionaliJson;

        try {
            JSONObject obj = datiNazionaliJson.getJSONObject(0);
            for (Iterator<String> keys = obj.keys(); keys.hasNext(); ) {
                String key = keys.next();

                if (discardedInfo.contains(key)) {
                    continue;
                }
                String name = getTrendNameByTrendKey(key);

                ArrayList<TrendValue> values = new ArrayList<>();
                for (int i = 0; i < datiNazionaliJson.length(); i++) {
                    JSONObject jsonObject = datiNazionaliJson.getJSONObject(i);
                    String date = jsonObject.getString(DATA_KEY);
                    Integer value = jsonObject.getInt(key);
                    values.add(new TrendValue(value, date));
                }
                trendsMap.put(key, new TrendInfo(name, key, values));
            }

            //Custom computed trends
            JSONObject jsonObjectIniziale = datiNazionaliJson.getJSONObject(0);

            ArrayList<TrendValue> nuoviPositivi = new ArrayList<>();
            ArrayList<TrendValue> nuoviGuariti = new ArrayList<>();
            ArrayList<TrendValue> nuoviDeceduti = new ArrayList<>();

            String dataIniziale= jsonObjectIniziale.getString(DATA_KEY);
            nuoviPositivi.add(new TrendValue(jsonObjectIniziale.getInt(TOTALE_ATTUALMENTE_POSITIVI_KEY), dataIniziale));
            nuoviGuariti.add(new TrendValue(jsonObjectIniziale.getInt(TOTALE_DIMESSI_GUARITI_KEY), dataIniziale));
            nuoviDeceduti.add(new TrendValue(jsonObjectIniziale.getInt(TOTALE_DECEDUTI_KEY), dataIniziale));

            for (int i = 1; i < datiNazionaliJson.length(); i++) {
                JSONObject jsonObjectCorrente = datiNazionaliJson.getJSONObject(i);
                JSONObject jsonObjectPrecedente = datiNazionaliJson.getJSONObject(i - 1);

                nuoviPositivi.add(computeDifferentialTrend(jsonObjectCorrente, jsonObjectPrecedente, TOTALE_CASI_KEY));
                nuoviGuariti.add(computeDifferentialTrend(jsonObjectCorrente, jsonObjectPrecedente, TOTALE_DIMESSI_GUARITI_KEY));
                nuoviDeceduti.add(computeDifferentialTrend(jsonObjectCorrente, jsonObjectPrecedente, TOTALE_DECEDUTI_KEY));
            }

            trendsMap.put(C_NUOVI_POSITIVI, new TrendInfo(getTrendNameByTrendKey(C_NUOVI_POSITIVI), C_NUOVI_POSITIVI, nuoviPositivi));
            trendsMap.put(C_NUOVI_DIMESSI_GUARITI, new TrendInfo(getTrendNameByTrendKey(C_NUOVI_DIMESSI_GUARITI), C_NUOVI_DIMESSI_GUARITI, nuoviGuariti));
            trendsMap.put(C_NUOVI_DECEDUTI, new TrendInfo(getTrendNameByTrendKey(C_NUOVI_DECEDUTI), C_NUOVI_DECEDUTI, nuoviDeceduti));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private TrendValue computeDifferentialTrend(JSONObject jsonObjectCorrente, JSONObject jsonObjectPrecedente, String key) throws JSONException {
        String date = jsonObjectCorrente.getString(DATA_KEY);
        Integer valoreCorrente= jsonObjectCorrente.getInt(key);
        Integer valorePrecendente = jsonObjectPrecedente.getInt(key);
        return new TrendValue(valoreCorrente - valorePrecendente, date);
    }

    public List<TrendInfo> getTrendsList() {
        return new ArrayList<>(trendsMap.values());
    }

    public TrendInfo getTrendByKey(String trendKey) {
        return trendsMap.get(trendKey);
    }

}
