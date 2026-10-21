package com.example.smartpantrymanager;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;

    private LinearLayout ingredientListContainer;
    private TextView txtIngredientCount;
    private TextView txtEmptyPantry;
    private EditText edtSearchPantry;
    private Button btnAddIngredient;
    private Button btnFindRecipes;

    private TextView navPantry;
    private TextView navRecipes;
    private TextView navSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        databaseHelper = new DatabaseHelper(this);

        // Connect pantry views
        ingredientListContainer =
                findViewById(R.id.ingredientListContainer);
        txtIngredientCount =
                findViewById(R.id.txtIngredientCount);
        txtEmptyPantry =
                findViewById(R.id.txtEmptyPantry);
        edtSearchPantry =
                findViewById(R.id.edtSearchPantry);
        btnAddIngredient =
                findViewById(R.id.btnAddIngredient);

        // Connect bottom navigation
        navPantry = findViewById(R.id.navPantry);
        navRecipes = findViewById(R.id.navRecipes);
        navSettings = findViewById(R.id.navSettings);

        // Pantry navigation: return to the main pantry screen
        navPantry.setOnClickListener(view -> {
            Intent intent = new Intent(
                    MainActivity.this,
                    MainActivity.class
            );

            intent.addFlags(
                    Intent.FLAG_ACTIVITY_CLEAR_TOP
                            | Intent.FLAG_ACTIVITY_SINGLE_TOP
            );

            startActivity(intent);
        });

        // Recipes navigation
        navRecipes.setOnClickListener(view -> {
            Intent intent = new Intent(
                    MainActivity.this,
                    RecipeSuggestionsActivity.class
            );
            startActivity(intent);
        });

        // Settings navigation
        navSettings.setOnClickListener(view -> {
            Intent intent = new Intent(
                    MainActivity.this,
                    SettingsActivity.class
            );
            startActivity(intent);
        });

        // Add ingredient button
        btnAddIngredient.setOnClickListener(view -> {
            Intent intent = new Intent(
                    MainActivity.this,
                    AddIngredientActivity.class
            );
            startActivity(intent);
        });

        // Create the Find Recipes button programmatically
        // This preserves compatibility with the existing XML.
        btnFindRecipes = new Button(this);
        btnFindRecipes.setText("Find Recipes");
        btnFindRecipes.setAllCaps(false);

        LinearLayout.LayoutParams recipeButtonParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        recipeButtonParams.setMargins(
                dp(8), dp(4), dp(8), dp(12)
        );

        if (btnAddIngredient.getParent() instanceof LinearLayout) {
            LinearLayout parent =
                    (LinearLayout) btnAddIngredient.getParent();

            int addButtonIndex =
                    parent.indexOfChild(btnAddIngredient);

            parent.addView(
                    btnFindRecipes,
                    addButtonIndex + 1,
                    recipeButtonParams
            );
        }

        btnFindRecipes.setOnClickListener(view -> {
            Intent intent = new Intent(
                    MainActivity.this,
                    RecipeSuggestionsActivity.class
            );
            startActivity(intent);
        });

        // Search ingredients as the user types
        edtSearchPantry.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(
                    CharSequence s, int start,
                    int count, int after) {
            }

            @Override
            public void onTextChanged(
                    CharSequence s, int start,
                    int before, int count) {
                displayIngredients(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        displayIngredients("");
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (databaseHelper != null
                && ingredientListContainer != null
                && edtSearchPantry != null) {
            displayIngredients(
                    edtSearchPantry.getText().toString()
            );
        }
    }

    private void displayIngredients(String searchText) {

        ingredientListContainer.removeAllViews();

        Cursor cursor = databaseHelper.getAllIngredients();
        int displayedCount = 0;

        String search = searchText == null
                ? ""
                : searchText.trim().toLowerCase(Locale.ROOT);

        try {
            while (cursor != null && cursor.moveToNext()) {

                int ingredientId = cursor.getInt(
                        cursor.getColumnIndexOrThrow(
                                DatabaseHelper.COL_ID
                        )
                );

                String name = cursor.getString(
                        cursor.getColumnIndexOrThrow(
                                DatabaseHelper.COL_NAME
                        )
                );

                double quantity = cursor.getDouble(
                        cursor.getColumnIndexOrThrow(
                                DatabaseHelper.COL_QUANTITY
                        )
                );

                String unit = cursor.getString(
                        cursor.getColumnIndexOrThrow(
                                DatabaseHelper.COL_UNIT
                        )
                );

                String expiryDate = cursor.getString(
                        cursor.getColumnIndexOrThrow(
                                DatabaseHelper.COL_EXPIRY_DATE
                        )
                );

                if (name == null
                        || !name.toLowerCase(Locale.ROOT)
                        .contains(search)) {
                    continue;
                }

                displayedCount++;

                // Ingredient card
                LinearLayout card = new LinearLayout(this);
                card.setOrientation(LinearLayout.VERTICAL);
                card.setPadding(
                        dp(16), dp(12), dp(16), dp(12)
                );

                GradientDrawable cardBackground =
                        new GradientDrawable();

                cardBackground.setColor(Color.WHITE);
                cardBackground.setCornerRadius(dp(12));
                cardBackground.setStroke(dp(1), Color.LTGRAY);

                card.setBackground(cardBackground);
                card.setElevation(dp(2));

                LinearLayout.LayoutParams cardParams =
                        new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        );

                cardParams.setMargins(
                        dp(8), dp(6), dp(8), dp(6)
                );

                ingredientListContainer.addView(
                        card, cardParams
                );

                // Ingredient name
                TextView txtName = new TextView(this);
                txtName.setText(name);
                txtName.setTextSize(20);
                txtName.setTypeface(null, Typeface.BOLD);
                txtName.setTextColor(Color.BLACK);
                card.addView(txtName);

                // Quantity and unit
                TextView txtQuantity = new TextView(this);
                txtQuantity.setText(
                        "Quantity: "
                                + formatQuantity(quantity)
                                + " " + unit
                );
                txtQuantity.setTextSize(16);
                txtQuantity.setTextColor(Color.DKGRAY);

                LinearLayout.LayoutParams quantityParams =
                        new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        );

                quantityParams.topMargin = dp(6);
                card.addView(txtQuantity, quantityParams);

                // Expiry information
                TextView txtExpiry = new TextView(this);
                txtExpiry.setText(getExpiryStatus(expiryDate));
                txtExpiry.setTextColor(getExpiryColor(expiryDate));
                txtExpiry.setTextSize(14);

                LinearLayout.LayoutParams expiryParams =
                        new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        );

                expiryParams.topMargin = dp(4);
                card.addView(txtExpiry, expiryParams);

                // Edit and Delete row
                LinearLayout buttonRow = new LinearLayout(this);
                buttonRow.setOrientation(LinearLayout.HORIZONTAL);

                LinearLayout.LayoutParams rowParams =
                        new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        );

                rowParams.topMargin = dp(12);
                card.addView(buttonRow, rowParams);

                // Edit button
                Button btnEdit = new Button(this);
                btnEdit.setText("Edit");

                LinearLayout.LayoutParams editParams =
                        new LinearLayout.LayoutParams(
                                0,
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                1f
                        );

                editParams.setMargins(0, 0, dp(4), 0);
                buttonRow.addView(btnEdit, editParams);

                btnEdit.setOnClickListener(view -> {
                    Intent intent = new Intent(
                            MainActivity.this,
                            EditIngredientActivity.class
                    );

                    intent.putExtra(
                            "ingredient_id", ingredientId
                    );

                    startActivity(intent);
                });

                // Delete button
                Button btnDelete = new Button(this);
                btnDelete.setText("Delete");

                LinearLayout.LayoutParams deleteParams =
                        new LinearLayout.LayoutParams(
                                0,
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                1f
                        );

                deleteParams.setMargins(dp(4), 0, 0, 0);
                buttonRow.addView(btnDelete, deleteParams);

                btnDelete.setOnClickListener(view ->
                        confirmDeleteIngredient(
                                ingredientId, name
                        )
                );
            }

        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        txtIngredientCount.setText(
                "Ingredients: " + displayedCount
        );

        if (displayedCount == 0) {
            txtEmptyPantry.setVisibility(View.VISIBLE);

            if (search.isEmpty()) {
                txtEmptyPantry.setText(
                        "Your pantry is empty. "
                                + "Add an ingredient to get started."
                );
            } else {
                txtEmptyPantry.setText(
                        "No ingredients found matching \""
                                + searchText + "\"."
                );
            }
        } else {
            txtEmptyPantry.setVisibility(View.GONE);
        }
    }

    private void confirmDeleteIngredient(
            int ingredientId, String name) {

        new AlertDialog.Builder(MainActivity.this)
                .setTitle("Delete Ingredient")
                .setMessage(
                        "Are you sure you want to delete \""
                                + name + "\"?"
                )
                .setPositiveButton("Delete", (dialog, which) -> {

                    boolean deleted =
                            databaseHelper.deleteIngredient(
                                    ingredientId
                            );

                    if (deleted) {
                        Toast.makeText(
                                MainActivity.this,
                                name + " deleted successfully.",
                                Toast.LENGTH_SHORT
                        ).show();
                    } else {
                        Toast.makeText(
                                MainActivity.this,
                                "Could not delete the ingredient.",
                                Toast.LENGTH_SHORT
                        ).show();
                    }

                    displayIngredients(
                            edtSearchPantry.getText().toString()
                    );
                })
                .setNegativeButton("Cancel", (dialog, which) ->
                        dialog.dismiss()
                )
                .show();
    }

    private String getExpiryStatus(String expiryDate) {

        if (expiryDate == null
                || expiryDate.trim().isEmpty()) {
            return "Expiry date: Not provided";
        }

        SimpleDateFormat dateFormat =
                new SimpleDateFormat(
                        "yyyy-MM-dd", Locale.getDefault()
                );

        dateFormat.setLenient(false);

        try {
            Date expiry = dateFormat.parse(expiryDate);

            if (expiry == null) {
                return "Expiry date: " + expiryDate;
            }

            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);

            Calendar expiryCalendar = Calendar.getInstance();
            expiryCalendar.setTime(expiry);
            expiryCalendar.set(Calendar.HOUR_OF_DAY, 0);
            expiryCalendar.set(Calendar.MINUTE, 0);
            expiryCalendar.set(Calendar.SECOND, 0);
            expiryCalendar.set(Calendar.MILLISECOND, 0);

            long differenceMillis =
                    expiryCalendar.getTimeInMillis()
                            - today.getTimeInMillis();

            long daysRemaining =
                    differenceMillis / (24 * 60 * 60 * 1000);

            if (daysRemaining < 0) {
                return "Expired";
            } else if (daysRemaining == 0) {
                return "Expires today";
            } else if (daysRemaining == 1) {
                return "Expires tomorrow";
            } else if (daysRemaining <= 7) {
                return "Expiring in " + daysRemaining + " days";
            } else {
                return "Fresh — expires in "
                        + daysRemaining + " days";
            }

        } catch (ParseException e) {
            return "Expiry date: " + expiryDate;
        }
    }

    private int getExpiryColor(String expiryDate) {

        String status = getExpiryStatus(expiryDate);

        if (status.equals("Expired")) {
            return Color.RED;
        } else if (status.equals("Expires today")
                || status.equals("Expires tomorrow")
                || status.startsWith("Expiring in")) {
            return Color.rgb(200, 120, 0);
        } else {
            return Color.rgb(0, 120, 0);
        }
    }

    private String formatQuantity(double quantity) {
        if (quantity == Math.floor(quantity)) {
            return String.valueOf((long) quantity);
        }

        return String.valueOf(quantity);
    }

    private int dp(int value) {
        return (int) (value * getResources()
                .getDisplayMetrics().density + 0.5f);
    }
}
