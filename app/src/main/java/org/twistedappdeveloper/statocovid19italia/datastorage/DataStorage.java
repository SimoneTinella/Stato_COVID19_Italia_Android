package org.twistedappdeveloper.statocovid19italia.datastorage;

import android.content.res.Resources;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.twistedappdeveloper.statocovid19italia.model.Avviso;
import org.twistedappdeveloper.statocovid19italia.model.TrendInfo;
import org.twistedappdeveloper.statocovid19italia.model.TrendValue;
import org.twistedappdeveloper.statocovid19italia.utils.TrendUtils;

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
    public static final String TOTALE_ATTUALMENTE_POSITIVI_KEY = "totale_positivi";
    public static final String NUOVI_ATTUALMENTE_POSITIVI_KEY = "variazione_totale_positivi";
    public static final String TOTALE_OSPEDALIZZAZIONI_KEY = "totale_ospedalizzati";
    public static final String TERAPIA_INTENSIVA_KEY = "terapia_intensiva";
    public static final String RICOVERATI_SINTOMI_KEY = "ricoverati_con_sintomi";
    public static final String ISOLAMENTO_DOMICILIARE_KEY = "isolamento_domiciliare";
    public static final String TAMPONI_KEY = "tamponi";
    public static final String NUOVI_POSITIVI_KEY = "nuovi_positivi";
    public static final String CASI_TESTATI_KEY = "casi_testati";
    public static final String CASI_DA_SOSPETTO_DIAGNOSTICO = "casi_da_sospetto_diagnostico";
    public static final String CASI_DA_SCREENING = "casi_da_screening";
    public static final String INGRESSI_TERAPIA_INT = "ingressi_terapia_intensiva";

    //Computed trends keys
    public static final String C_NUOVI_DIMESSI_GUARITI = "c_nuovi_dimessi_guariti";
    public static final String C_NUOVI_DECEDUTI = "c_nuovi_deceduti";
    public static final String C_NUOVI_TAMPONI = "c_nuovi_tamponi";
    public static final String C_RAPPORTO_POSITIVI_TAMPONI = "c_rapporto_positivi_tamponi";

    //National, Regional and Provincial no trend keys
    public static final String DATA_KEY = "data";
    private static final String STATO_KEY = "stato";
    public static final String DEN_REGIONE_KEY = "denominazione_regione";
    public static final String DEN_PROVINCIA_KEY = "denominazione_provincia";
    private static final String COD_REGIONE_KEY = "codice_regione";
    private static final String LAT_REGIONE_KEY = "lat";
    private static final String LONG_REGIONE_KEY = "long";
    private static final String COD_PROVINCIA_KEY = "codice_provincia";
    private static final String SIGLA_PROVINCIA_KEY = "sigla_provincia";
    private static final String NOTE_IT_KEY = "note";
    private static final String NOTE_EN_KEY = "note_en";
    private static final String NOTE_CASI_KEY = "note_casi";
    private static final String NOTE_TEST_KEY = "note_test";

    //AVVISI
    private static final String TIPO_AVVISO_KEY = "tipologia_avviso";
    private static final String AVVISO_KEY = "avviso";
    private static final String NOTE_KEY = "note";
    private static final String COD_AVVISO_KEY = "codice";
    private static final String REGIONE_KEY = "regione";
    private static final String PROVINCIA_KEY = "provincia";


    private final Set<String> discardedInfo = new HashSet<>();

    private static DataStorage istance;

    private JSONArray dataArrayJson;

    private Map<String, TrendInfo> trendsMap;

    private Map<String, DataStorage> subLevelDataStorageMap;

    private Map<String, Avviso> mappaAvvisi = new HashMap<>(); //popolato solo nel datastorage nazionale

    private Map<String, List<Avviso>> avvisiRelativiMap = new HashMap<>(); //avvisi relativi al proprio dataset

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

    public static final String defaultDataContext = "Nazionale";

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
        discardedInfo.add(SIGLA_PROVINCIA_KEY);
        discardedInfo.add(COD_PROVINCIA_KEY);
        discardedInfo.add(NOTE_IT_KEY);
        discardedInfo.add(NOTE_EN_KEY);
        discardedInfo.add(NOTE_CASI_KEY);
        discardedInfo.add(NOTE_TEST_KEY);
    }

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

    public String getFullDateStringByIndex(int index) {
        String date;
        try {
            date = getFullDateStringFromJSONObject(dataArrayJson.getJSONObject(index));
        } catch (JSONException e) {
            e.printStackTrace();
            date = "NoData";
        }
        return date;
    }

    public String getSimpleDateStringByIndex(int index) {
        String date;
        try {
            date = getSimpleDateStringFromJSONObject(dataArrayJson.getJSONObject(index));
        } catch (JSONException e) {
            e.printStackTrace();
            date = "NoData";
        }
        return date;
    }

    private Avviso getAvvisoByKey(String key) {
        return getIstance().mappaAvvisi.get(key);
    }

    public List<Avviso> getAvvisiRelativoByDate(String date) {
        return avvisiRelativiMap.get(date);
    }

    public void setDataArrayJson(JSONArray dataArrayJson) {
        this.dataArrayJson = dataArrayJson;
        trendsMap.clear();
        try {
            JSONObject obj = dataArrayJson.getJSONObject(0);
            JSONObject tmpObj = obj;
            for (Iterator<String> keys = obj.keys(); keys.hasNext(); ) {
                String key = keys.next();

                if (discardedInfo.contains(key)) {
                    continue;
                }

                //Escludo eventuali nuovi campi aggiunti nel json non rappresentanti segnali
                try {
                    String value = dataArrayJson.getJSONObject(0).getString(key);
                    if (!value.equals("null")) {
                        Double.parseDouble(value);
                    }
                } catch (NumberFormatException e) {
                    Log.d("AndroTag", String.format("Valori della chiave %s scartati", key));
                    continue;
                }

                String name = getTrendNameByTrendKey(resources, key);
                ArrayList<TrendValue> values = new ArrayList<>();
                for (int i = 0; i < dataArrayJson.length(); i++) {
                    JSONObject jsonObject = dataArrayJson.getJSONObject(i);
                    String date = getFullDateStringFromJSONObject(jsonObject);
                    String valueString = jsonObject.getString(key);
                    double value;
                    if (valueString.equals("null")) {
                        value = 0d;
                    } else {
                        value = jsonObject.getDouble(key);
                    }
                    double precValue;
                    String precValueString = tmpObj.getString(key);
                    if (precValueString.equals("null")) {
                        precValue = 0d;
                    } else {
                        precValue = tmpObj.getDouble(key);
                    }
                    double delta = value - precValue;
                    double percentage = delta / (precValue == 0 ? 1 : precValue);
                    values.add(new TrendValue(value, date, percentage, delta, precValue));
                    tmpObj = jsonObject;
                }
                tmpObj = obj;
                trendsMap.put(key, new TrendInfo(name, key, values));
            }

            for (int i = 0; i < dataArrayJson.length(); i++) {
                JSONObject jsonObject = dataArrayJson.getJSONObject(i);
                String data = getFullDateStringByIndex(i);
                String noteKey = jsonObject.getString(NOTE_IT_KEY);
                if (!noteKey.isEmpty()) {
                    String[] split = noteKey.split(";");
                    List<Avviso> avvisi;
                    if (avvisiRelativiMap.get(data) == null) {
                        avvisi = new ArrayList<>();
                        avvisiRelativiMap.put(data, avvisi);
                    } else {
                        avvisi = avvisiRelativiMap.get(data);
                    }
                    for (String nota : split) {
                        Avviso avviso = getAvvisoByKey(nota);
                        if (avviso != null) {
                            avvisi.add(getAvvisoByKey(nota));
                        }
                    }

                }

            }

            //Custom computed trends
            if (this.dataContextScope == Scope.NAZIONALE || this.dataContextScope == Scope.REGIONALE) {
                JSONObject jsonObjectIniziale = dataArrayJson.getJSONObject(0);

                ArrayList<TrendValue> nuoviGuariti = new ArrayList<>();
                ArrayList<TrendValue> nuoviDeceduti = new ArrayList<>();
                ArrayList<TrendValue> nuoviTamponi = new ArrayList<>();
                ArrayList<TrendValue> rapportoPositiviTamponi = new ArrayList<>();

                String dataIniziale = getFullDateStringFromJSONObject(jsonObjectIniziale);
                nuoviGuariti.add(new TrendValue(jsonObjectIniziale.getDouble(TOTALE_DIMESSI_GUARITI_KEY), dataIniziale));
                nuoviDeceduti.add(new TrendValue(jsonObjectIniziale.getDouble(TOTALE_DECEDUTI_KEY), dataIniziale));
                nuoviTamponi.add(new TrendValue(jsonObjectIniziale.getDouble(TAMPONI_KEY), dataIniziale));
                rapportoPositiviTamponi.add(new TrendValue((jsonObjectIniziale.getDouble(NUOVI_POSITIVI_KEY) / jsonObjectIniziale.getDouble(TAMPONI_KEY)) * 100, dataIniziale));

                for (int i = 1; i < dataArrayJson.length(); i++) {
                    nuoviGuariti.add(computeDifferentialTrend(i, i - 1, TOTALE_DIMESSI_GUARITI_KEY));
                    nuoviDeceduti.add(computeDifferentialTrend(i, i - 1, TOTALE_DECEDUTI_KEY));
                    nuoviTamponi.add(computeDifferentialTrend(i, i - 1, TAMPONI_KEY));

                    rapportoPositiviTamponi.add(new TrendValue((trendsMap.get(NUOVI_POSITIVI_KEY).getTrendValueByIndex(i).getValue() / nuoviTamponi.get(i).getValue()) * 100, dataIniziale));
                }

                trendsMap.put(C_NUOVI_DIMESSI_GUARITI, new TrendInfo(getTrendNameByTrendKey(resources, C_NUOVI_DIMESSI_GUARITI), C_NUOVI_DIMESSI_GUARITI, nuoviGuariti));
                trendsMap.put(C_NUOVI_DECEDUTI, new TrendInfo(getTrendNameByTrendKey(resources, C_NUOVI_DECEDUTI), C_NUOVI_DECEDUTI, nuoviDeceduti));
                trendsMap.put(C_NUOVI_TAMPONI, new TrendInfo(getTrendNameByTrendKey(resources, C_NUOVI_TAMPONI), C_NUOVI_TAMPONI, nuoviTamponi));
                trendsMap.put(C_RAPPORTO_POSITIVI_TAMPONI, new TrendInfo(getTrendNameByTrendKey(resources, C_RAPPORTO_POSITIVI_TAMPONI), C_RAPPORTO_POSITIVI_TAMPONI, rapportoPositiviTamponi));
            } else {
                JSONObject jsonObjectIniziale = dataArrayJson.getJSONObject(0);
                ArrayList<TrendValue> nuoviPositivi = new ArrayList<>();
                String dataIniziale = getFullDateStringFromJSONObject(jsonObjectIniziale);
                nuoviPositivi.add(new TrendValue(jsonObjectIniziale.getDouble(TOTALE_CASI_KEY), dataIniziale));
                for (int i = 1; i < dataArrayJson.length(); i++) {
                    nuoviPositivi.add(computeDifferentialTrend(i, i - 1, TOTALE_CASI_KEY));
                }
                trendsMap.put(NUOVI_POSITIVI_KEY, new TrendInfo(getTrendNameByTrendKey(resources, NUOVI_POSITIVI_KEY), NUOVI_POSITIVI_KEY, nuoviPositivi));
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
                regione = TrendUtils.getFixedProvinciaDen(regione);

                JSONArray regionalJSONArray;
                if (regionalJsonArrayMap.containsKey(regione)) {
                    regionalJSONArray = regionalJsonArrayMap.get(regione);
                } else {
                    regionalJSONArray = new JSONArray();
                    regionalJsonArrayMap.put(regione, regionalJSONArray);
                }
                // unisco i valori nel caso di province unite. Es In fase di definizione o Fuori Regione etc..
                boolean found = false;
                for (int j = 0; j < regionalJSONArray.length(); j++) {
                    JSONObject jsonObjectTmp = regionalJSONArray.getJSONObject(j);
                    if (jsonObjectTmp.get(DataStorage.DATA_KEY).equals(jsonObject.get(DataStorage.DATA_KEY))) {
                        jsonObjectTmp.put(DataStorage.TOTALE_CASI_KEY, jsonObjectTmp.getInt(DataStorage.TOTALE_CASI_KEY) + jsonObject.getInt(DataStorage.TOTALE_CASI_KEY));
                        found = true;
                    }
                }
                if (!found) {
                    regionalJSONArray.put(jsonObject);
                }
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

    private TrendValue computeDifferentialTrend(int currentIndex, int precIndex, String key) throws JSONException {
        JSONObject jsonObjectCorrente = this.dataArrayJson.getJSONObject(currentIndex);
        JSONObject jsonObjectPrecedente = this.dataArrayJson.getJSONObject(precIndex);

        double valoreCorrente = jsonObjectCorrente.getDouble(key);
        double valorePrecendente = jsonObjectPrecedente.getDouble(key);
        double deltaPrecedente = 0;
        double valorePrecedenteAncora;

        if (precIndex > 0) {
            JSONObject jsonObjectPrecedenteAncora = this.dataArrayJson.getJSONObject(precIndex - 1);
            valorePrecedenteAncora = jsonObjectPrecedenteAncora.getDouble(key);
            deltaPrecedente = valorePrecendente - valorePrecedenteAncora;
        }

        String date = getFullDateStringFromJSONObject(jsonObjectCorrente);
        double delta = valoreCorrente - valorePrecendente;

        double percentage = (delta - deltaPrecedente) / (deltaPrecedente == 0 ? 1 : deltaPrecedente);

        return new TrendValue(delta, date, percentage, delta - deltaPrecedente, deltaPrecedente);
    }

    private TrendValue computeRapportTrend(int index, String pKey, String qKey) throws JSONException {
        JSONObject jsonObject = this.dataArrayJson.getJSONObject(index);

        double valoreP = jsonObject.getDouble(pKey);
        double valoreQ = jsonObject.getDouble(qKey);

        String date = getFullDateStringFromJSONObject(jsonObject);

        return new TrendValue((valoreP / valoreQ) * 100, date);
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

    private String getFullDateStringFromJSONObject(JSONObject jsonObject) throws JSONException {
        try {
            Date date = dateFormatRead.parse(jsonObject.getString(DATA_KEY));
            return dateFormatWriteFull.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return "NoData";
        }
    }

    private String getSimpleDateStringFromJSONObject(JSONObject jsonObject) throws JSONException {
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

    public void setAvvisiDataArrayJson(JSONArray avvisiDataArrayJson) {
        mappaAvvisi.clear();
        avvisiRelativiMap.clear();
        for (int i = 0; i < avvisiDataArrayJson.length(); i++) {
            try {
                JSONObject jsonObject = avvisiDataArrayJson.getJSONObject(i);
                String cod = jsonObject.getString(COD_AVVISO_KEY);
                Avviso avviso = new Avviso(
                        jsonObject.getString(TIPO_AVVISO_KEY),
                        jsonObject.getString(AVVISO_KEY),
                        jsonObject.getString(NOTE_KEY),
                        jsonObject.getString(REGIONE_KEY),
                        jsonObject.getString(PROVINCIA_KEY)
                );
                mappaAvvisi.put(cod, avviso);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    public Date getDateByIndex(int index) {
        try {
            return dateFormatRead.parse(dataArrayJson.getJSONObject(index).getString(DATA_KEY));
        } catch (ParseException | JSONException e) {
            e.printStackTrace();
            return new Date();
        }
    }

    public int getIndexByDate(Date date) {
        for (int i = 0; i < dataArrayJson.length(); i++) {
            if (getDateByIndex(i).equals(date)) {
                return i;
            }
        }
        return getDataLength() - 1;
    }
}
