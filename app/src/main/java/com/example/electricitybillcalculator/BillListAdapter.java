package com.example.electricitybillcalculator;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class BillListAdapter extends ArrayAdapter<BillModel> {

    private Context context;
    private ArrayList<BillModel> billList;
    private DecimalFormat df = new DecimalFormat("#,##0.00");

    public BillListAdapter(Context context, ArrayList<BillModel> billList) {
        super(context, R.layout.list_item_bill, billList);
        this.context = context;
        this.billList = billList;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.list_item_bill, parent, false);
        }

        BillModel bill = billList.get(position);

        TextView textViewMonth = convertView.findViewById(R.id.textViewMonth);
        TextView textViewDate = convertView.findViewById(R.id.textViewDate);
        TextView textViewUnits = convertView.findViewById(R.id.textViewUnits);
        TextView textViewRebate = convertView.findViewById(R.id.textViewRebate);
        TextView textViewTotalCharges = convertView.findViewById(R.id.textViewTotalCharges);
        TextView textViewFinalCost = convertView.findViewById(R.id.textViewFinalCost);

        textViewMonth.setText(bill.getMonth());

        // Format timestamp
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    .parse(bill.getTimestamp());
            textViewDate.setText(sdf.format(date));
        } catch (Exception e) {
            textViewDate.setText(bill.getTimestamp());
        }

        textViewUnits.setText(bill.getUnits() + " kWh");
        textViewRebate.setText(bill.getRebate() + "%");
        textViewTotalCharges.setText("RM " + df.format(bill.getTotalCharges()));
        textViewFinalCost.setText("RM " + df.format(bill.getFinalCost()));

        return convertView;
    }
}