package org.twistedappdeveloper.statocovid19italia.datastorage;

import android.content.res.Resources;
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
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static org.twistedappdeveloper.statocovid19italia.utils.TrendUtils.getTrendNameByTrendKey;

/***
 * Singleton Class used to share the data between activities
 * **/
public class DataStorage {

    //National and Regional trend keys
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

    //Computed trends keys
    public static final String C_NUOVI_POSITIVI = "c_nuovi_positivi";
    public static final String C_NUOVI_DIMESSI_GUARITI = "c_nuovi_dimessi_guariti";
    public static final String C_NUOVI_DECEDUTI = "c_nuovi_deceduti";

    //National, Regional and Provincial no trend keys
    private static final String DATA_KEY = "data";
    private static final String STATO_KEY = "stato";
    public static final String DEN_REGIONE_KEY = "denominazione_regione";
    public static final String DEN_PROVINCIA_KEY = "denominazione_provincia";
    private static final String COD_REGIONE_KEY = "codice_regione";
    private static final String LAT_REGIONE_KEY = "lat";
    private static final String LONG_REGIONE_KEY = "long";
    private static final String COD_PROVINCIA_KEY = "codice_provincia";
    private static final String SIGLA_PROVINCIA_KEY = "sigla_provincia";


    private final Set<String> discardedInfo = new HashSet<>();

    private static DataStorage istance;

    private JSONArray dataArrayJson;

    private Map<String, TrendInfo> trendsMap;

    private Map<String, DataStorage> subLevelDataStorageMap;

    private String dataContext;
    private Scope dataContextScope;

    private final DateFormat dateFormatRead = new SimpleDateFormat("yyyy-MM-dd", Locale.ITALY);
    private final DateFormat dateFormatWriteSimple = new SimpleDateFormat("dd/MM", Locale.ITALY);
    private final DateFormat dateFormatWriteFull = new SimpleDateFormat("dd/MM/yyyy", Locale.ITALY);

    private Resources resources;

    public enum Scope {
        NAZIONALE,
        REGIONALE,
        PROVINCIALE
    }

    private DataStorage(String dataContext, Resources resources, Scope dataContextScope) {
        this.dataContext = dataContext;
        this.resources = resources;
        this.dataContextScope = dataContextScope;
        trendsMap = new HashMap<>();
        subLevelDataStorageMap = new HashMap<>();
        discardedInfo.add(DATA_KEY);
        discardedInfo.add(STATO_KEY);
        discardedInfo.add(COD_REGIONE_KEY);
        discardedInfo.add(DEN_REGIONE_KEY);
        discardedInfo.add(LAT_REGIONE_KEY);
        discardedInfo.add(LONG_REGIONE_KEY);
//        discardedInfo.add(NUOVI_ATTUALMENTE_POSITIVI_KEY);
        discardedInfo.add(SIGLA_PROVINCIA_KEY);
        discardedInfo.add(COD_PROVINCIA_KEY);

    }

    public static final String defaultDataContext = "Nazionale";

    public static DataStorage createAndGetIstanceIfNotExist(Resources resources, Scope dataContextScope) {
        if (istance == null) {
            istance = new DataStorage(defaultDataContext, resources, dataContextScope);
        }
        return istance;
    }

    public static DataStorage getIstance() {
        return istance;
    }

    /*** Indica che dati contiente il DataStorage. Es Nazionale oppure Lombardia etc. ***/
    public String getDataContext() {
        return dataContext;
    }

    /***
     * Se è il DataStorage relativo ai dati nazionali restituisce la lungheza dei dati nazionali altrimenti della singola regione
     * ***/
    public int getDataLength() {
        if (dataArrayJson != null) {
            return dataArrayJson.length();
        }
        return 0;
    }

    public String getFullDateByIndex(int index) {
        String date;
        try {
            date = getFullDateFromJSONObject(dataArrayJson.getJSONObject(index));
        } catch (JSONException e) {
            e.printStackTrace();
            date = "NoData";
        }
        return date;
    }

    public String getSimpleDateByIndex(int index) {
        String date;
        try {
            date = getSimpleDateFromJSONObject(dataArrayJson.getJSONObject(index));
        } catch (JSONException e) {
            e.printStackTrace();
            date = "NoData";
        }
        return date;
    }

