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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    private String orderStatus;


    private String name;
    private String color;
    private String units;
    private String cps;
    private String id;

    public String toString() {
        return getName() + ":\n  "
                + getId() + "\n  "
                + getCps() + "\n  "
                + getUnits() + "\n  "
                + getColor();
    }

}
