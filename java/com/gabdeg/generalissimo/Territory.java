package com.gabdeg.generalissimo;



public class Territory {

    public Territory(String id, String name) {
        setId(id);
        setName(name);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String toString() {
        return getId() + ", " + getName();
    }

    private String id;
    private String name;

}
