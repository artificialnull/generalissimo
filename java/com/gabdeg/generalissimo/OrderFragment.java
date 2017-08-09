package com.gabdeg.generalissimo;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.HttpCookie;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;


public class OrderFragment extends Fragment {

    int gameID;
    JSONArray unitData;
    JSONArray sessionData;
    ArrayList<UnitOrder> unitOrders = new ArrayList<>();
    PageParser parser = new PageParser();
    Networker browser = new Networker();
    private View view;
    boolean isReady = false;
    WebView webView;
    ArrayList<Order> orders = new ArrayList<>();

    public boolean isFinishedLoading = false;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private Button saveButton;
    private Button readyButton;
    private TextView noOrderMessage;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        gameID = ((DiplomacyGame) getArguments().getSerializable(GameActivity.GAME_INFO)).getGameID();

        CookieManager.getInstance().setAcceptCookie(true);
        java.net.URI baseURI = null;
        try {
            baseURI = new URI("http://webdiplomacy.net/");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        List<HttpCookie> cookies = browser.getCookies();
        String url = baseURI.toString();
        for (HttpCookie cookie : cookies) {
            String setCookie = new StringBuilder(cookie.toString())
                    .append("; domain=").append(cookie.getDomain())
                    .append("; path=").append(cookie.getPath())
                    .toString();
            Log.v("Cookie", cookie.toString());

            CookieManager.getInstance().setCookie(url, setCookie);
        }

        view = inflater.inflate(R.layout.order_fragment, container, false);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.order_recycler_view);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(
                mRecyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        mRecyclerView.addItemDecoration(mDividerItemDecoration);

        webView = (WebView) view.findViewById(R.id.webview);
        WebSettings webSettings = webView.getSettings();
        webSettings.setDomStorageEnabled(true);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setLoadsImagesAutomatically(false);
        webView.addJavascriptInterface(
                new OrderFragment.MyJavaScriptInterface(getActivity()), "HTMLViewer");

        saveButton = (Button) view.findViewById(R.id.order_save_button);
        saveButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new SaveOrdersTask().execute(0);
                    }
                }
        );

        readyButton = (Button) view.findViewById(R.id.order_ready_button);
        readyButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new SaveOrdersTask().execute(1);
                    }
                }
        );

        noOrderMessage = (TextView) view.findViewById(R.id.order_none_message);

        getGamePage(gameID);

        mAdapter.notifyDataSetChanged();

        return view;
    }

    private class SaveOrdersTask extends AsyncTask<Integer, Void, Integer> {

        protected Integer doInBackground(Integer... ints) {
            try {
                String paramStr = "orderUpdates=";
                JSONArray ordersToSave = new JSONArray();
                for (Order order : orders) {
                    ordersToSave.put(order.toJSONObject());
                }
                paramStr += ordersToSave.toString();
                paramStr += "&context=";
                paramStr += OrderFragment.this.sessionData.getJSONObject(0).toString();
                paramStr += "&contextKey=";
                paramStr += OrderFragment.this.sessionData.getString(1);
                paramStr += "&_=";
                Log.v("ORDERS_TO_JSON", paramStr);

                String link = "http://webdiplomacy.net/ajax.php";
                if (ints[0] == 1) {
                    if (!isReady) {
                        link += "?ready=on";
                    } else {
                        link += "?notready=on";
                    }
                }
                Log.v("SUBMIT_LINK", link);

                browser.post(
                        link,
                        paramStr
                );
                return 0;

            } catch (Exception err) {
                err.printStackTrace();
                return 1;
            }
        }

        protected void onPostExecute(Integer status) {
            if (status != 0) {
                Toast.makeText(getActivity(), "Could not submit orders!", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getActivity(), "Orders submitted", Toast.LENGTH_SHORT).show();
                ((GameActivity) getActivity()).refreshGame();
            }
        }
    }

    public void setGameHTML(String html) {
        parser.loadFromStr(html);
    }

    public void setUnitData(String unitData) {
        unitData = unitData.replace("\\", "");
        unitData = unitData.substring(1, unitData.length() - 1);
        try {
            this.unitData = new JSONArray(unitData);
            this.orders.clear();
            Log.v("Number of units recv", String.valueOf(this.unitData.length()));
            for (int i = 0; i < this.unitData.length(); i++) {
                Order order = new Order();
                order.loadFromJSONObject(this.unitData.getJSONObject(i));
                //Log.v("ORDER", order.toString());
                this.orders.add(order);
            }
            getActivity().runOnUiThread(
                    new Runnable() {
                        @Override
                        public void run() {
                            mAdapter.notifyDataSetChanged();
                            if (orders.size() <= 0) {
                                saveButton.setVisibility(View.GONE);
                                readyButton.setVisibility(View.GONE);
                                mRecyclerView.setVisibility(View.GONE);
                                noOrderMessage.setVisibility(View.VISIBLE);
                            } else {
                                saveButton.setVisibility(View.VISIBLE);
                                readyButton.setVisibility(View.VISIBLE);
                                mRecyclerView.setVisibility(View.VISIBLE);
                                noOrderMessage.setVisibility(View.GONE);
                            }
                            isFinishedLoading = true;
                            Log.v("ORDER_FRAGMENT", "Finished loading here!");
                            ((GameActivity) getActivity()).isFinished();
                        }
                    }
            );


        } catch (Exception e) {
            e.printStackTrace();
            this.unitData = null;
        }
    }

    public void setSessionData(String sessionData) {
        sessionData = sessionData.replace("\\", "");
        sessionData = sessionData.substring(1, sessionData.length() - 1);
        Log.v("SESSION_STRINGS", sessionData);
        try {
            if (this.sessionData != null) {
                Log.v("PAST_TURN", this.sessionData.getJSONObject(0).getString("turn"));
                Log.v("THIS_TURN", new JSONArray(sessionData).getJSONObject(0).getString("turn"));
                if (new JSONArray(sessionData).getJSONObject(0).getInt("turn") >
                        this.sessionData.getJSONObject(0).getInt("turn")) {
                    Log.v("GAME_ADVANCED", "Wahoo!");
                    getActivity().runOnUiThread(
                            new Runnable() {
                                @Override
                                public void run() {
                                    ((GameActivity) getActivity()).refreshGame();
                                }
                            }
                    );

                }
            }
            this.sessionData = new JSONArray(sessionData);
            if (this.sessionData.getJSONObject(0).getString("orderStatus").contains("Ready")) {
                getActivity().runOnUiThread(
                        new Runnable() {
                            @Override
                            public void run() {
                                readyButton.setText("not ready");
                                Log.v("READINESS", "IS_READY");
                                isReady = true;
                            }
                        }
                );
            } else {
                getActivity().runOnUiThread(
                        new Runnable() {
                            @Override
                            public void run() {
                                readyButton.setText("ready");
                                isReady = false;
                            }
                        }
                );
            }
        } catch (Exception err) {
            err.printStackTrace();
        }
    }

    public void parseOrdersFromStr(String orderStr) {
        try {
            unitOrders.clear();
            orderStr = orderStr.replace("\\", "");
            orderStr = orderStr.substring(1, orderStr.length() - 1);
            Log.v("ORDERS_DATA", orderStr);
            JSONArray orderJSON = new JSONArray(orderStr);
            Log.v("NUMBER_OF_ORDERS", String.valueOf(orderJSON.length()));
            Elements units = parser.select(".order");
            for (int i = 0; i < orderJSON.length(); i++) {
                UnitOrder newUnitOrder = new UnitOrder();
                Element unit = units.get(i);
                String unitID = unit.select("div").first().attr("id").substring(7);
                String unitTitle = unit.select(".orderBegin").first().text();
                if (unitTitle.equals("")) {
                    unitTitle = "Build/Destroy";
                }
                Elements typeOptions = unit.select(".type").first()
                        .select("select").first()
                        .select("option");
                for (Element typeOption : typeOptions) {
                    newUnitOrder.allType.put(
                            typeOption.attr("value"), typeOption.text()
                    );
                    //Log.v("Possible orders", newUnitOrder.allType.get(typeOption.attr("value")));
                }



                newUnitOrder.loadFromJSONObject(orderJSON.getJSONObject(i));
                if (unitID.equals(newUnitOrder.getOrderID())) {
                    newUnitOrder.setFormalUnitTitle(unitTitle);
                }
                unitOrders.add(newUnitOrder);
            }

            for (UnitOrder unitOrder : unitOrders) {
                //Log.v(unitOrder.getFormalUnitTitle(), unitOrder.allType.get(unitOrder.getType()));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    class MyJavaScriptInterface {
        private Context ctx;

        MyJavaScriptInterface(Context ctx) {
            this.ctx = ctx;
        }
        @JavascriptInterface
        public void showHTML(String html) {
            Log.v("SHOW_HTML", "METHOD RUN");
            OrderFragment.this.setGameHTML(html);
        }

        @JavascriptInterface
        public void getUnitData(String unitData) {
            Log.v("SET_UNIT_DATA", "METHOD RUN");
            OrderFragment.this.setUnitData(unitData);
        }

        @JavascriptInterface
        public void getSessionData(String sessionData) {
            Log.v("SET_SESSION_DATA", "METHOD RUN");
            OrderFragment.this.setSessionData(sessionData);
        }

        @JavascriptInterface
        public void getOrdersData(String orderStr) {
            Log.v("GET_ORDERS_DATA", "METHOD RUN");
            OrderFragment.this.parseOrdersFromStr(orderStr);
        }
    }


    public void getGamePage(int gameID) {

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                Log.v("COOKIE", CookieManager.getInstance().getCookie(url));
                /*
                webView.loadUrl("javascript:window.HTMLViewer.showHTML" +
                        "('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');"
                );
                */

                webView.loadUrl("javascript:SimpleOrders = [];\n" +
                        "if (typeof MyOrders === \"undefined\") {\n" +
                        "\tMyOrders = [];\n" +
                        "}\n" +
                        "for (var i = 0; i < MyOrders.length; i++) {\n" +
                        "\torder = MyOrders[i];\n" +
                        "\tsimpleOrder = {};\n" +
                        "\ttypeChoices = order.updateTypeChoices();\n" +
                        "\tif (typeof typeChoices._object !== \"undefined\") {\n" +
                        "\t\ttypeChoices = typeChoices._object;\n" +
                        "    }\n" +
                        "\n" +
                        "\tsimpleOrder.OrderInfo = {\n" +
                        "\t\ttype: order.type,\n" +
                        "\t\ttoTerrID: order.toTerrID,\n" +
                        "\t\tfromTerrID: order.fromTerrID,\n" +
                        "\t\tviaConvoy: order.viaConvoy,\n" +
                        "\t\tid: order.id\n" +
                        "    };\n" +
                        "\n" +
                        "\tif (order.viaConvoy === \"\") { simpleOrder.OrderInfo.viaConvoy = \"No\"; }\n" +
                        "\n" +
                        "\tfor (var key in typeChoices) {\n" +
                        "\t\tsimpleOrder[key] = {\n" +
                        "\t\t\tname: typeChoices[key],\n" +
                        "\t\t\tresults: {},\n" +
                        "\t\t\tprefix: \"\"\n" +
                        "        };\n" +
                        "\t\tif (typeof order.beginHTML() !== \"undefined\") {\n" +
                        "\t\t\tsimpleOrder[key].prefix = order.beginHTML().trim();\n" +
                        "        }\n" +
                        "\t\torder.inputValue(\"type\", key);\n" +
                        "\t\tif (typeof order.updateToTerrChoices() !== \"undefined\") {\n" +
                        "            for (var id in order.updateToTerrChoices()._object) {\n" +
                        "\t\t\t\ttry {\n" +
                        "                \torder.inputValue(\"toTerrID\", id);\n" +
                        "                } catch (err) {\n" +
                        "\t\t\t\t\torder.updateValue(\"toTerrID\", id);\n" +
                        "                }\n" +
                        "\t\t\t\ttoTerrName = order.updateToTerrChoices()._object[id];\n" +
                        "                simpleOrder[key][\"results\"][id] = {\n" +
                        "                    name: toTerrName,\n" +
                        "\t\t\t\t\tresults: {},\n" +
                        "\t\t\t\t\tprefix: order.toTerrHTML().split(\"<\")[0].replace(toTerrName, \"\").trim()\n" +
                        "\n" +
                        "                };\n" +
                        "\n" +
                        "\t\t\t\tif (typeof order.updateViaConvoyChoices() !== \"undefined\") {\n" +
                        "\t\t\t\t\tfor (var cid in order.updateViaConvoyChoices()._object) {\n" +
                        "\t\t\t\t\t\torder.inputValue(\"viaConvoy\", cid);\n" +
                        "\t\t\t\t\t\tsimpleOrder[key][\"results\"][id][\"results\"][cid] = {\n" +
                        "\t\t\t\t\t\t\tname: order.updateViaConvoyChoices()._object[cid],\n" +
                        "\t\t\t\t\t\t\tresults: {},\n" +
                        "\t\t\t\t\t\t\tprefix: \"\"\n" +
                        "                        };\n" +
                        "\t\t\t\t\t\tif (typeof order.convoyPath !== \"undefined\") { \n" +
                        "\t\t\t\t\t\t\tsimpleOrder[key][\"results\"][id][\"results\"][cid].prefix = order.convoyPath.toString();\n" +
                        "                        }\n" +
                        "                    }\n" +
                        "                }\n" +
                        "\n" +
                        "\t\t\t\tif (typeof order.updateFromTerrChoices() !== \"undefined\") {\n" +
                        "\t\t\t\t\tfromTerrChoices = order.updateFromTerrChoices()._object;\n" +
                        "\t\t\t\t\tfromTerrHTML = order.fromTerrHTML();\n" +
                        "\n" +
                        "                    for (var fid in fromTerrChoices) {\n" +
                        "\t\t\t\t\t\ttry {\n" +
                        "\t\t\t\t\t\t\torder.inputValue(\"fromTerrID\", fid);\n" +
                        "                        } catch (err) {\n" +
                        "\t\t\t\t\t\t\torder.updateValue(\"fromTerrID\", fid);\n" +
                        "                        }\n" +
                        "\t\t\t\t\t\tfromTerrName = fromTerrChoices[fid];\n" +
                        "\t\t\t\t\t\tif (typeof fromTerrName === \"undefined\") {\n" +
                        "\t\t\t\t\t\t\tconsole.log(key, id, fid, order.updateFromTerrChoices());\n" +
                        "                        }\n" +
                        "\t\t\t\t\t\tsimpleOrder[key][\"results\"][id][\"results\"][fid] = {\n" +
                        "                        \tname: fromTerrName,\n" +
                        "\t\t\t\t\t\t\tresults: {},\n" +
                        "\t\t\t\t\t\t\tprefix: fromTerrHTML.split(\"<\")[0].replace(fromTerrName, \"\").trim()\n" +
                        "                        };\n" +
                        "\t\t\t\t\t\tif (key === \"Convoy\") {\n" +
                        "\t\t\t\t\t\t\tsimpleOrder[key][\"results\"][id][\"results\"][fid][\"results\"][\"Yes\"] = {\n" +
                        "\t\t\t\t\t\t\t\tname: \"via convoy\",\n" +
                        "\t\t\t\t\t\t\t\tresults: {},\n" +
                        "\t\t\t\t\t\t\t\tprefix: \"\"\n" +
                        "                            };\n" +
                        "                            if (typeof order.convoyPath !== \"undefined\") { \n" +
                        "                            \tsimpleOrder[key][\"results\"][id][\"results\"][fid][\"results\"][\"Yes\"].prefix = order.convoyPath.toString();\n" +
                        "                            }\n" +
                        "                        }\n" +
                        "\t\t\t\t\t\tif (key === \"Support move\") {\n" +
                        "\t\t\t\t\t\t\tif (typeof order.convoyPath !== \"undefined\" && order.convoyPath.length > 0) {\n" +
                        "\t\t\t\t\t\t\t\tsimpleOrder[key][\"results\"][id][\"results\"][fid][\"results\"][\"Yes\"] = {\n" +
                        "                                    name: \"via convoy\",\n" +
                        "                                    results: {},\n" +
                        "                                    prefix: \"\"\n" +
                        "                           \t\t};\n" +
                        "                            \tsimpleOrder[key][\"results\"][id][\"results\"][fid][\"results\"][\"Yes\"].prefix = order.convoyPath.toString();\n" +
                        "                            }\n" +
                        "                        }\n" +
                        "                    }\n" +
                        "                } \n" +
                        "            }\n" +
                        "        }\n" +
                        "    }\n" +
                        "\t\n" +
                        "\tif (typeof order.Unit !== \"undefined\") {\n" +
                        "\t\tsimpleOrder.UnitInfo = {\n" +
                        "\t\t\ttype: order.Unit.type,\n" +
                        "\t\t\tid: order.Unit.id,\n" +
                        "\t\t\tterr: {\n" +
                        "\t\t\t\tid: order.Unit.terrID,\n" +
                        "\t\t\t\tname: Territories._object[order.Unit.terrID].name\n" +
                        "            }\n" +
                        "        }\n" +
                        "    }\n" +
                        "\t\n" +
                        "\tSimpleOrders.push(simpleOrder);\n" +
                        "} window.HTMLViewer.getUnitData(JSON.stringify(SimpleOrders));");

                webView.loadUrl("javascript:window.HTMLViewer.getSessionData" +
                        "(JSON.stringify([context, contextKey]));"
                );

                /*
                webView.loadUrl("javascript:window.HTMLViewer.getOrdersData" +
                        "(JSON.stringify(ordersData));"
                );*/
            }
        });

        webView.loadUrl("http://webdiplomacy.net/board.php?gameID=" + String.valueOf(gameID));

        mAdapter = new OrderAdapter(orders, (AppCompatActivity) getActivity());
        Log.v("ORDERS", String.valueOf(orders.size()));
        mRecyclerView.setAdapter(mAdapter);

    }

}
