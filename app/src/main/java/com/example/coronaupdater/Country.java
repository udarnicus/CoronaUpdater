package com.example.coronaupdater;

public class Country {
    public int index;
    public String name;
    private String tote = "-1";
    private String infizierte = "-1";

    public Country(final String name, final int index){
        this.index = index;
        this.name = name;
    }


    public String getTote() {
        return tote;
    }

    public void setTote(String tote) {
        this.tote = tote;
    }

    public String getInfizierte() {
        return infizierte;
    }

    public void setInfizierte(String infizierte) {
        this.infizierte = infizierte;
    }
}
