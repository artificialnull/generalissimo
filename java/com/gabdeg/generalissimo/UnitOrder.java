package com.gabdeg.generalissimo;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class UnitOrder {
    private String orderID;
    private String unitID;
    private String type;
    private String toTerrID;
    private String fromTerrID;
    private String viaConvoy;

    public String getFormalUnitTitle() {
        return formalUnitTitle;
    }

    public void setFormalUnitTitle(String formalUnitTitle) {
        this.formalUnitTitle = formalUnitTitle;
    }

    private String formalUnitTitle;

    public Map<String, String> allToTerrID = new HashMap<>();
    public Map<String, String> allFromTerrID = new HashMap<>();
    public Map<String, String> allType = new HashMap<>();

    public String getOrderID() {
        return orderID;
    }

    public void setOrderID(String orderID) {
        this.orderID = orderID;
    }

    public String getUnitID() {
        return unitID;
    }

    public void setUnitID(String unitID) {
        this.unitID = unitID;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getToTerrID() {
        return toTerrID;
    }

    public void setToTerrID(String toTerrID) {
        this.toTerrID = toTerrID;
    }

    public String getFromTerrID() {
        return fromTerrID;
    }

    public void setFromTerrID(String fromTerrID) {
        this.fromTerrID = fromTerrID;
    }

    public String getViaConvoy() {
        return viaConvoy;
    }

    public void setViaConvoy(String viaConvoy) {
        this.viaConvoy = viaConvoy;
    }

    public void loadFromJSONObject(JSONObject unitOrderJSON) {
        try {
            this.orderID    = unitOrderJSON.getString("id");
            this.unitID     = unitOrderJSON.getString("unitID");
            this.type       = unitOrderJSON.getString("type");
            this.toTerrID   = unitOrderJSON.getString("toTerrID");
            this.fromTerrID = unitOrderJSON.getString("fromTerrID");
            this.viaConvoy  = unitOrderJSON.getString("viaConvoy");
        } catch (Exception err) {
            err.printStackTrace();
        }
    }
}
