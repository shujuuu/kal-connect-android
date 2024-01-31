package com.kal.connect.modules.dashboard.AppointmentsDetails.Tabs.ECG;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;


import com.kal.connect.R;

import java.util.ArrayList;
import java.util.HashMap;

public class ECGAdapter extends RecyclerView.Adapter<ECGAdapter.ViewHolder> {


    // Step 1: Initialize By receiving the data via constructor
    Context mContext;
    ArrayList<HashMap<String, Object>> items;
    private static ECGAdapter.ItemClickListener itemClickListener;


    public ECGAdapter(ArrayList<HashMap<String, Object>> partnerItems, Context context) {
        this.items = partnerItems;
        this.mContext = context;
    }

    // Step 2: Create View Holder class to set the data for each cell
    public class ViewHolder extends RecyclerView.ViewHolder implements View
            .OnClickListener {

        public TextView lblRecordName;
        public Button btnView;

        public ViewHolder(View view) {
            super(view);

            btnView = (Button) view.findViewById(R.id.btnView);
            lblRecordName = (TextView) view.findViewById(R.id.lblRecordName);
            itemView.setOnClickListener(this);
            btnView.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {

            itemClickListener.onItemClick(getAdapterPosition(), v);

            notifyDataSetChanged();


        }

    }

    // Step 3: Override Recyclerview methods to load the data one by one
    @Override
    public ECGAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.record_item, parent, false);
        return new ECGAdapter.ViewHolder(itemView);

    }

    @Override
    public void onBindViewHolder(ECGAdapter.ViewHolder holder, int position) {

        HashMap<String, Object> item = items.get(position);
        String recordName = (item.get("recordName") != null) ? item.get("recordName").toString() : "";
        holder.lblRecordName.setText(recordName);

    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    // Step 4: Create Interface and it's methods to Receive Click event
    public interface ItemClickListener {
        public void onItemClick(int position, View v);
    }

    // Method to receive Click event
    public void setOnItemClickListener(ECGAdapter.ItemClickListener clickListener) {
        this.itemClickListener = clickListener;

    }

}

