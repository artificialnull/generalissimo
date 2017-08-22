package com.gabdeg.generalissimo;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;



public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder> {

    private AppCompatActivity mContext;
    private ArrayList<Order> orders;

    public OrderAdapter(ArrayList<Order> orders, AppCompatActivity mContext) {
        this.mContext = mContext;
        this.orders = orders;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView orderTextPrefix;
        public Spinner  orderTypeSpinner;
        public Spinner  orderToTerrSpinner;
        public Spinner  orderFromTerrSpinner;
        public Spinner  orderViaConvoySpinner;
        public ImageView    orderIcon;
        public TextView orderToPrefix;
        public TextView orderFromPrefix;

        public ViewHolder(View v) {
            super(v);

            orderIcon = (ImageView) v.findViewById(R.id.unit_icon);

            orderTextPrefix = (TextView) v.findViewById(R.id.order_prefix);
            orderTypeSpinner = (Spinner) v.findViewById(R.id.order_type_spinner);
            orderToTerrSpinner = (Spinner) v.findViewById(R.id.order_to_spinner);
            orderFromTerrSpinner = (Spinner) v.findViewById(R.id.order_from_spinner);
            orderViaConvoySpinner = (Spinner) v.findViewById(R.id.order_via_spinner);
            orderToPrefix = (TextView) v.findViewById(R.id.order_to_prefix);
            orderFromPrefix = (TextView) v.findViewById(R.id.order_from_prefix);

        }
    }


    @Override
    public OrderAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.order_text_view, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        onBindViewHolder(holder, position, null);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position, List<Object> list) {
        final Order order = orders.get(position);

        if (list.size() == 0) {
            //holder.orderTextPrefix.setText(order.getChoices().get(0).getPrefix());
            ArrayList<Order.Choice> choices = order.getChoices();
            ArrayAdapter<String> typeAdapter = new ArrayAdapter<String>(
                    mContext,
                    R.layout.order_spinner_item,
                    order.getChoiceNames()
            );
            typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            holder.orderTypeSpinner.setAdapter(typeAdapter);

            if (order.getOrderUnit() != null) {
                if (order.getOrderUnit().getType().equals("Army")) {
                    holder.orderIcon.setImageResource(R.drawable.army);
                } else if (order.getOrderUnit().getType().equals("Fleet")) {
                    holder.orderIcon.setImageResource(R.drawable.fleet);
                }
            } else {
                holder.orderIcon.setVisibility(View.GONE);
            }

            if (orders.get(position).getSelectedType() != null) {
                holder.orderTypeSpinner.setSelection(
                        typeAdapter.getPosition(orders.get(position).getSelectedType().getName())
                );
            }


            holder.orderTypeSpinner.setOnItemSelectedListener(
                    new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                            String orderTypeSelection = (String) parent.getItemAtPosition(pos);

                            Order.Choice selectedChoice = orders.get(holder.getAdapterPosition())
                                    .getChoiceFromName(orderTypeSelection);
                            orders.get(holder.getAdapterPosition()).setSelectedType(selectedChoice);

                            //Log.v("SELECTED", orders.get(position).getSelectedType().getName());

                            if (selectedChoice.getPrefix().equals("")) {
                                holder.orderTextPrefix.setText(orders.get(holder.getAdapterPosition()).getOrderPrefix());
                            } else {
                                holder.orderTextPrefix.setText(selectedChoice.getPrefix());
                            }

                            if (selectedChoice.getResults().size() != 0) {

                                //holder.orderToPrefix.setText(selectedChoice.getResults().get(0).getPrefix());

                                final ArrayAdapter<String> toTerrAdapter = new ArrayAdapter<String>(
                                        mContext,
                                        R.layout.order_spinner_item,
                                        selectedChoice.getResultNames()
                                );
                                toTerrAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                holder.orderToTerrSpinner.setAdapter(toTerrAdapter);
                                holder.orderToTerrSpinner.setVisibility(View.VISIBLE);
                                holder.orderToPrefix.setVisibility(View.VISIBLE);
                                if (orders.get(holder.getAdapterPosition()).getSelectedToTerr() != null) {
                                    holder.orderToTerrSpinner.setSelection(
                                            toTerrAdapter.getPosition(orders.get(holder.getAdapterPosition()).getSelectedToTerr().getName())
                                    );
                                }

                                holder.orderToTerrSpinner.setOnItemSelectedListener(
                                        new AdapterView.OnItemSelectedListener() {
                                            @Override
                                            public void onItemSelected(AdapterView<?> parent, View view, final int pos, long id) {
                                                String orderToSelection = (String) parent.getItemAtPosition(pos);

                                                Order.Choice selectedChoice = orders.get(holder.getAdapterPosition())
                                                        .getSelectedType()
                                                        .getResultFromName(orderToSelection);
                                                orders.get(holder.getAdapterPosition()).setSelectedToTerr(selectedChoice);
                                                //Log.v("SELECTED", orders.get(position).getSelectedToTerr().getName());


                                                holder.orderToPrefix.setText(selectedChoice.getPrefix());

                                                if (selectedChoice.getResults().size() != 0) {
                                                    if (selectedChoice.getResultFromName("via convoy") != null && selectedChoice.getResultFromName("via land") != null) {
                                                        ArrayAdapter<String> viaConvoyAdapter = new ArrayAdapter<String>(
                                                                mContext,
                                                                R.layout.order_spinner_item,
                                                                selectedChoice.getResultNames()
                                                        );
                                                        viaConvoyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                                        holder.orderViaConvoySpinner.setAdapter(viaConvoyAdapter);
                                                        holder.orderViaConvoySpinner.setVisibility(View.VISIBLE);
                                                        holder.orderFromTerrSpinner.setVisibility(View.GONE);
                                                        holder.orderFromPrefix.setVisibility(View.GONE);
                                                        if (orders.get(holder.getAdapterPosition()).getSelectedViaConvoy() != null) {
                                                            holder.orderViaConvoySpinner.setSelection(
                                                                    viaConvoyAdapter.getPosition(orders.get(holder.getAdapterPosition()).getSelectedViaConvoy().getName())
                                                            );
                                                        }
                                                        holder.orderViaConvoySpinner.setOnItemSelectedListener(
                                                                new AdapterView.OnItemSelectedListener() {
                                                                    @Override
                                                                    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                                                                        String orderToSelection = (String) parent.getItemAtPosition(pos);

                                                                        Order.Choice selectedChoice = orders.get(holder.getAdapterPosition())
                                                                                .getSelectedToTerr()
                                                                                .getResultFromName(orderToSelection);
                                                                        orders.get(holder.getAdapterPosition()).setSelectedViaConvoy(selectedChoice);
                                                                    }

                                                                    @Override
                                                                    public void onNothingSelected(AdapterView<?> parent) {

                                                                    }
                                                                }
                                                        );


                                                    } else if (
                                                            (selectedChoice.getResultFromName("via convoy") == null
                                                                    && selectedChoice.getResultFromName("via land") == null
                                                            )
                                                            && !(selectedChoice.getResultFromName("via convoy") != null
                                                                    || selectedChoice.getResultFromName("via land") != null
                                                            )
                                                            ){
                                                        holder.orderFromPrefix.setText(selectedChoice.getResults().get(0).getPrefix());

                                                        ArrayAdapter<String> fromTerrAdapter = new ArrayAdapter<String>(
                                                                mContext,
                                                                R.layout.order_spinner_item,
                                                                selectedChoice.getResultNames()
                                                        );

                                                        fromTerrAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                                        holder.orderFromTerrSpinner.setAdapter(fromTerrAdapter);
                                                        holder.orderFromTerrSpinner.setVisibility(View.VISIBLE);
                                                        holder.orderFromPrefix.setVisibility(View.VISIBLE);
                                                        holder.orderViaConvoySpinner.setVisibility(View.GONE);
                                                        if (orders.get(holder.getAdapterPosition()).getSelectedFromTerr() != null) {
                                                            holder.orderFromTerrSpinner.setSelection(
                                                                    fromTerrAdapter.getPosition(orders.get(holder.getAdapterPosition()).getSelectedFromTerr().getName())
                                                            );
                                                        }

                                                        holder.orderFromTerrSpinner.setOnItemSelectedListener(
                                                                new AdapterView.OnItemSelectedListener() {
                                                                    @Override
                                                                    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                                                                        String orderToSelection = (String) parent.getItemAtPosition(pos);

                                                                        Order.Choice selectedChoice = orders.get(holder.getAdapterPosition())
                                                                                .getSelectedToTerr()
                                                                                .getResultFromName(orderToSelection);
                                                                        orders.get(holder.getAdapterPosition()).setSelectedFromTerr(selectedChoice);
                                                                    }

                                                                    @Override
                                                                    public void onNothingSelected(AdapterView<?> parent) {

                                                                    }
                                                                }
                                                        );


                                                    } else {
                                                        holder.orderFromPrefix.setVisibility(View.GONE);
                                                        holder.orderFromTerrSpinner.setVisibility(View.GONE);
                                                        holder.orderViaConvoySpinner.setVisibility(View.GONE);
                                                    }

                                                } else {
                                                    holder.orderFromPrefix.setVisibility(View.GONE);
                                                    holder.orderFromTerrSpinner.setVisibility(View.GONE);
                                                    holder.orderViaConvoySpinner.setVisibility(View.GONE);
                                                }

                                            }

                                            @Override
                                            public void onNothingSelected(AdapterView<?> parent) {

                                            }
                                        }
                                );

                            } else {
                                holder.orderToTerrSpinner.setVisibility(View.GONE);
                                holder.orderFromTerrSpinner.setVisibility(View.GONE);
                                holder.orderToPrefix.setVisibility(View.GONE);
                                holder.orderFromPrefix.setVisibility(View.GONE);
                                holder.orderViaConvoySpinner.setVisibility(View.GONE);
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    }
            );

        }
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }
}
