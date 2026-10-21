package com.example.smartpantrymanager;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.database.Cursor;

public class EditIngredientActivity extends Activity {

    private EditText edtIngredientName;
    private EditText edtIngredientQuantity;
    private EditText edtExpiryDate;
    private Spinner spinnerIngredientUnit;
    private DatabaseHelper databaseHelper;
    private int ingredientId = -1;

    private final String[] units = {
            "Select unit", "pieces", "g", "kg",
            "ml", "litres", "cups", "tablespoons", "teaspoons"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_ingredient);

        databaseHelper = new DatabaseHelper(this);

        edtIngredientName = findViewById(R.id.edtIngredientName);
        edtIngredientQuantity = findViewById(R.id.edtIngredientQuantity);
        spinnerIngredientUnit = findViewById(R.id.spinnerIngredientUnit);
        edtExpiryDate = findViewById(R.id.edtExpiryDate);

        Button btnSave = findViewById(R.id.btnSaveIngredient);
        Button btnCancel = findViewById(R.id.btnCancelIngredient);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                units
        );
        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );
        spinnerIngredientUnit.setAdapter(adapter);

        ingredientId = getIntent().getIntExtra("ingredient_id", -1);

        if (ingredientId == -1) {
            Toast.makeText(this, "Ingredient not selected",
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadIngredient();

        btnSave.setText("Update Ingredient");
        btnSave.setOnClickListener(v -> updateIngredient());

        btnCancel.setOnClickListener(v -> finish());
    }

    private void loadIngredient() {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        Cursor cursor = db.query(
                DatabaseHelper.TABLE_INGREDIENTS,
                new String[]{
                        DatabaseHelper.COL_NAME,
                        DatabaseHelper.COL_QUANTITY,
                        DatabaseHelper.COL_UNIT,
                        DatabaseHelper.COL_EXPIRY_DATE
                },
                DatabaseHelper.COL_ID + " = ?",
                new String[]{String.valueOf(ingredientId)},
                null, null, null
        );

        try {
            if (cursor.moveToFirst()) {
                edtIngredientName.setText(cursor.getString(0));
                edtIngredientQuantity.setText(
                        String.valueOf(cursor.getDouble(1))
                );

                String unit = cursor.getString(2);
                for (int i = 0; i < units.length; i++) {
                    if (units[i].equals(unit)) {
                        spinnerIngredientUnit.setSelection(i);
                        break;
                    }
                }

                String expiryDate = cursor.getString(3);
                edtExpiryDate.setText(
                        expiryDate == null ? "" : expiryDate
                );
            } else {
                Toast.makeText(this, "Ingredient not found",
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        } finally {
            cursor.close();
        }
    }

    private void updateIngredient() {
        String name = edtIngredientName.getText().toString().trim();
        String quantityText =
                edtIngredientQuantity.getText().toString().trim();
        String unit = spinnerIngredientUnit.getSelectedItem().toString();
        String expiryDate =
                edtExpiryDate.getText().toString().trim();

        if (name.isEmpty()) {
            edtIngredientName.setError("Enter an ingredient name");
            edtIngredientName.requestFocus();
            return;
        }

        if (quantityText.isEmpty()) {
            edtIngredientQuantity.setError("Enter a quantity");
            edtIngredientQuantity.requestFocus();
            return;
        }

        double quantity;

        try {
            quantity = Double.parseDouble(quantityText);
            if (quantity <= 0) {
                edtIngredientQuantity.setError(
                        "Quantity must be greater than zero"
                );
                edtIngredientQuantity.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            edtIngredientQuantity.setError("Enter a valid number");
            edtIngredientQuantity.requestFocus();
            return;
        }

        if ("Select unit".equals(unit)) {
            Toast.makeText(this, "Please select a unit",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_NAME, name);
        values.put(DatabaseHelper.COL_QUANTITY, quantity);
        values.put(DatabaseHelper.COL_UNIT, unit);

        if (expiryDate.isEmpty()) {
            values.putNull(DatabaseHelper.COL_EXPIRY_DATE);
        } else {
            values.put(DatabaseHelper.COL_EXPIRY_DATE, expiryDate);
        }

        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        int rowsUpdated = db.update(
                DatabaseHelper.TABLE_INGREDIENTS,
                values,
                DatabaseHelper.COL_ID + " = ?",
                new String[]{String.valueOf(ingredientId)}
        );

        if (rowsUpdated > 0) {
            Toast.makeText(this, "Ingredient updated successfully",
                    Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Could not update ingredient",
                    Toast.LENGTH_SHORT).show();
        }
    }
}
