package com.gabdeg.generalissimo;


import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class NationAdapter extends RecyclerView.Adapter<NationAdapter.ViewHolder> {

    ArrayList<NationChatPair> nations = new ArrayList<>();
    private AppCompatActivity mContext;

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView mNationName;
        public TextView mNationCpCount;
        public TextView mNationUnitCount;
        public TextView mNationCpUnitSep;

        public ViewHolder(View v) {
            super(v);

            mNationName = (TextView) v.findViewById(R.id.chat_country_name);
            mNationCpCount = (TextView) v.findViewById(R.id.chat_cp_count);
            mNationUnitCount = (TextView) v.findViewById(R.id.chat_unit_count);
            mNationCpUnitSep = (TextView) v.findViewById(R.id.chat_unit_cp_sep);
        }
    }

    public NationAdapter(ArrayList<NationChatPair> nationChatMap, AppCompatActivity context) {
        nations = nationChatMap;
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
    public void onBindViewHolder(final ViewHolder holder, int position) {
        Nation nation = nations.get(position).getNation();

        holder.mNationName.setText(nation.getName());
        holder.mNationName.setTextColor(
                Color.parseColor(nation.getColor())
        );
        holder.mNationCpCount.setText(nation.getCps());
        holder.mNationUnitCount.setText(nation.getUnits());

        if (nation.getCps().equals("N/A") && nation.getUnits().equals("N/A")) {
            holder.mNationUnitCount.setText("Defeated");
            holder.mNationCpUnitSep.setVisibility(View.GONE);
            holder.mNationCpCount.setText(" ");
        } else {
            holder.mNationCpUnitSep.setVisibility(View.VISIBLE);
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
            Log.v("PARSE_INT", "Couldnt PRASE!!IO");
        }

    }

    @Override
    public int getItemCount() {
        return nations.size();
    }

}
