package com.gabdeg.generalissimo;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.transition.TransitionManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
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
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


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
    private ImageButton expandButton;
    private TextView noOrderMessage;
    private CardView cardView;
    private RelativeLayout buttonLayout;

    public boolean isExpanded = true;

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

        cardView = (CardView) view.findViewById(R.id.order_card_view);

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

        buttonLayout = (RelativeLayout) view.findViewById(R.id.order_button_layout) ;
        noOrderMessage = (TextView) view.findViewById(R.id.order_none_message);

        expandButton = (ImageButton) view.findViewById(R.id.order_expand_button);
        expandButton.setBackground(null);
        expandButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        TransitionManager.beginDelayedTransition(cardView);
                        if (isExpanded) {
                            saveButton.setVisibility(View.GONE);
                            readyButton.setVisibility(View.GONE);
                            mRecyclerView.setVisibility(View.GONE);
                            noOrderMessage.setVisibility(View.GONE);
                            buttonLayout.setVisibility(View.GONE);
                            expandButton.setImageResource(R.drawable.expand);
                            isExpanded = false;
                        } else {
                            if (orders.size() > 0) {
                                mRecyclerView.setVisibility(View.VISIBLE);
                                saveButton.setVisibility(View.VISIBLE);
                                readyButton.setVisibility(View.VISIBLE);
                                buttonLayout.setVisibility(View.VISIBLE);
                            } else {
                                noOrderMessage.setVisibility(View.VISIBLE);
                            }
                            expandButton.setImageResource(R.drawable.unexpand);
                            isExpanded = true;
                        }
                    }
                }
        );

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
                                buttonLayout.setVisibility(View.GONE);
                            } else {
                                saveButton.setVisibility(View.VISIBLE);
                                readyButton.setVisibility(View.VISIBLE);
                                mRecyclerView.setVisibility(View.VISIBLE);
                                noOrderMessage.setVisibility(View.GONE);
                                buttonLayout.setVisibility(View.VISIBLE);
                            }
                            isFinishedLoading = true;
                            Log.v("ORDER_FRAGMENT", "Finished loading here!");
                            if (getActivity() != null) {
                                ((GameActivity) getActivity()).isFinished();
                            }
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

                String orderJS = null;
                try {
                    InputStream inp = getActivity().getAssets().open("orders.js");

                    StringBuilder buf = new StringBuilder();
                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(
                                    inp, "UTF-8"
                            )
                    );
                    String line = null;
                    while ((line = in.readLine()) != null) {
                        buf.append(line).append('\n');
                    }

                    orderJS = buf.toString();

                } catch (Exception err) {
                    err.printStackTrace();
                }

                webView.loadUrl(
                        "javascript:" +
                        orderJS +
                        "window.HTMLViewer.getUnitData(JSON.stringify(SimpleOrders));"
                );

                webView.loadUrl("javascript:window.HTMLViewer.getSessionData" +
                        "(JSON.stringify([context, contextKey]));"
                );

            }
        });

        webView.loadUrl("http://webdiplomacy.net/board.php?gameID=" + String.valueOf(gameID));

        mAdapter = new OrderAdapter(orders, (AppCompatActivity) getActivity());
        Log.v("ORDERS", String.valueOf(orders.size()));
        mRecyclerView.setAdapter(mAdapter);

    }

}
