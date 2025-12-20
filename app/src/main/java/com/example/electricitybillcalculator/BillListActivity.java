package com.example.electricitybillcalculator;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;

public class BillListActivity extends AppCompatActivity {

    private ListView billListView;
    private TextView emptyTextView;
    private FloatingActionButton fabBack;
    private DatabaseHelper databaseHelper;
    private ArrayList<BillModel> billList;
    private BillListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill_list);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Bill History");
        }

        // Setup back navigation
        toolbar.setNavigationOnClickListener(v -> finish());

        // Initialize database
        databaseHelper = new DatabaseHelper(this);

        // Initialize views
        billListView = findViewById(R.id.billListView);
        emptyTextView = findViewById(R.id.emptyTextView);
        fabBack = findViewById(R.id.fabBack);

        // Load bills
        loadBills();

        // Setup list view click listener
        billListView.setOnItemClickListener((parent, view, position, id) -> {
            BillModel bill = billList.get(position);
            openBillDetail(bill);
        });

        // Setup FAB click listener
        fabBack.setOnClickListener(v -> finish());
    }

    private void loadBills() {
        billList = databaseHelper.getAllBills();

        if (billList.isEmpty()) {
            emptyTextView.setVisibility(View.VISIBLE);
            billListView.setVisibility(View.GONE);
            emptyTextView.setText("No bills saved yet.\n\nCalculate and save your first bill!");
        } else {
            emptyTextView.setVisibility(View.GONE);
            billListView.setVisibility(View.VISIBLE);

            adapter = new BillListAdapter(this, billList);
            billListView.setAdapter(adapter);
        }
    }

    private void openBillDetail(BillModel bill) {
        Intent intent = new Intent(this, BillDetailActivity.class);
        intent.putExtra("BILL_ID", bill.getId());
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadBills(); // Refresh list when returning to activity
    }

    @Override
    protected void onDestroy() {
        databaseHelper.close();
        super.onDestroy();
    }
}