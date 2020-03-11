package org.twistedappdeveloper.statocovid19italia;

import org.json.JSONArray;

class DataStorage {
    private static DataStorage istance;

    private JSONArray datiNazionaliJson;

    private DataStorage() {
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
    }
}
