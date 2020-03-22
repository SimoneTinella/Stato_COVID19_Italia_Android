package org.twistedappdeveloper.statocovid19italia.model;

import java.util.List;

public class ProvinceSelectionWrapper implements Comparable<ProvinceSelectionWrapper> {
    private String regione;
    private List<ProvinceSelection> provinceSelectionList;

    public ProvinceSelectionWrapper(String regione, List<ProvinceSelection> provinceSelectionList) {
        this.regione = regione;
        this.provinceSelectionList = provinceSelectionList;
    }

    public String getRegione() {
        return regione;
    }

    public List<ProvinceSelection> getProvinceSelectionList() {
        return provinceSelectionList;
    }

    @Override
    public int compareTo(ProvinceSelectionWrapper o) {
        return this.regione.compareTo(o.regione);
    }
}
