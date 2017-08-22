package com.gabdeg.generalissimo;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;

public class Order {

    public class Unit {

        public Territory getTerritory() {
            return territory;
        }

        public void setTerritory(Territory territory) {
            this.territory = territory;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public Unit(String type, Territory territory, String id) {
            this.setType(type);
            this.setTerritory(territory);
            this.setId(id);
        }

        private String id;
        private Territory territory;
        private String type;

    }

    public class Territory {

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

        public Territory(String id, String name) {
            this.setId(id);
            this.setName(name);
        }

        private String id;
        private String name;
    }

    public class Choice {

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

        public String getPrefix() {
            return prefix;
        }

        public void setPrefix(String prefix) {
            this.prefix = prefix;
        }

        public ArrayList<Choice> getResults() {
            return results;
        }

        public ArrayList<String> getResultNames() {
            ArrayList<String> resultNames = new ArrayList<>();
            for (Choice result : results) {
                resultNames.add(result.getName());
            }
            return resultNames;
        }

        public Choice getResultFromName(String name) {
            for (Choice choice : getResults()) {
                if (choice.getName().equals(name)) {
                    return choice;
                }
            }
            return null;
        }

        public Choice getResultFromID(String id) {
            for (Choice choice : getResults()) {
                if (choice.getId().equals(id)) {
                    return choice;
                }
            }
            return null;
        }

        public void setResults(ArrayList<Choice> results) {
            this.results = results;
        }

        public void addToResults(Choice result) {
            this.results.add(result);
        }

        public Choice loadChoiceFromJSON(JSONObject choiceJSON, String choiceID) {

            try {
                Choice choice = new Choice();
                choice.setId(choiceID);
                choice.setName(choiceJSON.getString("name"));
                Iterator<String> results = choiceJSON.getJSONObject("results").keys();
                choice.setPrefix(choiceJSON.getString("prefix"));
                while (results.hasNext()) {
                    String key = results.next();
                    choice.addToResults(
                            loadChoiceFromJSON(
                                    choiceJSON.getJSONObject("results").getJSONObject(key),
                                    key
                            )
                    );
                }
                return choice;
            } catch (Exception err) {
                //err.printStackTrace();
            }
            return null;
        }

        public String toString() {
            String complete = "";
            String incomplete = "";
            complete += this.getId() + " - " + this.getName() + " : ";
            for (Choice result : this.getResults()) {
                BufferedReader bufReader = new BufferedReader(new StringReader(result.toString()));
                String line = null;
                try {
                    while ((line = bufReader.readLine()) != null) {
                        incomplete += "  " + line + "\n";
                    }
                } catch (Exception err) {
                    err.printStackTrace();
                }
            }
            return complete + "\n" + incomplete;
        }

        private String id;
        private String name;
        private String prefix;
        private ArrayList<Choice> results;

        public Choice() {
            this.id = "";
            this.name = "";
            this.results = new ArrayList<>();
        }

        public Choice(String id, String name) {
            this.id = id;
            this.name = name;
            this.results = new ArrayList<>();
        }

        public Choice(String id, String name, ArrayList<Choice> results) {
            this.id = id;
            this.name = name;
            this.results = results;
        }
    }

    public Order() {
        this.choices = new ArrayList<>();
    }

    public ArrayList<Choice> getChoices() {
        return choices;
    }

    public ArrayList<String> getChoiceNames() {
        // this probably shouldn't actually be used ever
        ArrayList<String> nameList = new ArrayList<>();
        for (Choice choice : getChoices()) {
            nameList.add(choice.getName());
        }
        return nameList;
    }

    public Choice getChoiceFromName(String name) {
        for (Choice choice : getChoices()) {
            if (choice.getName().equals(name)) {
                return choice;
            }
        }
        return null;
    }

    public Choice getChoiceFromID(String id) {
        for (Choice choice : getChoices()) {
            if (choice.getId().equals(id)) {
                return choice;
            }
        }
        return null;
    }

    public void setChoices(ArrayList<Choice> choices) {
        this.choices = choices;
    }

    public void addToChoices(Choice choice) {
        this.choices.add(choice);
    }

    public Unit getOrderUnit() {
        return orderUnit;
    }

    public void setOrderUnit(Unit orderUnit) {
        this.orderUnit = orderUnit;
    }

    public String getOrderPrefix() {
        if (getOrderUnit() != null) {
            return "The " + getOrderUnit().type.toLowerCase()
                    + " at " + getOrderUnit().territory.getName();
        } else {
            return "Build/Destroy";
        }
    }

    private Unit orderUnit;
    private ArrayList<Choice> choices;

    public String toString() {
        String complete = "";
        for (Choice choice : this.getChoices()) {
            complete += choice.toString() + "\n";
        }
        return complete;
    }