    public void setDataArrayJson(JSONArray dataArrayJson) {
        this.dataArrayJson = dataArrayJson;
        trendsMap.clear();
        try {
            JSONObject obj = dataArrayJson.getJSONObject(0);
            for (Iterator<String> keys = obj.keys(); keys.hasNext(); ) {
                String key = keys.next();

                if (discardedInfo.contains(key)) {
                    continue;
                }

                //Escludo eventuali nuovi campi aggiunti nel json non rappresentanti segnali
                try {
                    Integer.parseInt(dataArrayJson.getJSONObject(0).getString(key));
                } catch (NumberFormatException e) {
                    Log.d("AndroTag", String.format("Valori della chiave %s scartati", key));
                    continue;
                }

                String name = getTrendNameByTrendKey(resources, key);
                ArrayList<TrendValue> values = new ArrayList<>();
                for (int i = 0; i < dataArrayJson.length(); i++) {
                    JSONObject jsonObject = dataArrayJson.getJSONObject(i);
                    String date = getFullDateFromJSONObject(jsonObject);
                    Integer value = jsonObject.getInt(key);
                    values.add(new TrendValue(value, date));
                }
                trendsMap.put(key, new TrendInfo(name, key, values));
            }

            //Custom computed trends
            if (this.dataContextScope == Scope.NAZIONALE || this.dataContextScope == Scope.REGIONALE) {
                JSONObject jsonObjectIniziale = dataArrayJson.getJSONObject(0);

                ArrayList<TrendValue> nuoviPositivi = new ArrayList<>();
                ArrayList<TrendValue> nuoviGuariti = new ArrayList<>();
                ArrayList<TrendValue> nuoviDeceduti = new ArrayList<>();

                String dataIniziale = getFullDateFromJSONObject(jsonObjectIniziale);
                nuoviPositivi.add(new TrendValue(jsonObjectIniziale.getInt(TOTALE_ATTUALMENTE_POSITIVI_KEY), dataIniziale));
                nuoviGuariti.add(new TrendValue(jsonObjectIniziale.getInt(TOTALE_DIMESSI_GUARITI_KEY), dataIniziale));
                nuoviDeceduti.add(new TrendValue(jsonObjectIniziale.getInt(TOTALE_DECEDUTI_KEY), dataIniziale));

                for (int i = 1; i < dataArrayJson.length(); i++) {
                    JSONObject jsonObjectCorrente = dataArrayJson.getJSONObject(i);
                    JSONObject jsonObjectPrecedente = dataArrayJson.getJSONObject(i - 1);

                    nuoviPositivi.add(computeDifferentialTrend(jsonObjectCorrente, jsonObjectPrecedente, TOTALE_CASI_KEY));
                    nuoviGuariti.add(computeDifferentialTrend(jsonObjectCorrente, jsonObjectPrecedente, TOTALE_DIMESSI_GUARITI_KEY));
                    nuoviDeceduti.add(computeDifferentialTrend(jsonObjectCorrente, jsonObjectPrecedente, TOTALE_DECEDUTI_KEY));
                }

                trendsMap.put(C_NUOVI_POSITIVI, new TrendInfo(getTrendNameByTrendKey(resources, C_NUOVI_POSITIVI), C_NUOVI_POSITIVI, nuoviPositivi));
                trendsMap.put(C_NUOVI_DIMESSI_GUARITI, new TrendInfo(getTrendNameByTrendKey(resources, C_NUOVI_DIMESSI_GUARITI), C_NUOVI_DIMESSI_GUARITI, nuoviGuariti));
                trendsMap.put(C_NUOVI_DECEDUTI, new TrendInfo(getTrendNameByTrendKey(resources, C_NUOVI_DECEDUTI), C_NUOVI_DECEDUTI, nuoviDeceduti));
            }else{
                JSONObject jsonObjectIniziale = dataArrayJson.getJSONObject(0);
                ArrayList<TrendValue> nuoviPositivi = new ArrayList<>();
                String dataIniziale = getFullDateFromJSONObject(jsonObjectIniziale);
                nuoviPositivi.add(new TrendValue(jsonObjectIniziale.getInt(TOTALE_CASI_KEY), dataIniziale));
                for (int i = 1; i < dataArrayJson.length(); i++) {
                    JSONObject jsonObjectCorrente = dataArrayJson.getJSONObject(i);
                    JSONObject jsonObjectPrecedente = dataArrayJson.getJSONObject(i - 1);
                    nuoviPositivi.add(computeDifferentialTrend(jsonObjectCorrente, jsonObjectPrecedente, TOTALE_CASI_KEY));
                }
                trendsMap.put(C_NUOVI_POSITIVI, new TrendInfo(getTrendNameByTrendKey(resources, C_NUOVI_POSITIVI), C_NUOVI_POSITIVI, nuoviPositivi));
            }
        } catch (JSONException e) {
            Log.e("AndroTagError", e.getMessage());
            e.printStackTrace();
        }
        Log.d("DataStorage", String.format("%s dati principali settati", dataContext));
    }

