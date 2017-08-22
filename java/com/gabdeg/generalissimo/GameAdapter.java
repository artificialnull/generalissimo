package com.gabdeg.generalissimo;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


public class GameAdapter extends RecyclerView.Adapter<GameAdapter.ViewHolder> {
    public ArrayList<DiplomacyGame> diplomacyGames;
    private AppCompatActivity mContext;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public CardView mGameCard;
        public TextView mGameTitle;
        public TextView mGamePhaseTime;
        public TextView mGamePhaseDate;
        public TextView mGamePhaseType;
        public TextView mGameCountry;
        public TextView mGameOrderStatus;
        public TextView mGameMailStatus;
        public RelativeLayout mGameLayout;

        public ViewHolder(View v) {
            super(v);

            mGameCard = (CardView) v.findViewById(R.id.card_view);
            mGameTitle = (TextView) v.findViewById(R.id.game_title);
            mGamePhaseTime = (TextView) v.findViewById(R.id.game_phase_time);
            mGamePhaseDate = (TextView) v.findViewById(R.id.game_phase_date);
            mGamePhaseType = (TextView) v.findViewById(R.id.game_phase_type);
            mGameCountry = (TextView) v.findViewById(R.id.game_nation_name);
            mGameOrderStatus = (TextView) v.findViewById(R.id.game_order_status);
            mGameMailStatus = (TextView) v.findViewById(R.id.game_mail_status);
            mGameLayout = (RelativeLayout) v.findViewById(R.id.game_layout);
        }
    }

    public GameAdapter(ArrayList<DiplomacyGame> games, AppCompatActivity context) {
        diplomacyGames = games;
        mContext = context;
    }

    @Override
    public GameAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.game_text_view, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }


    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        onBindViewHolder(holder, position, null);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position, List<Object> list) {
        final DiplomacyGame game = diplomacyGames.get(position);

        if (list.size() == 0) {
            String fullGameName = game.getGameName();
            holder.mGameTitle.setText(
                    fullGameName
            );
            holder.mGamePhaseDate.setText(
                    game.getGamePhaseDate()
            );
            holder.mGamePhaseType.setText(
                    game.getGamePhaseType()
            );
            holder.mGameCountry.setText(
                    game.getGameNation().getName()
            );

            holder.mGameCountry.setTextColor(
                    Color.parseColor(game.getGameNation().getColor())
            );

            if (game.getGameOrderStatus().equals("Ready")) {
                holder.mGameOrderStatus.setText("✓");
                holder.mGameOrderStatus.setTextColor(Color.parseColor("#00bf03"));
            } else if (game.getGameOrderStatus().equals("Not received")) {
                holder.mGameOrderStatus.setText("!!");
                holder.mGameOrderStatus.setTextColor(Color.parseColor("#ff5555"));
            } else if (game.getGameOrderStatus().equals("Not completed")) {
                holder.mGameOrderStatus.setText("!");
                holder.mGameOrderStatus.setTextColor(Color.parseColor("#ff5555"));
            } else if (game.getGameOrderStatus().equals("Completed")){
                holder.mGameOrderStatus.setText("✓");
                holder.mGameOrderStatus.setTextColor(
                        mContext.getResources().getColor(android.R.color.secondary_text_dark)
                );
                // default text color is fine
            } else {
                holder.mGameOrderStatus.setText("―");
                holder.mGameOrderStatus.setTextColor(
                        mContext.getResources().getColor(android.R.color.secondary_text_dark)
                );
                // same as above
            }

            if (game.getGameMessageStatus()) {
                holder.mGameMailStatus.setText("⬤");
            }
        }

        holder.mGamePhaseTime.setText(
                game.getReadableTimeToPhase()
        );
        if (game.getSecondsToPhase() <= 300) {
            holder.mGamePhaseTime.setTextColor(Color.parseColor("#ff5555"));
        }
        holder.mGameLayout.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //int gameID = diplomacyGames.get(holder.getLayoutPosition()).getGameID();

                        Intent intent = new Intent(mContext, GameActivity.class);
                        Bundle args = new Bundle();
                        args.putSerializable(GameActivity.GAME_INFO, game);
                        intent.putExtras(args);
                        mContext.startActivity(intent);

                    }
                }
        );
    }

    @Override
    public int getItemCount() {
        return diplomacyGames.size();
    }
}
