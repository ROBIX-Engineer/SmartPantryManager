
package com.example.smartpantrymanager;

import android.app.Activity;
import android.os.Bundle;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.Locale;

public class RecipeDetailActivity extends Activity {

    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        databaseHelper = new DatabaseHelper(this);

        long recipeId = getIntent().getLongExtra("recipe_id", -1);

        ScrollView scrollView = new ScrollView(this);

        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setPadding(32, 32, 32, 32);

        TextView heading = new TextView(this);
        heading.setText("Recipe Details");
        heading.setTextSize(26);
        heading.setTypeface(null, Typeface.BOLD);
        heading.setPadding(0, 0, 0, 20);
        mainLayout.addView(heading);

        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        Cursor recipeCursor = db.query(
                DatabaseHelper.TABLE_RECIPES,
                null,
                DatabaseHelper.COL_RECIPE_ID + " = ?",
                new String[]{String.valueOf(recipeId)},
                null,
                null,
                null
        );

        boolean recipeFound = false;

        try {
            if (recipeCursor.moveToFirst()) {
                recipeFound = true;

                String recipeName = recipeCursor.getString(
                        recipeCursor.getColumnIndexOrThrow(
                                DatabaseHelper.COL_RECIPE_NAME
                        )
                );

                String description = recipeCursor.getString(
                        recipeCursor.getColumnIndexOrThrow(
                                DatabaseHelper.COL_RECIPE_DESCRIPTION
                        )
                );

                TextView nameView = new TextView(this);
                nameView.setText(recipeName);
                nameView.setTextSize(23);
                nameView.setTypeface(null, Typeface.BOLD);
                nameView.setPadding(0, 0, 0, 12);
                mainLayout.addView(nameView);

                if (description != null && !description.isEmpty()) {
                    TextView descriptionView = new TextView(this);
                    descriptionView.setText(description);
                    descriptionView.setTextSize(16);
                    descriptionView.setPadding(0, 0, 0, 24);
                    mainLayout.addView(descriptionView);
                }

                TextView ingredientsHeading = new TextView(this);
                ingredientsHeading.setText("Required Ingredients");
                ingredientsHeading.setTextSize(19);
                ingredientsHeading.setTypeface(null, Typeface.BOLD);
                ingredientsHeading.setPadding(0, 8, 0, 12);
                mainLayout.addView(ingredientsHeading);

                Cursor ingredientsCursor = db.query(
                        DatabaseHelper.TABLE_RECIPE_INGREDIENTS,
                        null,
                        DatabaseHelper.COL_RECIPE_INGREDIENT_RECIPE_ID + " = ?",
                        new String[]{String.valueOf(recipeId)},
                        null,
                        null,
                        DatabaseHelper.COL_RECIPE_INGREDIENT_NAME + " ASC"
                );

                try {
                    while (ingredientsCursor.moveToNext()) {
                        String ingredientName = ingredientsCursor.getString(
                                ingredientsCursor.getColumnIndexOrThrow(
                                        DatabaseHelper.COL_RECIPE_INGREDIENT_NAME
                                )
                        );

                        double quantity = ingredientsCursor.getDouble(
                                ingredientsCursor.getColumnIndexOrThrow(
                                        DatabaseHelper.COL_RECIPE_INGREDIENT_QUANTITY
                                )
                        );

                        String unit = ingredientsCursor.getString(
                                ingredientsCursor.getColumnIndexOrThrow(
                                        DatabaseHelper.COL_RECIPE_INGREDIENT_UNIT
                                )
                        );

                        TextView ingredientView = new TextView(this);
                        ingredientView.setText(
                                "\u2022 " + ingredientName + " - "
                                        + String.format(
                                        Locale.getDefault(),
                                        "%s",
                                        quantity
                                )
                                        + " " + unit
                        );
                        ingredientView.setTextSize(17);
                        ingredientView.setPadding(8, 8, 8, 8);
                        mainLayout.addView(ingredientView);
                    }
                } finally {
                    ingredientsCursor.close();
                }

                TextView instructionsHeading = new TextView(this);
                instructionsHeading.setText("Preparation");
                instructionsHeading.setTextSize(19);
                instructionsHeading.setTypeface(null, Typeface.BOLD);
                instructionsHeading.setPadding(0, 24, 0, 12);
                mainLayout.addView(instructionsHeading);

                TextView instructions = new TextView(this);
                instructions.setText(
                        "1. Prepare and measure all the required ingredients.\n\n"
                                + "2. Follow the recipe description above as a guide.\n\n"
                                + "3. Cook or combine the ingredients until ready to serve."
                );
                instructions.setTextSize(16);
                mainLayout.addView(instructions);
            }
        } finally {
            recipeCursor.close();
        }

        if (!recipeFound) {
            TextView errorMessage = new TextView(this);
            errorMessage.setText("Recipe not found. Please return and select another recipe.");
            errorMessage.setTextSize(17);
            mainLayout.addView(errorMessage);
        }

        scrollView.addView(
                mainLayout,
                new ScrollView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                )
        );

        setContentView(scrollView);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }
}
