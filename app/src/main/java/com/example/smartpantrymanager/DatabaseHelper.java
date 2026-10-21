
package com.example.smartpantrymanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "smart_pantry.db";
    private static final int DATABASE_VERSION = 3;

    public static final String TABLE_INGREDIENTS = "ingredients";
    public static final String COL_ID = "id";
    public static final String COL_NAME = "name";
    public static final String COL_QUANTITY = "quantity";
    public static final String COL_UNIT = "unit";
    public static final String COL_EXPIRY_DATE = "expiry_date";

    public static final String TABLE_RECIPES = "recipes";
    public static final String COL_RECIPE_ID = "id";
    public static final String COL_RECIPE_NAME = "name";
    public static final String COL_RECIPE_DESCRIPTION = "description";
    public static final String COL_RECIPE_INSTRUCTIONS = "instructions";

    public static final String TABLE_RECIPE_INGREDIENTS =
            "recipe_ingredients";
    public static final String COL_RECIPE_INGREDIENT_ID = "id";
    public static final String COL_RECIPE_INGREDIENT_RECIPE_ID = "recipe_id";
    public static final String COL_RECIPE_INGREDIENT_NAME = "ingredient_name";
    public static final String COL_RECIPE_INGREDIENT_QUANTITY = "quantity";
    public static final String COL_RECIPE_INGREDIENT_UNIT = "unit";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createIngredientsTable(db);
        createRecipeTables(db);
        seedRecipes(db);
    }

    @Override
    public void onUpgrade(
            SQLiteDatabase db,
            int oldVersion,
            int newVersion) {

        if (oldVersion < 2) {
            createRecipeTables(db);
        }

        if (oldVersion < 3) {
            seedRecipes(db);
        }
    }

    private void createIngredientsTable(SQLiteDatabase db) {
        String sql = "CREATE TABLE " + TABLE_INGREDIENTS + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_NAME + " TEXT NOT NULL, " +
                COL_QUANTITY + " REAL NOT NULL, " +
                COL_UNIT + " TEXT NOT NULL, " +
                COL_EXPIRY_DATE + " TEXT)";
        db.execSQL(sql);
    }

    private void createRecipeTables(SQLiteDatabase db) {
        String recipesSql = "CREATE TABLE IF NOT EXISTS " +
                TABLE_RECIPES + " (" +
                COL_RECIPE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_RECIPE_NAME + " TEXT NOT NULL, " +
                COL_RECIPE_DESCRIPTION + " TEXT, " +
                COL_RECIPE_INSTRUCTIONS + " TEXT NOT NULL)";

        db.execSQL(recipesSql);

        String ingredientsSql = "CREATE TABLE IF NOT EXISTS " +
                TABLE_RECIPE_INGREDIENTS + " (" +
                COL_RECIPE_INGREDIENT_ID +
                " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_RECIPE_INGREDIENT_RECIPE_ID + " INTEGER NOT NULL, " +
                COL_RECIPE_INGREDIENT_NAME + " TEXT NOT NULL, " +
                COL_RECIPE_INGREDIENT_QUANTITY + " REAL NOT NULL, " +
                COL_RECIPE_INGREDIENT_UNIT + " TEXT NOT NULL, " +
                "FOREIGN KEY (" + COL_RECIPE_INGREDIENT_RECIPE_ID +
                ") REFERENCES " + TABLE_RECIPES +
                "(" + COL_RECIPE_ID + ") ON DELETE CASCADE)";

        db.execSQL(ingredientsSql);
    }

    // Add a pantry ingredient
    public boolean insertIngredient(
            String name, double quantity, String unit, String expiryDate) {

        ContentValues values = new ContentValues();
        values.put(COL_NAME, name);
        values.put(COL_QUANTITY, quantity);
        values.put(COL_UNIT, unit);
        values.put(COL_EXPIRY_DATE, expiryDate);

        return getWritableDatabase().insert(
                TABLE_INGREDIENTS, null, values) != -1;
    }

    // Get all pantry ingredients
    public Cursor getAllIngredients() {
        return getReadableDatabase().query(
                TABLE_INGREDIENTS, null, null, null, null, null,
                COL_NAME + " ASC");
    }

    // Update a pantry ingredient
    public boolean updateIngredient(
            int ingredientId, String name, double quantity,
            String unit, String expiryDate) {

        ContentValues values = new ContentValues();
        values.put(COL_NAME, name);
        values.put(COL_QUANTITY, quantity);
        values.put(COL_UNIT, unit);
        values.put(COL_EXPIRY_DATE, expiryDate);

        return getWritableDatabase().update(
                TABLE_INGREDIENTS, values,
                COL_ID + " = ?",
                new String[]{String.valueOf(ingredientId)}) > 0;
    }

    // Delete a pantry ingredient
    public boolean deleteIngredient(int ingredientId) {
        return getWritableDatabase().delete(
                TABLE_INGREDIENTS,
                COL_ID + " = ?",
                new String[]{String.valueOf(ingredientId)}) > 0;
    }

    // Insert a recipe
    private long insertRecipe(
            SQLiteDatabase db, String name,
            String description, String instructions) {

        ContentValues values = new ContentValues();
        values.put(COL_RECIPE_NAME, name);
        values.put(COL_RECIPE_DESCRIPTION, description);
        values.put(COL_RECIPE_INSTRUCTIONS, instructions);

        return db.insert(TABLE_RECIPES, null, values);
    }

    // Insert one ingredient required by a recipe
    private boolean insertRecipeIngredient(
            SQLiteDatabase db, long recipeId,
            String ingredientName, double quantity, String unit) {

        ContentValues values = new ContentValues();
        values.put(COL_RECIPE_INGREDIENT_RECIPE_ID, recipeId);
        values.put(COL_RECIPE_INGREDIENT_NAME, ingredientName);
        values.put(COL_RECIPE_INGREDIENT_QUANTITY, quantity);
        values.put(COL_RECIPE_INGREDIENT_UNIT, unit);

        return db.insert(TABLE_RECIPE_INGREDIENTS, null, values) != -1;
    }

    // Add a recipe and all its required ingredients
    private void addRecipe(
            SQLiteDatabase db, String name, String description,
            String instructions, String[][] ingredients) {

        long recipeId = insertRecipe(db, name, description, instructions);

        if (recipeId == -1) {
            return;
        }

        for (String[] ingredient : ingredients) {
            insertRecipeIngredient(
                    db,
                    recipeId,
                    ingredient[0],
                    Double.parseDouble(ingredient[1]),
                    ingredient[2]
            );
        }
    }

    // Add the sample recipes only when the database is first created
    // or upgraded to version 3.
    private void seedRecipes(SQLiteDatabase db) {
        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + TABLE_RECIPES, null);

        boolean alreadySeeded = false;

        try {
            if (cursor.moveToFirst()) {
                alreadySeeded = cursor.getInt(0) > 0;
            }
        } finally {
            cursor.close();
        }

        if (alreadySeeded) {
            return;
        }

        db.beginTransaction();

        try {
            addRecipe(db, "Tomato Omelette",
                    "Eggs cooked with fresh tomato.",
                    "Chop the tomato. Beat the eggs, mix with tomato, " +
                            "and cook in a lightly oiled pan.",
                    new String[][]{
                            {"egg", "2", "pcs"},
                            {"tomato", "1", "pcs"}
                    });

            addRecipe(db, "Cheese Omelette",
                    "A simple cheesy omelette.",
                    "Beat the eggs, add cheese, and cook in a pan.",
                    new String[][]{
                            {"egg", "2", "pcs"},
                            {"cheese", "30", "g"}
                    });

            addRecipe(db, "Tomato Pasta",
                    "Pasta with a quick tomato sauce.",
                    "Cook the pasta. Cook chopped tomatoes in a pan " +
                            "and combine with the pasta.",
                    new String[][]{
                            {"pasta", "100", "g"},
                            {"tomato", "2", "pcs"}
                    });

            addRecipe(db, "Garlic Pasta",
                    "Pasta with garlic and olive oil.",
                    "Cook the pasta. Gently cook chopped garlic in " +
                            "olive oil and toss with the pasta.",
                    new String[][]{
                            {"pasta", "100", "g"},
                            {"garlic", "2", "cloves"},
                            {"olive oil", "1", "tbsp"}
                    });

            addRecipe(db, "Potato Mash",
                    "Creamy mashed potatoes.",
                    "Boil potatoes until soft, mash them, and mix " +
                            "with milk and butter.",
                    new String[][]{
                            {"potato", "2", "pcs"},
                            {"milk", "50", "ml"},
                            {"butter", "1", "tbsp"}
                    });

            addRecipe(db, "Banana Pancakes",
                    "Easy banana and egg pancakes.",
                    "Mash the banana, mix with egg and flour, " +
                            "then cook small pancakes in a pan.",
                    new String[][]{
                            {"banana", "1", "pcs"},
                            {"egg", "1", "pcs"},
                            {"flour", "50", "g"}
                    });

            addRecipe(db, "French Toast",
                    "Golden egg-soaked bread.",
                    "Beat the egg with milk. Dip the bread in the " +
                            "mixture and fry until golden.",
                    new String[][]{
                            {"bread", "2", "slices"},
                            {"egg", "1", "pcs"},
                            {"milk", "50", "ml"}
                    });

            addRecipe(db, "Cheese Sandwich",
                    "A quick bread and cheese sandwich.",
                    "Place cheese between slices of bread. Serve " +
                            "as is or toast until golden.",
                    new String[][]{
                            {"bread", "2", "slices"},
                            {"cheese", "2", "slices"}
                    });

            addRecipe(db, "Tomato Sandwich",
                    "A fresh tomato sandwich.",
                    "Slice the tomato and place it between slices " +
                            "of bread.",
                    new String[][]{
                            {"bread", "2", "slices"},
                            {"tomato", "1", "pcs"}
                    });

            addRecipe(db, "Fried Rice",
                    "Rice fried with egg and carrot.",
                    "Cook the rice. Fry chopped carrot, add egg, " +
                            "then stir in the rice until hot.",
                    new String[][]{
                            {"rice", "150", "g"},
                            {"egg", "1", "pcs"},
                            {"carrot", "1", "pcs"}
                    });

            addRecipe(db, "Carrot Soup",
                    "A warming carrot soup.",
                    "Cook chopped carrots and onion in stock until " +
                            "soft, then blend until smooth.",
                    new String[][]{
                            {"carrot", "2", "pcs"},
                            {"onion", "1", "pcs"},
                            {"stock", "250", "ml"}
                    });

            addRecipe(db, "Potato Soup",
                    "A simple potato soup.",
                    "Simmer chopped potatoes and onion in stock " +
                            "until soft, then mash or blend.",
                    new String[][]{
                            {"potato", "2", "pcs"},
                            {"onion", "1", "pcs"},
                            {"stock", "250", "ml"}
                    });

            addRecipe(db, "Banana Smoothie",
                    "A creamy banana smoothie.",
                    "Blend the banana and milk until smooth.",
                    new String[][]{
                            {"banana", "1", "pcs"},
                            {"milk", "250", "ml"}
                    });

            addRecipe(db, "Apple Smoothie",
                    "A refreshing apple smoothie.",
                    "Core and chop the apple, then blend with milk.",
                    new String[][]{
                            {"apple", "1", "pcs"},
                            {"milk", "200", "ml"}
                    });

            addRecipe(db, "Garlic Mashed Potatoes",
                    "Mashed potatoes with garlic.",
                    "Boil potatoes until soft. Mash with butter " +
                            "and finely chopped garlic.",
                    new String[][]{
                            {"potato", "2", "pcs"},
                            {"garlic", "1", "clove"},
                            {"butter", "1", "tbsp"}
                    });

            addRecipe(db, "Carrot Rice",
                    "Rice cooked with carrot.",
                    "Cook the rice. Finely chop and cook the carrot, " +
                            "then mix with the rice.",
                    new String[][]{
                            {"rice", "150", "g"},
                            {"carrot", "1", "pcs"}
                    });

            addRecipe(db, "Tomato Egg Rice",
                    "Rice with tomato and egg.",
                    "Cook the rice. Fry chopped tomato, add egg, " +
                            "then mix in the rice.",
                    new String[][]{
                            {"rice", "150", "g"},
                            {"egg", "1", "pcs"},
                            {"tomato", "1", "pcs"}
                    });

            addRecipe(db, "Apple Pancakes",
                    "Pancakes made with apple.",
                    "Grate the apple and mix with flour, egg, and " +
                            "milk. Cook small pancakes in a pan.",
                    new String[][]{
                            {"apple", "1", "pcs"},
                            {"flour", "50", "g"},
                            {"egg", "1", "pcs"},
                            {"milk", "50", "ml"}
                    });

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }
}
