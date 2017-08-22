package com.gabdeg.generalissimo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.jsoup.nodes.Element;

import java.io.IOException;

public class InfoFragment extends Fragment {

    DiplomacyGame game;
    Bitmap thumbnail = null;
    private View view;

    int defaultTextColor;

    Networker browser = new Networker();
    PageParser parser = new PageParser();

    boolean hasStartedIncrementing = false;
    public boolean isFinishedLoading = false;

    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.info_fragment, container, false);

        game = (DiplomacyGame) getArguments().getSerializable(GameActivity.GAME_INFO);

        defaultTextColor = ((TextView) view.findViewById(R.id.info_phase_time)).getTextColors().getDefaultColor();

        view.findViewById(R.id.map_image_small).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Bundle args = new Bundle();
                        args.putSerializable(GameActivity.GAME_INFO, game);
                        Intent intent = new Intent(getContext(), MapActivity.class);
                        intent.putExtras(args);
                        startActivity(intent);
                    }
                }
        );

        updateGameView();
        refreshInfo();

        return view;
    }

    public void refreshInfo() {
        isFinishedLoading = false;
        new UpdateGameInfoTask().execute();
    }

    public Bitmap getBitmapFromURL(String url) {
        try {
            return BitmapFactory.decodeStream(browser.get(url));
        } catch (IOException err) {
            err.printStackTrace();
        }
        return null;
    }

    public Bitmap cropBitmapToSquare(Bitmap toCrop) {
        int width = toCrop.getWidth();
        int height = toCrop.getHeight();
        if (height > width) {
            return Bitmap.createBitmap(toCrop, 0, (height - width) / 2, width, width);
        } else {
            return Bitmap.createBitmap(toCrop, (width - height) / 2, 0, height, height);
        }
    }

    public Bitmap cropBitmapTo16x9(Bitmap toCrop) {
        float width = toCrop.getWidth();
        float height = toCrop.getHeight();

        float newHeight = (width / 16) * 9;

        return Bitmap.createBitmap(
                toCrop,
                0,
                Math.round((height - newHeight) / 2),
                Math.round(width),
                Math.round(newHeight)
        );
    }

    public void updateGameView() {
        ((TextView) view.findViewById(R.id.info_phase_type)).setText(
                game.getGamePhaseType()
        );
        ((TextView) view.findViewById(R.id.info_country_name)).setText(
                game.getGameNation().getName()
        );
        ((TextView) view.findViewById(R.id.info_country_name)).setTextColor(
                Color.parseColor(game.getGameNation().getColor())
        );
        TextView unitCount = ((TextView) view.findViewById(R.id.info_unit_count));
        unitCount.setText(
                game.getGameNation().getUnits()
        );
        try {
            if (Integer.parseInt(game.getGameNation().getUnits().split(" ")[0])
                    > Integer.parseInt(game.getGameNation().getCps().split(" ")[0])) {
                unitCount.setTextColor(Color.parseColor("#ff5555"));
            } else if (Integer.parseInt(game.getGameNation().getUnits().split(" ")[0])
                    < Integer.parseInt(game.getGameNation().getCps().split(" ")[0])) {
                unitCount.setTextColor(Color.parseColor("#00bf03"));
            } else {
                unitCount.setTextColor(defaultTextColor);
            }
        } catch (NumberFormatException err) {
            unitCount.setTextColor(defaultTextColor);
        }

        ((TextView) view.findViewById(R.id.info_cp_count)).setText(
                game.getGameNation().getCps()
        );
        ((TextView) view.findViewById(R.id.info_phase_date)).setText(
                game.getGamePhaseDate()
        );

        TextView mGameOrderStatus = (TextView) view.findViewById(R.id.info_order_status);

        if (game.getGameOrderStatus().equals("Ready")) {
            mGameOrderStatus.setText("✓");
            mGameOrderStatus.setTextColor(Color.parseColor("#009902"));
        } else if (game.getGameOrderStatus().equals("Not received")) {
            mGameOrderStatus.setText("!!");
            mGameOrderStatus.setTextColor(Color.parseColor("#aa0000"));
        } else if (game.getGameOrderStatus().equals("Not completed")) {
            mGameOrderStatus.setText("!");
            mGameOrderStatus.setTextColor(Color.parseColor("#aa0000"));
        } else if (game.getGameOrderStatus().equals("Completed")){
            mGameOrderStatus.setText("✓");
            mGameOrderStatus.setTextColor(Color.parseColor("#000000"));
            // change to dark because of light circle background
        } else {
            mGameOrderStatus.setText("―");
            mGameOrderStatus.setTextColor(Color.parseColor("#000000"));

            // same as above
        }

        /*
        if (game.getGameMessageStatus()) {
            ((TextView) view.findViewById(R.id.info_mail_status)).setText("✉");
        } else {
            ((TextView) view.findViewById(R.id.info_mail_status)).setVisibility(View.INVISIBLE);
        }*/

        if (thumbnail != null) {
            ((ImageView) view.findViewById(R.id.map_image_small)).setImageBitmap(thumbnail);
        }

        final TextView mGamePhaseTime = (TextView) view.findViewById(R.id.info_phase_time);
        final Handler h = new Handler();
        if (!hasStartedIncrementing) {
            h.postDelayed(new Runnable() {
                public void run() {
                    mGamePhaseTime.setText(
                            game.getReadableTimeToPhase()
                    );
                    if (game.getSecondsToPhase() <= 300) {
                        mGamePhaseTime.setTextColor(Color.parseColor("#ff5555"));
                    } else {
                        mGamePhaseTime.setTextColor(defaultTextColor);
                    }
                    h.postDelayed(this, 1000);
                }
            }, 1000);
            hasStartedIncrementing = true;
        }
    }

    private class UpdateGameInfoTask extends AsyncTask<Void, Void, Void> {

        protected Void doInBackground(Void... voids) {
            String gamePage = null;
            try {
                gamePage = browser.getAsString(
                        "http://webdiplomacy.net/board.php?gameID="
                        + String.valueOf(game.getGameID())
                );
            } catch (Exception err) {
                err.printStackTrace();
            }
            parser.loadFromStr(gamePage);

            game.setGamePhaseType(
                    parser.select(".gamePhase").first().text()
            );
            game.setGamePhaseDate(
                    parser.select(".gameDate").first().text()
            );
            game.setGameTimeOfPhase(
                    Long.parseLong(
                            parser.select(".timeremaining").first().attr("unixtime")
                    )
            );
            try {
                Element gameUserDetail = parser.select(".memberUserDetail").first();
                if (gameUserDetail.html().contains("Completed")) {
                    game.setGameOrderStatus("Completed");
                } else if (gameUserDetail.html().contains("Ready")) {
                    game.setGameOrderStatus("Ready");
                } else if (gameUserDetail.html().contains("Not received")) {
                    game.setGameOrderStatus("Not received");
                } else if (gameUserDetail.html().contains("not completed")) {
                    game.setGameOrderStatus("Not completed");
                } else {
                    game.setGameOrderStatus("None");
                }

                if (gameUserDetail.html().contains("Unread message")) {
                    game.setGameMessageStatus(true);
                } else {
                    game.setGameMessageStatus(false);
                }
            } catch (NullPointerException err) {
                game.setGameOrderStatus("None");
                game.setGameMessageStatus(false);
            }
            try {
                Element gameUnitLine = parser.select(".memberSCCount").first();
                game.getGameNation().setCps(gameUnitLine.text().split(", ")[0]);
                game.getGameNation().setUnits(gameUnitLine.text().split(", ")[1]);
            } catch (NullPointerException err) {
                game.getGameNation().setUnits("N/A");
                game.getGameNation().setCps("N/A");
            }

            String mapUrl = "http://webdiplomacy.net/" + parser.select("#mapImage").attr("src");
            thumbnail = getBitmapFromURL(mapUrl);
            if (thumbnail == null) {
                thumbnail = BitmapFactory.decodeResource(getResources(), R.drawable.sadface);
            }
            thumbnail = cropBitmapTo16x9(thumbnail);

            return null;
        }

        protected void onPostExecute(Void voido) {
            updateGameView();
            isFinishedLoading = true;
            if (getActivity() != null) {
                ((GameActivity) getActivity()).isFinished();
            }

        }
    }


}
