package com.gabdeg.generalissimo;


import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class NationAdapter extends RecyclerView.Adapter<NationAdapter.ViewHolder> {

    ArrayList<NationChatPair> nations = new ArrayList<>();
    Nation self;
    private AppCompatActivity mContext;

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView mNationName;
        public TextView mNationCpCount;
        public TextView mNationUnitCount;
        public TextView mNationCpUnitSep;
        public TextView mNationOrderStatus;
        public Button   mChatButton;

        public ViewHolder(View v) {
            super(v);

            mNationName = (TextView) v.findViewById(R.id.chat_country_name);
            mNationCpCount = (TextView) v.findViewById(R.id.chat_cp_count);
            mNationUnitCount = (TextView) v.findViewById(R.id.chat_unit_count);
            mNationCpUnitSep = (TextView) v.findViewById(R.id.chat_unit_cp_sep);
            mNationOrderStatus = (TextView) v.findViewById(R.id.nation_order_status);
            mChatButton = (Button) v.findViewById(R.id.chat_open_button);
        }
    }

    public NationAdapter(ArrayList<NationChatPair> nationChatMap,
                         Nation player, AppCompatActivity context) {
        nations = nationChatMap;
        self = player;
        mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.chat_text_view, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        Nation nation = nations.get(position).getNation();
        Chat chat = nations.get(position).getChat();

        holder.mNationName.setText(nation.getName());
        holder.mNationName.setTextColor(
                Color.parseColor(nation.getColor())
        );
        holder.mNationCpCount.setText(nation.getCps());
        holder.mNationUnitCount.setText(nation.getUnits());

        if (nation.getCps().equals("N/A") && nation.getUnits().equals("N/A")) {
            holder.mNationUnitCount.setText("Defeated");
            holder.mNationCpUnitSep.setVisibility(View.GONE);
            holder.mNationCpCount.setText("");
        } else if (nation.getUnits().equals("Global")) {
            holder.mNationUnitCount.setText("Public");
            holder.mNationCpUnitSep.setVisibility(View.GONE);
            holder.mNationCpCount.setText("");
        } else {
            holder.mNationCpUnitSep.setVisibility(View.VISIBLE);
        }

        try {
            if (nation.getOrderStatus().equals("Ready")) {
                holder.mNationOrderStatus.setText("✓");
                holder.mNationOrderStatus.setTextColor(Color.parseColor("#00bf03"));
            } else if (nation.getOrderStatus().equals("Not received")) {
                holder.mNationOrderStatus.setText("!!");
                holder.mNationOrderStatus.setTextColor(Color.parseColor("#ff5555"));
            } else if (nation.getOrderStatus().equals("Not completed")) {
                holder.mNationOrderStatus.setText("!");
                holder.mNationOrderStatus.setTextColor(Color.parseColor("#ff5555"));
            } else if (nation.getOrderStatus().equals("Completed")) {
                holder.mNationOrderStatus.setText("✓");
                holder.mNationOrderStatus.setTextColor(Color.parseColor("#000000"));
                // default text color is fine
            } else {
                holder.mNationOrderStatus.setText("―");
                holder.mNationOrderStatus.setTextColor(Color.parseColor("#000000"));
                // same as above
            }
        } catch (Exception err) {
            err.printStackTrace();
            holder.mNationOrderStatus.setText("―");
            holder.mNationOrderStatus.setTextColor(Color.parseColor("#000000"));
        }

        try {
            if (Integer.parseInt(nation.getUnits().split(" ")[0])
                    > Integer.parseInt(nation.getCps().split(" ")[0])) {
                holder.mNationUnitCount.setTextColor(Color.parseColor("#ff5555"));
            } else if (Integer.parseInt(nation.getUnits().split(" ")[0])
                    < Integer.parseInt(nation.getCps().split(" ")[0])) {
                holder.mNationUnitCount.setTextColor(Color.parseColor("#00bf03"));
            } else {
                holder.mNationUnitCount.setTextColor(
                        mContext.getResources().getColor(android.R.color.secondary_text_dark));
            }
        } catch (NumberFormatException err) {
        }

        if (chat == null) {
            holder.mChatButton.setVisibility(View.GONE);
        } else {
            holder.mChatButton.setVisibility(View.VISIBLE);

            if (chat.hasUnread()) {
                holder.mChatButton.setTextColor(
                        mContext.getResources().getColor(android.R.color.holo_blue_light)
                );
            } else {
                holder.mChatButton.setTextColor(
                        mContext.getResources().getColor(android.R.color.primary_text_dark)
                );
            }

            //Log.v("CHAT_URL", chat.getUrl());
        }

        holder.mChatButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(mContext, ChatActivity.class);
                        Bundle args = new Bundle();
                        args.putSerializable(ChatActivity.CHAT_INFO, nations.get(position));
                        args.putSerializable(ChatActivity.NATION_INFO, self);
                        intent.putExtras(args);
                        mContext.startActivity(intent);
                    }
                }
        );

    }

    @Override
    public int getItemCount() {
        return nations.size();
    }

}
