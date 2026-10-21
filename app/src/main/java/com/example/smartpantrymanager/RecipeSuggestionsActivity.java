
package com.example.smartpantrymanager;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.Locale;

public class RecipeSuggestionsActivity extends Activity {

    private DatabaseHelper databaseHelper;
    private LinearLayout recipeContainer;

    private int dp(int value) {
        return (int) (value * getResources()
                .getDisplayMetrics().density + 0.5f);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        databaseHelper = new DatabaseHelper(this);

        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
        scrollView.setBackgroundColor(Color.rgb(246, 248, 243));

        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setPadding(dp(20), dp(24), dp(20), dp(24));

        TextView heading = new TextView(this);
        heading.setText("Recipe Suggestions");
        heading.setTextSize(26);
        heading.setTextColor(Color.rgb(36, 92, 67));
        heading.setTypeface(null, Typeface.BOLD);

        mainLayout.addView(heading);

        TextView explanation = new TextView(this);
        explanation.setText(
                "Discover recipes you can prepare using the " +
                        "ingredients currently available in your pantry."
        );
        explanation.setTextSize(15);
        explanation.setTextColor(Color.DKGRAY);
        explanation.setPadding(0, dp(8), 0, dp(20));

        mainLayout.addView(explanation);

        recipeContainer = new LinearLayout(this);
        recipeContainer.setOrientation(LinearLayout.VERTICAL);

        mainLayout.addView(
                recipeContainer,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                )
        );

        scrollView.addView(mainLayout);
        setContentView(scrollView);

