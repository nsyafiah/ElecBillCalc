package com.example.electricitybillcalculator;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity {

    private Spinner monthSpinner;
    private TextInputEditText unitsEditText;
    private TextInputLayout unitsInputLayout;
    private RadioGroup rebateRadioGroup;
    private MaterialCardView resultsCard;
    private TextView resultMonth, resultTotalCharges, resultRebate, resultFinalCost;
    private Button calculateButton, saveButton, historyButton, aboutButton;

    private DatabaseHelper databaseHelper;
    private String selectedMonth = "";
    private double calculatedTotalCharges = 0;
    private double calculatedFinalCost = 0;
    private double selectedRebate = 0;

    // Rate table (sen per kWh)
    private static final double RATE_1 = 21.8; // First 200 kWh
    private static final double RATE_2 = 33.4; // Next 100 kWh (201-300)
    private static final double RATE_3 = 51.6; // Next 300 kWh (301-600)
    private static final double RATE_4 = 54.6; // Next 300 kWh (601-1000)


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize database
        databaseHelper = new DatabaseHelper(this);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set the navigation (menu) icon
        toolbar.setNavigationIcon(R.drawable.ic_menu);


        // Set click listener for the navigation icon
        toolbar.setNavigationOnClickListener(v -> {
                    Toast.makeText(MainActivity.this, "Menu clicked", Toast.LENGTH_SHORT).show();
        });

        // Initialize views
        initializeViews();

        // Setup month spinner
        setupMonthSpinner();

        // Setup input validation
        setupInputValidation();

        // Setup button click listeners
        setupButtonListeners();

        // Setup radio group listener
        setupRadioGroupListener();

        Toast.makeText(this, "App Started Successfully!", Toast.LENGTH_SHORT).show();
    }

    private void initializeViews() {
        monthSpinner = findViewById(R.id.monthSpinner);
        unitsEditText = findViewById(R.id.unitsEditText);
        unitsInputLayout = findViewById(R.id.unitsInputLayout);
        rebateRadioGroup = findViewById(R.id.rebateRadioGroup);
        resultsCard = findViewById(R.id.resultsCard);
        resultMonth = findViewById(R.id.resultMonth);
        resultTotalCharges = findViewById(R.id.resultTotalCharges);
        resultRebate = findViewById(R.id.resultRebate);
        resultFinalCost = findViewById(R.id.resultFinalCost);
        calculateButton = findViewById(R.id.calculateButton);
        saveButton = findViewById(R.id.saveButton);
        historyButton = findViewById(R.id.historyButton);
        aboutButton = findViewById(R.id.aboutButton);

        // Set default rebate to 0% (first radio button)
        rebateRadioGroup.check(R.id.rebate0);
        selectedRebate = 0;
    }

    private void setupMonthSpinner() {
        String[] months = {"Select Month", "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, months);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        monthSpinner.setAdapter(adapter);

        monthSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    selectedMonth = months[position];
                } else {
                    selectedMonth = "";
                }
                clearErrors();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedMonth = "";
            }
        });
    }

    private void setupRadioGroupListener() {
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
        });
    }

    private void setupInputValidation() {
        unitsEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                validateUnits();
            }
        });
    }

    private void setupButtonListeners() {
        calculateButton.setOnClickListener(v -> calculateBill());

        saveButton.setOnClickListener(v -> saveBill());

        historyButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, BillListActivity.class);
            startActivity(intent);
        });

        aboutButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AboutActivity.class);
            startActivity(intent);
        });
    }

    private boolean validateInputs() {
        boolean isValid = true;

        // Validate month
        if (selectedMonth.isEmpty()) {
            Toast.makeText(this, "Please select a month", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        // Validate units
        if (!validateUnits()) {
            isValid = false;
        }

        return isValid;
    }

    private boolean validateUnits() {
        String unitsStr = unitsEditText.getText().toString().trim();
        if (unitsStr.isEmpty()) {
            unitsInputLayout.setError("Please enter units between 1-1000 kWh");
            return false;
        }

        try {
            double units = Double.parseDouble(unitsStr);
            if (units < 1 || units > 1000) {
                unitsInputLayout.setError("Units must be 1-1000 kWh");
                return false;
            }
        } catch (NumberFormatException e) {
            unitsInputLayout.setError("Please enter a valid number");
            return false;
        }

        unitsInputLayout.setError(null);
        return true;
    }

    private void clearErrors() {
        unitsInputLayout.setError(null);
    }

    private void calculateBill() {
        if (!validateInputs()) {
            return;
        }

        try {
            double units = Double.parseDouble(unitsEditText.getText().toString().trim());

            // Calculate charges based on block rates
            calculatedTotalCharges = calculateCharges(units);
            calculatedFinalCost = calculatedTotalCharges - (calculatedTotalCharges * selectedRebate / 100);

            // Display results
            displayResults(units, selectedRebate);

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid input format", Toast.LENGTH_SHORT).show();
        }
    }

    private double calculateCharges(double units) {
        double charges = 0;

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

    private void displayResults(double units, double rebate) {
        DecimalFormat df = new DecimalFormat("#,##0.00");

        resultMonth.setText(selectedMonth);
        resultTotalCharges.setText("RM " + df.format(calculatedTotalCharges));
        resultRebate.setText(rebate + "%");
        resultFinalCost.setText("RM " + df.format(calculatedFinalCost));

        resultsCard.setVisibility(View.VISIBLE);
        saveButton.setVisibility(View.VISIBLE);
    }

    private void saveBill() {
        if (selectedMonth.isEmpty() || calculatedFinalCost == 0) {
            Toast.makeText(this, "Please calculate bill first", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double units = Double.parseDouble(unitsEditText.getText().toString().trim());

            BillModel bill = new BillModel();
            bill.setMonth(selectedMonth);
            bill.setUnits(units);
            bill.setRebate(selectedRebate);
            bill.setTotalCharges(calculatedTotalCharges);
            bill.setFinalCost(calculatedFinalCost);

            if (databaseHelper.addBill(bill)) {
                Toast.makeText(this, "Bill saved to history", Toast.LENGTH_SHORT).show();
                saveButton.setEnabled(false);
                saveButton.setText("Saved");
            } else {
                Toast.makeText(this, "Failed to save bill", Toast.LENGTH_SHORT).show();
            }

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid data", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        databaseHelper.close();
        super.onDestroy();
    }
}