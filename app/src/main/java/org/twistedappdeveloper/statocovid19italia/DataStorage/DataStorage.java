package org.twistedappdeveloper.statocovid19italia.DataStorage;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.twistedappdeveloper.statocovid19italia.model.TrendInfo;
import org.twistedappdeveloper.statocovid19italia.model.TrendValue;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
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
public class DataStorage {
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

    private static final String DEN_REGIONE_KEY = "denominazione_regione";
    private static final String COD_REGIONE_KEY = "codice_regione";
    private static final String LAT_REGIONE_KEY = "lat";
    private static final String LONG_REGIONE_KEY = "long";

    //Computed trends
    public static final String C_NUOVI_POSITIVI = "c_nuovi_positivi";
    public static final String C_NUOVI_DIMESSI_GUARITI = "c_nuovi_dimessi_guariti";
    public static final String C_NUOVI_DECEDUTI = "c_nuovi_deceduti";

    private final Set<String> discardedInfo = new HashSet<>();

    private static DataStorage istance;

    private JSONArray mainDataJson;

    private Map<String, TrendInfo> mainTrendsMap;
    private Map<String, DataStorage> secondaryDataStorageMap;

    public String getContestoDati() {
        return contestoDati;
    }

    private String contestoDati;

    private final DateFormat dateFormatRead = new SimpleDateFormat("yyyy-MM-dd");
    private final DateFormat dateFormatWriteSimple = new SimpleDateFormat("dd/MM");
    private final DateFormat dateFormatWriteFull = new SimpleDateFormat("dd/MM/yyyy");

    private DataStorage(String contestoDati) {
        this.contestoDati = contestoDati;
        mainTrendsMap = new HashMap<>();
        secondaryDataStorageMap = new HashMap<>();
        discardedInfo.add(DATA_KEY);
        discardedInfo.add(STATO_KEY);
//        discardedInfo.add(NUOVI_ATTUALMENTE_POSITIVI_KEY);
        discardedInfo.add(COD_REGIONE_KEY);
        discardedInfo.add(DEN_REGIONE_KEY);
        discardedInfo.add(LAT_REGIONE_KEY);
        discardedInfo.add(LONG_REGIONE_KEY);
    }

    public static DataStorage getIstance() {
        if (istance == null) {
            istance = new DataStorage("Nazionale");
        }
        return istance;
    }

    /***
     * Se è il DataStorage relativo ai dati nazionali restituisce la lungheza dei dati nazionali altrimenti della singola regione
     * ***/
    public int getMainDataLength() {
        if (mainDataJson != null) {
            return mainDataJson.length();
        }
        return 0;
    }

    public String getFullDateByIndex(int index) {
        String date;
        try {
            date = getFullDateFromJSONObject(mainDataJson.getJSONObject(index));
        } catch (JSONException e) {
            e.printStackTrace();
            date = "NoData";
        }
        return date;
    }

    public String getSimpleDateByIndex(int index) {
        String date;
        try {
            date = getSimpleDateFromJSONObject(mainDataJson.getJSONObject(index));
        } catch (JSONException e) {
            e.printStackTrace();
            date = "NoData";
        }
        return date;
    }

