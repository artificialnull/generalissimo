package com.gabdeg.generalissimo;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    ArrayList<ChatMessage> msgs = new ArrayList<>();
    private AppCompatActivity mContext;

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView msgSender;
        public TextView msgContent;

        public ViewHolder(View v) {
            super(v);

            msgContent = (TextView) v.findViewById(R.id.chat_message_content);
            msgSender = (TextView) v.findViewById(R.id.chat_message_sender);

        }
    }

    public ChatAdapter(ArrayList<ChatMessage> msgs, AppCompatActivity mContext) {
        this.msgs = msgs;
        this.mContext = mContext;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_text_view, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.msgSender.setText(
                msgs.get(position).getSenderName()
        );
        holder.msgSender.setTextColor(
                Color.parseColor(
                        msgs.get(position).getSenderColor()
                )
        );
        holder.msgContent.setText(
                msgs.get(position).getContent()
        );
    }

    @Override
    public int getItemCount() {
        return msgs.size();
    }

}
