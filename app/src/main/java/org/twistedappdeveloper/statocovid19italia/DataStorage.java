package org.twistedappdeveloper.statocovid19italia;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.twistedappdeveloper.statocovid19italia.model.TrendInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

class DataStorage {
    private static DataStorage istance;

    private JSONArray datiNazionaliJson;

    private Map<String, TrendInfo> trendsMap;

    private DataStorage() {
        trendsMap = new HashMap<>();
    }

    static DataStorage getIstance() {
        if (istance == null) {
            istance = new DataStorage();
        }
        return istance;
    }

    JSONArray getDatiNazionaliJson() {
        return datiNazionaliJson;
    }

    void setDatiNazionaliJson(JSONArray datiNazionaliJson) {
        this.datiNazionaliJson = datiNazionaliJson;

        try {
            JSONObject obj = getDatiNazionaliJson().getJSONObject(0);
            for (Iterator<String> keys = obj.keys(); keys.hasNext(); ) {
                String key = keys.next();

                if (key.equalsIgnoreCase("data") ||
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

                ArrayList<Integer> values = new ArrayList<>();
                for (int i = 0; i < getDatiNazionaliJson().length(); i++) {
                    values.add(getDatiNazionaliJson().getJSONObject(i).getInt(key));
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