    public JSONObject toJSONObject() {
        JSONObject toReturn = new JSONObject();

        try {
            toReturn.put("id", this.getId());
            if (this.getOrderUnit() != null) {
                toReturn.put("unitID", this.getUnitID());
            } else {
                toReturn.put("unitID", JSONObject.NULL);
            }

            toReturn.put("type", this.getSelectedType().getId());
            if (this.getSelectedToTerr() != null) {
                toReturn.put("toTerrID", this.getSelectedToTerr().getId());
            } else {
                toReturn.put("toTerrID", "");
            }
            if (this.getSelectedFromTerr() != null) {
                toReturn.put("fromTerrID", this.getSelectedFromTerr().getId());
            } else {
                toReturn.put("fromTerrID", "");
            }
            if (this.getSelectedViaConvoy() != null) {
                toReturn.put("viaConvoy", this.getSelectedViaConvoy().getId());
                if (this.getSelectedViaConvoy().getId().equals("Yes")) {
                    if (!this.getSelectedViaConvoy().getPrefix().equals("")) {
                        try {
                            toReturn.put("convoyPath", new JSONArray(
                                    "[" + this.getSelectedToTerr().getResultFromID("Yes").getPrefix() + "]"
                            ));
                        } catch (Exception err) {
                            err.printStackTrace();
                        }
                    }
                }
            } else {
                if (this.getSelectedToTerr() != null) {
                    if (this.getSelectedToTerr().getName().contains("convoy")) {
                        toReturn.put("viaConvoy", "Yes");
                        if (!this.getSelectedToTerr().getResultFromID("Yes").getPrefix().equals("")) {
                            try {
                                toReturn.put("convoyPath", new JSONArray(
                                        "[" + this.getSelectedToTerr().getResultFromID("Yes").getPrefix() + "]"
                                ));
                            } catch (Exception err) {
                                err.printStackTrace();
                            }
                        }
                    } else {
                        toReturn.put("viaConvoy", "");
                        if (this.getSelectedType().getId().equals("Convoy") || this.getSelectedType().getId().equals("Support move")) {
                            if (this.getSelectedFromTerr().getResults().size() > 0) {
                                if (!this.getSelectedFromTerr().getResultFromID("Yes").getPrefix().equals("")) {
                                    try {
                                        toReturn.put("convoyPath", new JSONArray(
                                                "[" + this.getSelectedFromTerr().getResultFromID("Yes").getPrefix() + "]"
                                        ));
                                    } catch (Exception err) {
                                        err.printStackTrace();
                                    }
                                }
                            }
                        } else if (this.getSelectedType().getId().equals("Move")) {
                            toReturn.put("viaConvoy", "No");
                        }
                    }
                } else {
                    toReturn.put("viaConvoy", "");
                }
            }
            //Log.v("ORDER_TO_JSON", toReturn.toString(2));
        } catch (Exception err) {
            err.printStackTrace();
        }
        return toReturn;
    }

    public String toJSONString() {
        return this.toJSONObject().toString();
    }

    public Choice getSelectedType() {
        return selectedType;
    }

    public void setSelectedType(Choice selectedType) {
        this.selectedType = selectedType;
    }

    public Choice getSelectedToTerr() {
        return selectedToTerr;
    }

    public void setSelectedToTerr(Choice selectedToTerr) {
        this.selectedToTerr = selectedToTerr;
    }

    public Choice getSelectedFromTerr() {
        return selectedFromTerr;
    }

    public void setSelectedFromTerr(Choice selectedFromTerr) {
        this.selectedFromTerr = selectedFromTerr;
    }

    public Choice getSelectedViaConvoy() {
        return selectedViaConvoy;
    }

    public void setSelectedViaConvoy(Choice selectedViaConvoy) {
        this.selectedViaConvoy = selectedViaConvoy;
    }

    private Choice selectedType;
    private Choice selectedToTerr;
    private Choice selectedFromTerr;
    private Choice selectedViaConvoy;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUnitID() {
        return getOrderUnit().getId();
    }

    private String id;

    public void loadFromJSONObject(JSONObject orderJSON) {
        try {

            JSONObject orderInfo = new JSONObject();

            Iterator choices = orderJSON.keys();
            while (choices.hasNext()) {
                String key = (String) choices.next();
                if (key.equals("UnitInfo")) {
                    JSONObject unitInfo = orderJSON.getJSONObject("UnitInfo");
                    this.setOrderUnit(
                            new Unit(
                                    unitInfo.getString("type"),
                                    new Territory(
                                            unitInfo.getJSONObject("terr").getString("id"),
                                            unitInfo.getJSONObject("terr").getString("name")
                                    ),
                                    unitInfo.getString("id")
                            )
                    );
                } else if (key.equals("OrderInfo")) {
                    orderInfo = orderJSON.getJSONObject("OrderInfo");
                } else {
                    JSONObject choiceJSON = orderJSON.getJSONObject(key);
                    this.addToChoices(
                            new Choice().loadChoiceFromJSON(choiceJSON, key)
                    );
                }
            }

            this.setSelectedType(
                    this.getChoiceFromID(orderInfo.getString("type"))
            );

            this.setId(
                    orderInfo.getString("id")
            );


            if (this.getSelectedType().getResults().size() > 0) {
                this.setSelectedToTerr(
                        this.getSelectedType().getResultFromID(orderInfo.getString("toTerrID"))
                );

                if (this.getSelectedToTerr().getResults().size() > 0) {
                    if (orderInfo.getString("fromTerrID").equals("")) {
                        this.setSelectedViaConvoy(
                                this.getSelectedToTerr().getResultFromID(orderInfo.getString("viaConvoy"))
                        );

                    } else {
                        this.setSelectedFromTerr(
                                this.getSelectedToTerr().getResultFromID(orderInfo.getString("fromTerrID"))
                        );
                    }
                }
            }


        } catch (Exception err) {
            err.printStackTrace();
        }
    }


}
