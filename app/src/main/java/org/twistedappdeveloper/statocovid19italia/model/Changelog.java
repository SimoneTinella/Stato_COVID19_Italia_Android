package org.twistedappdeveloper.statocovid19italia.model;

public class Changelog {
    private String versionaName, description;

    public Changelog(String versionaName, String description) {
        this.versionaName = versionaName;
        this.description = description;
    }

    public String getVersionaName() {
        return versionaName;
    }

    public String getDescription() {
        return description;
    }
}
