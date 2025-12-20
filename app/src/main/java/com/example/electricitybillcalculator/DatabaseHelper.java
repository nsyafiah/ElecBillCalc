package com.example.electricitybillcalculator;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "electricity_bills.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_BILLS = "bills";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_MONTH = "month";
    private static final String COLUMN_UNITS = "units";
    private static final String COLUMN_REBATE = "rebate";
    private static final String COLUMN_TOTAL_CHARGES = "total_charges";
    private static final String COLUMN_FINAL_COST = "final_cost";
    private static final String COLUMN_TIMESTAMP = "timestamp";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_BILLS_TABLE = "CREATE TABLE " + TABLE_BILLS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_MONTH + " TEXT,"
                + COLUMN_UNITS + " REAL,"
                + COLUMN_REBATE + " REAL,"
                + COLUMN_TOTAL_CHARGES + " REAL,"
                + COLUMN_FINAL_COST + " REAL,"
                + COLUMN_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP"
                + ")";
        db.execSQL(CREATE_BILLS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BILLS);
        onCreate(db);
    }

    // Add new bill
    public boolean addBill(BillModel bill) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_MONTH, bill.getMonth());
        values.put(COLUMN_UNITS, bill.getUnits());
        values.put(COLUMN_REBATE, bill.getRebate());
        values.put(COLUMN_TOTAL_CHARGES, bill.getTotalCharges());
        values.put(COLUMN_FINAL_COST, bill.getFinalCost());

        long result = db.insert(TABLE_BILLS, null, values);
        db.close();

        return result != -1;
    }

    // Get all bills
    public ArrayList<BillModel> getAllBills() {
        ArrayList<BillModel> billList = new ArrayList<>();
        String query = "SELECT * FROM " + TABLE_BILLS + " ORDER BY " + COLUMN_TIMESTAMP + " DESC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                BillModel bill = new BillModel();
                bill.setId(cursor.getInt(0));
                bill.setMonth(cursor.getString(1));
                bill.setUnits(cursor.getDouble(2));
                bill.setRebate(cursor.getDouble(3));
                bill.setTotalCharges(cursor.getDouble(4));
                bill.setFinalCost(cursor.getDouble(5));
                bill.setTimestamp(cursor.getString(6));

                billList.add(bill);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return billList;
    }

    // Get bill by ID
    public BillModel getBillById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_BILLS,
                new String[]{COLUMN_ID, COLUMN_MONTH, COLUMN_UNITS, COLUMN_REBATE,
                        COLUMN_TOTAL_CHARGES, COLUMN_FINAL_COST, COLUMN_TIMESTAMP},
                COLUMN_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null);

        BillModel bill = null;
        if (cursor != null && cursor.moveToFirst()) {
            bill = new BillModel(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getDouble(2),
                    cursor.getDouble(3),
                    cursor.getDouble(4),
                    cursor.getDouble(5),
                    cursor.getString(6)
            );
            cursor.close();
        }
        db.close();
        return bill;
    }

    // Update bill
    public boolean updateBill(BillModel bill) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_MONTH, bill.getMonth());
        values.put(COLUMN_UNITS, bill.getUnits());
        values.put(COLUMN_REBATE, bill.getRebate());
        values.put(COLUMN_TOTAL_CHARGES, bill.getTotalCharges());
        values.put(COLUMN_FINAL_COST, bill.getFinalCost());

        int result = db.update(TABLE_BILLS, values,
                COLUMN_ID + "=?", new String[]{String.valueOf(bill.getId())});
        db.close();

        return result > 0;
    }

    // Delete bill
    public boolean deleteBill(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_BILLS, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
        db.close();

        return result > 0;
    }
}