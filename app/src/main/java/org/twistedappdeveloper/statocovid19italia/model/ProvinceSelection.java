package org.twistedappdeveloper.statocovid19italia.model;

public class ProvinceSelection implements Comparable<ProvinceSelection> {
    private String provincia;
    private boolean selected;

    public ProvinceSelection(String provincia, boolean selected) {
        this.provincia = provincia;
        this.selected = selected;
    }

    public String getProvincia() {
        return provincia;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    @Override
    public int compareTo(ProvinceSelection o) {
        return this.provincia.compareTo(o.provincia);
    }
}