    /*** Usato per settare nel DataStorage Nazionale i dati relativi alle regioni **/
    public void setSubLvlDataArrayJson(JSONArray subLevelDataJSONArray, String key, Scope dataContextScope) {
        this.subLevelDataStorageMap.clear();

        Map<String, JSONArray> regionalJsonArrayMap = new HashMap<>();
        try {
            for (int i = 0; i < subLevelDataJSONArray.length(); i++) {
                JSONObject jsonObject = subLevelDataJSONArray.getJSONObject(i);

                String regione = jsonObject.getString(key);
                JSONArray regionalJSONArray;
                if (regionalJsonArrayMap.containsKey(regione)) {
                    regionalJSONArray = regionalJsonArrayMap.get(regione);
                } else {
                    regionalJSONArray = new JSONArray();
                    regionalJsonArrayMap.put(regione, regionalJSONArray);
                }
                regionalJSONArray.put(jsonObject);
            }

            for (String dataContext : regionalJsonArrayMap.keySet()) {
                JSONArray regionalJSONArray = regionalJsonArrayMap.get(dataContext);
                DataStorage regionalDataStorage = new DataStorage(dataContext, resources, dataContextScope);
                regionalDataStorage.setDataArrayJson(regionalJSONArray);
                subLevelDataStorageMap.put(dataContext, regionalDataStorage);
            }
        } catch (JSONException e) {
            Log.e("AndroTagError", e.getMessage());
            e.printStackTrace();
        }
        Log.d("DataStorage", String.format("%s dati secondari settati", dataContext));
    }

    private TrendValue computeDifferentialTrend(JSONObject jsonObjectCorrente, JSONObject jsonObjectPrecedente, String key) throws JSONException {
        String date = getFullDateFromJSONObject(jsonObjectCorrente);
        Integer valoreCorrente = jsonObjectCorrente.getInt(key);
        Integer valorePrecendente = jsonObjectPrecedente.getInt(key);
        return new TrendValue(valoreCorrente - valorePrecendente, date);
    }

    public List<TrendInfo> getTrendsList() {
        return new ArrayList<>(trendsMap.values());
    }

    public TrendInfo getTrendByKey(String trendKey) {
        return trendsMap.get(trendKey);
    }


    public List<String> getSubLevelDataKeys() {
        List<String> keys = new ArrayList<>(subLevelDataStorageMap.keySet());
        Collections.sort(keys);
        return keys;
    }

    public DataStorage getDataStorageByDataContext(String dataContext) {
        if (dataContext.equalsIgnoreCase(this.dataContext)) {
            return this;
        } else {
            return subLevelDataStorageMap.get(dataContext);
        }
    }


    private String getFullDateFromJSONObject(JSONObject jsonObject) throws JSONException {
        try {
            Date date = dateFormatRead.parse(jsonObject.getString(DATA_KEY));
            return dateFormatWriteFull.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return "NoData";
        }
    }

    private String getSimpleDateFromJSONObject(JSONObject jsonObject) throws JSONException {
        try {
            Date date = dateFormatRead.parse(jsonObject.getString(DATA_KEY));
            return dateFormatWriteSimple.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return "NoData";
        }
    }

    public Scope getDataContextScope() {
        return dataContextScope;
    }
}
