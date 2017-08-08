package com.gabdeg.generalissimo;

import java.io.Serializable;


public class Nation implements Serializable {

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getUnits() {
        return units;
    }

    public void setUnits(String units) {
        this.units = units;
    }

    public String getCps() {
        return cps;
    }

    public void setCps(String cps) {
        this.cps = cps;
    }

    private String name;
    private String color;
    private String units;
    private String cps;

}
