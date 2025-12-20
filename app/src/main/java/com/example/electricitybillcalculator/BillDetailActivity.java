package com.example.electricitybillcalculator;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import java.text.DecimalFormat;

public class BillDetailActivity extends AppCompatActivity {

    private int billId;
    private BillModel bill;
    private DatabaseHelper databaseHelper;

    private AutoCompleteTextView monthSpinner;
    private TextInputEditText unitsEditText;
    private TextInputLayout unitsInputLayout;
    private RadioGroup rebateRadioGroup;
    private TextView textViewDate, textViewTotalCharges, textViewFinalCost;
    private Button updateButton, deleteButton;

    private String selectedMonth = "";
    private double selectedRebate = 0;

    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill_detail);

        // Get bill ID from intent
        billId = getIntent().getIntExtra("BILL_ID", -1);
        if (billId == -1) {
            Toast.makeText(this, "Invalid bill", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize database
        databaseHelper = new DatabaseHelper(this);

        // Load bill
        bill = databaseHelper.getBillById(billId);
        if (bill == null) {
            Toast.makeText(this, "Bill not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Setup toolbar
        toolbar = findViewById(R.id.toolbar);  // Use the class-level variable
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Bill Details");

        // Initialize views
        initializeViews();

        // Populate data
        populateData();

        // Setup listeners
        setupListeners();
    }

    private void initializeViews() {
        monthSpinner = findViewById(R.id.monthSpinner);
        unitsEditText = findViewById(R.id.unitsEditText);
        unitsInputLayout = findViewById(R.id.unitsInputLayout);
        rebateRadioGroup = findViewById(R.id.rebateRadioGroup);
        textViewDate = findViewById(R.id.textViewDate);
        textViewTotalCharges = findViewById(R.id.textViewTotalCharges);
        textViewFinalCost = findViewById(R.id.textViewFinalCost);
        updateButton = findViewById(R.id.updateButton);
        deleteButton = findViewById(R.id.deleteButton);

        // Setup month spinner
        String[] months = {"January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, months);
        monthSpinner.setAdapter(adapter);
    }

    private void populateData() {
        // Set month
        selectedMonth = bill.getMonth();
        monthSpinner.setText(selectedMonth);

        // Set units
        unitsEditText.setText(String.valueOf(bill.getUnits()));

        // Set rebate
        selectedRebate = bill.getRebate();
        int radioId = getRebateRadioId(selectedRebate);
        rebateRadioGroup.check(radioId);

        // Set date
        textViewDate.setText(bill.getTimestamp());

        // Set calculated values
        DecimalFormat df = new DecimalFormat("#,##0.00");
        textViewTotalCharges.setText("RM " + df.format(bill.getTotalCharges()));
        textViewFinalCost.setText("RM " + df.format(bill.getFinalCost()));
    }

    private int getRebateRadioId(double rebate) {
        if (rebate == 0) return R.id.rebate0;
        if (rebate == 1) return R.id.rebate1;
        if (rebate == 2) return R.id.rebate2;
        if (rebate == 3) return R.id.rebate3;
        if (rebate == 4) return R.id.rebate4;
        if (rebate == 5) return R.id.rebate5;
        return R.id.rebate0;
    }

    private void setupListeners() {
        // Month selection
        monthSpinner.setOnItemClickListener((parent, view, position, id) -> {
            String[] months = {"January", "February", "March", "April", "May", "June",
                    "July", "August", "September", "October", "November", "December"};
            selectedMonth = months[position];
        });

        // Rebate selection
        rebateRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rebate0) {
                selectedRebate = 0;
            } else if (checkedId == R.id.rebate1) {
                selectedRebate = 1;
            } else if (checkedId == R.id.rebate2) {
                selectedRebate = 2;
            } else if (checkedId == R.id.rebate3) {
                selectedRebate = 3;
            } else if (checkedId == R.id.rebate4) {
                selectedRebate = 4;
            } else if (checkedId == R.id.rebate5) {
                selectedRebate = 5;
            }
            calculateAndUpdate();
        });

        // Units input
        unitsEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (validateUnits()) {
                    calculateAndUpdate();
                }
            }
        });



        // Update button
        updateButton.setOnClickListener(v -> updateBill());

        // Delete button
        deleteButton.setOnClickListener(v -> deleteBill());

        // Back navigation
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private boolean validateUnits() {
        String unitsStr = unitsEditText.getText().toString().trim();
        if (unitsStr.isEmpty()) {
            unitsInputLayout.setError("Please enter units");
            return false;
        }

        try {
            double units = Double.parseDouble(unitsStr);
            if (units < 1 || units > 1000) {
                unitsInputLayout.setError("Units must be 1-1000 kWh");
                return false;
            }
        } catch (NumberFormatException e) {
            unitsInputLayout.setError("Invalid number");
            return false;
        }

        unitsInputLayout.setError(null);
        return true;
    }

    private void calculateAndUpdate() {
        if (!validateUnits()) {
            return;
        }

        try {
            double units = Double.parseDouble(unitsEditText.getText().toString().trim());

            // Calculate charges
            double totalCharges = calculateCharges(units);
            double finalCost = totalCharges - (totalCharges * selectedRebate / 100);

            // Update display
            DecimalFormat df = new DecimalFormat("#,##0.00");
            textViewTotalCharges.setText("RM " + df.format(totalCharges));
            textViewFinalCost.setText("RM " + df.format(finalCost));

            // Update bill object
            bill.setMonth(selectedMonth);
            bill.setUnits(units);
            bill.setRebate(selectedRebate);
            bill.setTotalCharges(totalCharges);
            bill.setFinalCost(finalCost);

        } catch (NumberFormatException e) {
            // Ignore for now
        }
    }

    private double calculateCharges(double units) {
        double charges = 0;
        double RATE_1 = 21.8, RATE_2 = 33.4, RATE_3 = 51.6, RATE_4 = 54.6;

        if (units > 600) {
            double block4 = Math.min(units - 600, 300);
            charges += block4 * RATE_4 / 100;
            units -= block4;
        }

        if (units > 300) {
            double block3 = Math.min(units - 300, 300);
            charges += block3 * RATE_3 / 100;
            units -= block3;
        }

        if (units > 200) {
            double block2 = Math.min(units - 200, 100);
            charges += block2 * RATE_2 / 100;
            units -= block2;
        }

        charges += units * RATE_1 / 100;

        return charges;
    }

    private void updateBill() {
        if (!validateUnits() || selectedMonth.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (databaseHelper.updateBill(bill)) {
            Toast.makeText(this, "Bill updated successfully", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, "Failed to update bill", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteBill() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Bill")
                .setMessage("Are you sure you want to delete this bill?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    if (databaseHelper.deleteBill(billId)) {
                        Toast.makeText(this, "Bill deleted", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        Toast.makeText(this, "Failed to delete bill", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    protected void onDestroy() {
        databaseHelper.close();
        super.onDestroy();
    }
}