package org.twistedappdeveloper.statocovid19italia.model;

public class Avviso {
    private String tipoAvviso, avviso, note, regione, provincia;

    public Avviso(String tipoAvviso, String avviso, String note, String regione, String provincia) {
        this.tipoAvviso = tipoAvviso;
        this.avviso = avviso;
        this.note = note;
        this.regione = regione;
        this.provincia= provincia;
    }

    public String getTipoAvviso() {
        return tipoAvviso;
    }

    public String getAvviso() {
        return avviso;
    }

    public String getNote() {
        return note;
    }

    public String getRegione() {
        return regione;
    }

    public String getProvincia() {
        return provincia;
    }
}
