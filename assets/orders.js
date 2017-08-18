SimpleOrders = [];
if (typeof MyOrders === "undefined") {
    MyOrders = [];
}
for (var i = 0; i < MyOrders.length; i++) {
    order = MyOrders[i];
    simpleOrder = {};
    typeChoices = order.updateTypeChoices();
    if (typeof typeChoices._object !== "undefined") {
        typeChoices = typeChoices._object;
    }

    simpleOrder.OrderInfo = {
        type: order.type,
        toTerrID: order.toTerrID,
        fromTerrID: order.fromTerrID,
        viaConvoy: order.viaConvoy,
        id: order.id
    };

    if (order.viaConvoy === "") { simpleOrder.OrderInfo.viaConvoy = "No"; }

    for (var key in typeChoices) {
        simpleOrder[key] = {
            name: typeChoices[key],
            results: {},
            prefix: ""
        };
        if (typeof order.beginHTML() !== "undefined") {
            simpleOrder[key].prefix = order.beginHTML().trim();
        }
        order.inputValue("type", key);
        order.updateValue("type", key);
        if (typeof order.updateToTerrChoices() !== "undefined") {
            toTerrChoices = order.updateToTerrChoices()._object;
            toTerrHTML = order.toTerrHTML();
            for (var id in toTerrChoices) {
                try {
                    order.inputValue("toTerrID", id);
                    order.updateValue("toTerrID", id);
                } catch (err) {
                    order.updateValue("toTerrID", id);
                }
                toTerrName = toTerrChoices[id];
                simpleOrder[key]["results"][id] = {
                    name: toTerrName,
                    results: {},
                    prefix: toTerrHTML.split("<")[0]
                    .replace(toTerrName, "")
                    .replace("undefined", "").trim()

                };

                if (typeof order.updateViaConvoyChoices() !== "undefined") {
                    for (var cid in order.updateViaConvoyChoices()._object) {
                        try {
                            order.inputValue("viaConvoy", cid);
                            order.updateValue("viaConvoy", cid);
                        } catch (err) {
                            order.updateValue("viaConvoy", cid);
                        }
                        simpleOrder[key]["results"][id]["results"][cid] = {
                            name: order.updateViaConvoyChoices()._object[cid],
                            results: {},
                            prefix: ""
                        };
                        if (typeof order.convoyPath !== "undefined") { 
                            simpleOrder[key]["results"][id]["results"][cid].prefix = order.convoyPath.toString();
                        }
                    }
                }

                if (typeof order.updateFromTerrChoices() !== "undefined") {
                    fromTerrChoices = order.updateFromTerrChoices()._object;
                    fromTerrHTML = order.fromTerrHTML();				

                    for (var fid in fromTerrChoices) {
                        try {
                            order.inputValue("fromTerrID", fid);
                            order.updateValue("fromTerrID", id);
                        } catch (err) {
                            order.updateValue("fromTerrID", fid);
                        }
                        fromTerrName = fromTerrChoices[fid];
                        if (typeof fromTerrName === "undefined") {
                            console.log(key, id, fid, order.updateFromTerrChoices());
                        }
                        simpleOrder[key]["results"][id]["results"][fid] = {
                            name: fromTerrName,
                            results: {},
                            prefix: fromTerrHTML.split("<")[0]
                            .replace(fromTerrName, "")
                            .replace("undefined", "").trim()
                        };
                        if (key === "Convoy") {
                            simpleOrder[key]["results"][id]["results"][fid]["results"]["Yes"] = {
                                name: "via convoy",
                                results: {},
                                prefix: ""
                            };
                            if (typeof order.convoyPath !== "undefined") { 
                                simpleOrder[key]["results"][id]["results"][fid]["results"]["Yes"].prefix = order.convoyPath.toString();
                            }
                        }
                        if (key === "Support move") {
                            if (typeof order.convoyPath !== "undefined" && order.convoyPath.length > 0) {
                                simpleOrder[key]["results"][id]["results"][fid]["results"]["Yes"] = {
                                    name: "via convoy",
                                    results: {},
                                    prefix: ""
                                };
                                simpleOrder[key]["results"][id]["results"][fid]["results"]["Yes"].prefix = order.convoyPath.toString();
                            }
                        }
                    }
                }
            }
        }
    }

    if (typeof order.Unit !== "undefined") {
        simpleOrder.UnitInfo = {
            type: order.Unit.type,
            id: order.Unit.id,
            terr: {
                id: order.Unit.terrID,
                name: Territories._object[order.Unit.terrID].name
            }
        };
    }

    SimpleOrders.push(simpleOrder);
}