        loadSuggestedRecipes();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (databaseHelper != null && recipeContainer != null) {
            loadSuggestedRecipes();
        }
    }

    private void loadSuggestedRecipes() {

        recipeContainer.removeAllViews();

        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        Cursor recipes = db.query(
                DatabaseHelper.TABLE_RECIPES,
                null,
                null,
                null,
                null,
                null,
                DatabaseHelper.COL_RECIPE_NAME + " ASC"
        );

        int suggestedCount = 0;
        int totalRecipes = 0;

        try {
            while (recipes.moveToNext()) {

                totalRecipes++;

                long recipeId = recipes.getLong(
                        recipes.getColumnIndexOrThrow(
                                DatabaseHelper.COL_RECIPE_ID
                        )
                );

                String recipeName = recipes.getString(
                        recipes.getColumnIndexOrThrow(
                                DatabaseHelper.COL_RECIPE_NAME
                        )
                );

                String description = recipes.getString(
                        recipes.getColumnIndexOrThrow(
                                DatabaseHelper.COL_RECIPE_DESCRIPTION
                        )
                );

                if (hasAllIngredients(db, recipeId)) {
                    addRecipeCard(
                            recipeId,
                            recipeName,
                            description
                    );
                    suggestedCount++;
                }
            }

        } finally {
            recipes.close();
        }

        TextView resultMessage = new TextView(this);
        resultMessage.setTextSize(15);
        resultMessage.setTextColor(Color.rgb(55, 75, 61));
        resultMessage.setPadding(
                dp(16), dp(16), dp(16), dp(16)
        );

        GradientDrawable messageBackground = new GradientDrawable();
        messageBackground.setColor(Color.WHITE);
        messageBackground.setCornerRadius(dp(12));
        resultMessage.setBackground(messageBackground);

        LinearLayout.LayoutParams messageParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        messageParams.setMargins(0, 0, 0, dp(16));

        if (totalRecipes == 0) {

            resultMessage.setText(
                    "No recipes have been added yet.\n\n" +
                            "Your recipe database is empty. Recipes must " +
                            "be saved in the database before suggestions " +
                            "can be displayed."
            );

        } else if (suggestedCount == 0) {

            resultMessage.setText(
                    "No matching recipes found yet.\n\n" +
                            "There are " + totalRecipes +
                            " recipe(s) in the database, but none currently " +
                            "matches all its required pantry ingredients, " +
                            "quantities and units.\n\n" +
                            "Try adding the required ingredients or " +
                            "checking the recipe ingredient records."
            );

        } else {

            resultMessage.setText(
                    "Great! You can prepare " + suggestedCount +
                            (suggestedCount == 1
                                    ? " recipe with your current ingredients."
                                    : " recipes with your current ingredients.")
            );
        }

        recipeContainer.addView(resultMessage, 0);
    }

    private boolean hasAllIngredients(
            SQLiteDatabase db,
            long recipeId) {

        Cursor requiredIngredients = db.query(
                DatabaseHelper.TABLE_RECIPE_INGREDIENTS,
                null,
                DatabaseHelper.COL_RECIPE_INGREDIENT_RECIPE_ID
                        + " = ?",
                new String[]{String.valueOf(recipeId)},
                null,
                null,
                null
        );

        boolean hasRequirements = false;
        boolean allAvailable = true;

        try {
            while (requiredIngredients.moveToNext()) {

                hasRequirements = true;

                String requiredName = requiredIngredients.getString(
                        requiredIngredients.getColumnIndexOrThrow(
                                DatabaseHelper.COL_RECIPE_INGREDIENT_NAME
                        )
                );

                double requiredQuantity =
                        requiredIngredients.getDouble(
                                requiredIngredients.getColumnIndexOrThrow(
                                        DatabaseHelper
                                                .COL_RECIPE_INGREDIENT_QUANTITY
                                )
                        );

                String requiredUnit = requiredIngredients.getString(
                        requiredIngredients.getColumnIndexOrThrow(
                                DatabaseHelper.COL_RECIPE_INGREDIENT_UNIT
                        )
                );

                if (requiredName == null || requiredUnit == null) {
                    allAvailable = false;
                    break;
                }

                String normalizedName = normalize(requiredName);
                String normalizedUnit = normalize(requiredUnit);

                if (normalizedName.isEmpty()
                        || normalizedUnit.isEmpty()
                        || requiredQuantity < 0) {
                    allAvailable = false;
                    break;
                }

                Cursor pantryItems = db.query(
                        DatabaseHelper.TABLE_INGREDIENTS,
                        new String[]{
                                DatabaseHelper.COL_NAME,
                                DatabaseHelper.COL_QUANTITY,
                                DatabaseHelper.COL_UNIT
                        },
                        "LOWER(TRIM("
                                + DatabaseHelper.COL_NAME
                                + ")) = ?",
                        new String[]{normalizedName},
                        null,
                        null,
                        null
                );

                boolean foundEnough = false;

                try {
                    while (pantryItems.moveToNext()) {

                        double pantryQuantity =
                                pantryItems.getDouble(
                                        pantryItems.getColumnIndexOrThrow(
                                                DatabaseHelper.COL_QUANTITY
                                        )
                                );

                        String pantryUnit =
                                pantryItems.getString(
                                        pantryItems.getColumnIndexOrThrow(
                                                DatabaseHelper.COL_UNIT
                                        )
                                );

                        if (pantryUnit != null
                                && normalize(pantryUnit)
                                .equals(normalizedUnit)
                                && pantryQuantity >= requiredQuantity) {

                            foundEnough = true;
                            break;
                        }
                    }

                } finally {
                    pantryItems.close();
                }

                if (!foundEnough) {
                    allAvailable = false;
                    break;
                }
            }

        } finally {
            requiredIngredients.close();
        }

        return hasRequirements && allAvailable;
    }

    private String normalize(String value) {
        return value == null
                ? ""
                : value.trim().toLowerCase(Locale.ROOT);
    }

    private void addRecipeCard(
            long recipeId,
            String name,
            String description) {

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(18), dp(18), dp(18), dp(18));
        card.setClickable(true);
        card.setFocusable(true);
        card.setElevation(dp(2));

        GradientDrawable cardBackground = new GradientDrawable();
        cardBackground.setColor(Color.WHITE);
        cardBackground.setCornerRadius(dp(14));
        cardBackground.setStroke(
                dp(1), Color.rgb(222, 232, 223)
        );
        card.setBackground(cardBackground);

        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        cardParams.setMargins(0, 0, 0, dp(14));
        card.setLayoutParams(cardParams);

        TextView nameView = new TextView(this);
        nameView.setText(name == null ? "Unnamed recipe" : name);
        nameView.setTextSize(19);
        nameView.setTextColor(Color.rgb(36, 92, 67));
        nameView.setTypeface(null, Typeface.BOLD);

        card.addView(nameView);

        if (description != null && !description.trim().isEmpty()) {

            TextView descriptionView = new TextView(this);
            descriptionView.setText(description);
            descriptionView.setTextSize(15);
            descriptionView.setTextColor(Color.DKGRAY);
            descriptionView.setPadding(0, dp(8), 0, dp(10));

            card.addView(descriptionView);
        }

        TextView tapMessage = new TextView(this);
        tapMessage.setText("Tap to view recipe details  →");
        tapMessage.setTextSize(14);
        tapMessage.setTextColor(Color.rgb(36, 92, 67));
        tapMessage.setGravity(Gravity.END);
        tapMessage.setPadding(0, dp(12), 0, 0);

        card.addView(tapMessage);

        card.setOnClickListener(view -> {

            Intent intent = new Intent(
                    RecipeSuggestionsActivity.this,
                    RecipeDetailActivity.class
            );

            intent.putExtra("recipe_id", recipeId);
            startActivity(intent);
        });

        recipeContainer.addView(card);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }
}
