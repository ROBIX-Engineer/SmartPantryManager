
package com.example.smartpantrymanager;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AddIngredientActivity extends AppCompatActivity {

    private EditText edtIngredientName;
    private EditText edtIngredientQuantity;
    private EditText edtExpiryDate;
    private Spinner spinnerIngredientUnit;
    private Button btnSaveIngredient;
    private Button btnCancelIngredient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_ingredient);

        edtIngredientName = findViewById(R.id.edtIngredientName);
        edtIngredientQuantity = findViewById(R.id.edtIngredientQuantity);
        edtExpiryDate = findViewById(R.id.edtExpiryDate);
        spinnerIngredientUnit = findViewById(R.id.spinnerIngredientUnit);
        btnSaveIngredient = findViewById(R.id.btnSaveIngredient);
        btnCancelIngredient = findViewById(R.id.btnCancelIngredient);

        String[] units = {
                "Select unit", "pieces", "g", "kg",
                "ml", "litres", "cups", "tablespoons", "teaspoons"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                units
        );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spinnerIngredientUnit.setAdapter(adapter);

        btnSaveIngredient.setOnClickListener(v -> {
            String name = edtIngredientName.getText()
                    .toString().trim();

            String quantityText = edtIngredientQuantity.getText()
                    .toString().trim();

            String unit = spinnerIngredientUnit
                    .getSelectedItem().toString();

            String expiryDate = edtExpiryDate.getText()
                    .toString().trim();

            if (name.isEmpty()) {
                edtIngredientName.setError(
                        "Enter an ingredient name"
                );
                edtIngredientName.requestFocus();
                return;
            }

            if (quantityText.isEmpty()) {
                edtIngredientQuantity.setError(
                        "Enter a quantity"
                );
                edtIngredientQuantity.requestFocus();
                return;
            }

            double quantity;

            try {
                quantity = Double.parseDouble(quantityText);
            } catch (NumberFormatException e) {
                edtIngredientQuantity.setError(
                        "Enter a valid quantity"
                );
                edtIngredientQuantity.requestFocus();
                return;
            }

            if (!Double.isFinite(quantity) || quantity <= 0) {
                edtIngredientQuantity.setError(
                        "Quantity must be greater than zero"
                );
                edtIngredientQuantity.requestFocus();
                return;
            }

            if (spinnerIngredientUnit.getSelectedItemPosition() == 0) {
                Toast.makeText(
                        this,
                        "Please select a unit",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            DatabaseHelper databaseHelper =
                    new DatabaseHelper(this);

            boolean saved = databaseHelper.insertIngredient(
                    name,
                    quantity,
                    unit,
                    expiryDate
            );

            databaseHelper.close();

            if (saved) {
                Toast.makeText(
                        this,
                        "Ingredient saved successfully!",
                        Toast.LENGTH_SHORT
                ).show();

                finish();
            } else {
                Toast.makeText(
                        this,
                        "Could not save ingredient. Please try again.",
                        Toast.LENGTH_LONG
                ).show();
            }
        });

        btnCancelIngredient.setOnClickListener(v -> finish());
    }
}
