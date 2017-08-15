package com.gabdeg.generalissimo;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.steadystate.css.parser.CSSOMParser;

import org.jsoup.examples.HtmlToPlainText;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.w3c.css.sac.InputSource;
import org.w3c.dom.css.CSSRule;
import org.w3c.dom.css.CSSRuleList;
import org.w3c.dom.css.CSSStyleDeclaration;
import org.w3c.dom.css.CSSStyleRule;
import org.w3c.dom.css.CSSStyleSheet;

import java.io.StringReader;
import java.util.ArrayList;

public class ChatActivity extends AppCompatActivity {

    public final static String CHAT_INFO = "CHAT_INFO";
    public final static String NATION_INFO = "NATION_INFO";

    NationChatPair chatInfo;
    Nation nationInfo;
    ArrayList<ChatMessage> msgs = new ArrayList<>();

    RecyclerView mRecyclerView;
    RecyclerView.LayoutManager mLayoutManager;
    RecyclerView.Adapter mAdapter;
    SwipeRefreshLayout mSwipeRefreshLayout;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        chatInfo = (NationChatPair) getIntent().getExtras().getSerializable(CHAT_INFO);
        nationInfo = (Nation) getIntent().getExtras().getSerializable(NATION_INFO);
        Log.v("CHAT_URL", chatInfo.getChat().getUrl());
        Log.v("PLAYING", nationInfo.getName());

        setContentView(R.layout.activity_chat);

        mRecyclerView = (RecyclerView) findViewById(R.id.chat_recycler_view);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new ChatAdapter(msgs, this);
        mRecyclerView.setAdapter(mAdapter);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.chat_swipe_refresh);
        mSwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        new GetMessagesTask().execute(chatInfo.getChat().getUrl());
                    }
                }
        );
        mSwipeRefreshLayout.setRefreshing(true);
        mSwipeRefreshLayout.setDistanceToTriggerSync(480);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("Chat: " + chatInfo.getNation().getName());


        findViewById(R.id.message_send_button).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(getApplicationContext(),
                                ((EditText) findViewById(R.id.message_text_input)).getText(),
                                Toast.LENGTH_SHORT)
                                .show();
                        ((EditText) findViewById(R.id.message_text_input)).setText("");
                    }
                }
        );

        new GetMessagesTask().execute(chatInfo.getChat().getUrl());

    }

    private class GetMessagesTask extends AsyncTask<String, Void, Integer> {

        protected Integer doInBackground(String... strings) {
            msgs.clear();

            PageParser parser = new PageParser();
            try {
                Networker browser = new Networker();
                parser.loadFromStr(browser.getAsString(strings[0]));

            } catch (Exception err) {
                err.printStackTrace();
                return 1;
            }

            Element chatBox = parser.select("div#chatboxscroll").first().select("table.chatbox").first();
            Log.v("CHAT_HTML", chatBox.html());
            Elements messages = chatBox.select("tr");

            String variantTitle = parser.select(".gamePanel").first().className().split(" ")[1]
                    .replace("variant", "");
            Log.v("VARIANT_NAME", variantTitle);

            CSSStyleSheet styles = null;
            try {
                Networker browser = new Networker();
                styles = new CSSOMParser().parseStyleSheet(
                        new InputSource(
                                new StringReader(
                                        browser.getAsString("http://webdiplomacy.net/variants/"
                                                + variantTitle + "/resources/style.css")
                                )
                        )
                );
            } catch (Exception err) {
                err.printStackTrace();
                return 1;
            }

            CSSRuleList ruleList = styles.getCssRules();

            for (Element message : messages) {

                ChatMessage msg = new ChatMessage();
                try {

                    msg.setContent(
                            new HtmlToPlainText().getPlainText(
                                    message.select(".right").first()
                            )
                    );

                    msg.setSenderName(
                            parser.select("span." +
                                    message.select(".right").first().className()
                                            .split(" ")[1]
                            ).first().text()
                    );
                } catch (Exception err) {
                    break;
                }


                for (int i = 0; i < ruleList.getLength(); i++) {
                    CSSRule rule = ruleList.item(i);
                    if (rule instanceof CSSStyleRule) {
                        CSSStyleRule styleRule = (CSSStyleRule) rule;
                        if (styleRule.getSelectorText().contains("." + message.select(".right").first().className()
                                .split(" ")[1])) {
                            CSSStyleDeclaration styleDeclaration = styleRule.getStyle();

                            for (int j = 0; j < styleDeclaration.getLength(); j++) {
                                String property = styleDeclaration.item(j);
                                if (property.equals("color")) {
                                    String colorString = styleDeclaration
                                            .getPropertyCSSValue(property).getCssText();
                                    colorString = colorString.substring(4, colorString.length()-1);
                                    String[] colors = colorString.split(", ");
                                    int r = Integer.parseInt(colors[0]);
                                    int g = Integer.parseInt(colors[1]);
                                    int b = Integer.parseInt(colors[2]);

                                    Log.v("OldNationColor", String.format("#%02x%02x%02x", r, g, b));

                                    float[] hsv = new float[3];

                                    Color.RGBToHSV(r, g, b, hsv);
                                    hsv[2] = 1;
                                    r = Color.red(Color.HSVToColor(hsv));
                                    g = Color.green(Color.HSVToColor(hsv));
                                    b = Color.blue(Color.HSVToColor(hsv));


                                    msg.setSenderColor(
                                            String.format("#%02x%02x%02x", r, g, b));
                                    j = styleDeclaration.getLength();
                                    i = ruleList.getLength();
                                }
                            }
                        }
                    }
                }

                msgs.add(msg);

            }

            return 0;
        }

        protected void onPostExecute(Integer status) {
            if (status == 0) {
                for (ChatMessage msg : msgs) {
                    Log.v("CHAT_MESSAGE", msg.toString());
                }
                mAdapter.notifyDataSetChanged();
                mRecyclerView.scrollToPosition(msgs.size() - 1);
                mSwipeRefreshLayout.setRefreshing(false);

            }
        }
    }

}