    public void setMainDataJson(JSONArray mainDataJson) {
        this.mainDataJson = mainDataJson;
        mainTrendsMap.clear();
        try {
            JSONObject obj = mainDataJson.getJSONObject(0);
            for (Iterator<String> keys = obj.keys(); keys.hasNext(); ) {
                String key = keys.next();

                if (discardedInfo.contains(key)) {
                    continue;
                }

                //Escludo eventuali nuovi campi aggiunti nel json non rappresentanti segnali
                try {
                    Integer.parseInt(mainDataJson.getJSONObject(0).getString(key));
                } catch (NumberFormatException e) {
                    continue;
                }

                String name = getTrendNameByTrendKey(key);
                ArrayList<TrendValue> values = new ArrayList<>();
                for (int i = 0; i < mainDataJson.length(); i++) {
                    JSONObject jsonObject = mainDataJson.getJSONObject(i);
                    String date = getFullDateFromJSONObject(jsonObject);
                    Integer value = jsonObject.getInt(key);
                    values.add(new TrendValue(value, date));
                }
                mainTrendsMap.put(key, new TrendInfo(name, key, values));
            }

            //Custom computed trends
            JSONObject jsonObjectIniziale = mainDataJson.getJSONObject(0);

            ArrayList<TrendValue> nuoviPositivi = new ArrayList<>();
            ArrayList<TrendValue> nuoviGuariti = new ArrayList<>();
            ArrayList<TrendValue> nuoviDeceduti = new ArrayList<>();

            String dataIniziale = getFullDateFromJSONObject(jsonObjectIniziale);
            nuoviPositivi.add(new TrendValue(jsonObjectIniziale.getInt(TOTALE_ATTUALMENTE_POSITIVI_KEY), dataIniziale));
            nuoviGuariti.add(new TrendValue(jsonObjectIniziale.getInt(TOTALE_DIMESSI_GUARITI_KEY), dataIniziale));
            nuoviDeceduti.add(new TrendValue(jsonObjectIniziale.getInt(TOTALE_DECEDUTI_KEY), dataIniziale));

            for (int i = 1; i < mainDataJson.length(); i++) {
                JSONObject jsonObjectCorrente = mainDataJson.getJSONObject(i);
                JSONObject jsonObjectPrecedente = mainDataJson.getJSONObject(i - 1);

                nuoviPositivi.add(computeDifferentialTrend(jsonObjectCorrente, jsonObjectPrecedente, TOTALE_CASI_KEY));
                nuoviGuariti.add(computeDifferentialTrend(jsonObjectCorrente, jsonObjectPrecedente, TOTALE_DIMESSI_GUARITI_KEY));
                nuoviDeceduti.add(computeDifferentialTrend(jsonObjectCorrente, jsonObjectPrecedente, TOTALE_DECEDUTI_KEY));
            }

            mainTrendsMap.put(C_NUOVI_POSITIVI, new TrendInfo(getTrendNameByTrendKey(C_NUOVI_POSITIVI), C_NUOVI_POSITIVI, nuoviPositivi));
            mainTrendsMap.put(C_NUOVI_DIMESSI_GUARITI, new TrendInfo(getTrendNameByTrendKey(C_NUOVI_DIMESSI_GUARITI), C_NUOVI_DIMESSI_GUARITI, nuoviGuariti));
            mainTrendsMap.put(C_NUOVI_DECEDUTI, new TrendInfo(getTrendNameByTrendKey(C_NUOVI_DECEDUTI), C_NUOVI_DECEDUTI, nuoviDeceduti));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("DataStorage", String.format("%s dati principali settati", contestoDati));
    }

    public void setSecondaryDataJson(JSONArray secondaryDataJson) {
        secondaryDataStorageMap.clear();

        Map<String, JSONArray> regionalJsonArrayMap = new HashMap<>();
        try {
            for (int i = 0; i < secondaryDataJson.length(); i++) {
                JSONObject jsonObject = secondaryDataJson.getJSONObject(i);

                String regione = jsonObject.getString(DEN_REGIONE_KEY);
                JSONArray regionalJSONArray;
                if (regionalJsonArrayMap.containsKey(regione)) {
                    regionalJSONArray = regionalJsonArrayMap.get(regione);
                } else {
                    regionalJSONArray = new JSONArray();
                    regionalJsonArrayMap.put(regione, regionalJSONArray);
                }
                regionalJSONArray.put(jsonObject);
            }

            for (String regione : regionalJsonArrayMap.keySet()) {
                JSONArray regionalJSONArray = regionalJsonArrayMap.get(regione);
                DataStorage regionalDataStorage = new DataStorage(regione);
                regionalDataStorage.setMainDataJson(regionalJSONArray);
                secondaryDataStorageMap.put(regione, regionalDataStorage);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("DataStorage", String.format("%s dati secondari settati", contestoDati));
    }

    private TrendValue computeDifferentialTrend(JSONObject jsonObjectCorrente, JSONObject jsonObjectPrecedente, String key) throws JSONException {
        String date = getFullDateFromJSONObject(jsonObjectCorrente);
        Integer valoreCorrente = jsonObjectCorrente.getInt(key);
        Integer valorePrecendente = jsonObjectPrecedente.getInt(key);
        return new TrendValue(valoreCorrente - valorePrecendente, date);
    }

    public List<TrendInfo> getMainTrendsList() {
        return new ArrayList<>(mainTrendsMap.values());
    }

    public TrendInfo getTrendByKey(String trendKey) {
        return mainTrendsMap.get(trendKey);
    }

    public DataStorage getRegionalDataStorageByDenRegione(String regione) {
        return secondaryDataStorageMap.get(regione);
    }

    public List<String> getSecondaryKeys() {
        List<String> keys = new ArrayList<>(secondaryDataStorageMap.keySet());
        Collections.sort(keys);
        return keys;
    }

    public DataStorage getDataStorageByContestoDati(String contestoDati) {
        if (contestoDati.equalsIgnoreCase(this.contestoDati)) {
            return this;
        } else {
            return getRegionalDataStorageByDenRegione(contestoDati);
        }
    }


    private String getFullDateFromJSONObject(JSONObject jsonObject) throws JSONException {
        try {
            Date date=dateFormatRead.parse(jsonObject.getString(DATA_KEY));
            return dateFormatWriteFull.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return "NoData";
        }
    }

    private String getSimpleDateFromJSONObject(JSONObject jsonObject) throws JSONException {
        try {
            Date date=dateFormatRead.parse(jsonObject.getString(DATA_KEY));
            return dateFormatWriteSimple.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return "NoData";
        }
    }
}
