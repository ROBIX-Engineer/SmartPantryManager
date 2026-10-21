# Smart Pantry Manager

Smart Pantry Manager is a Java-based Android application for recording
ingredients available at home and suggesting recipes that can be
prepared using ingredients already in the user's pantry. Its aim is to
support meal planning and reduce avoidable food waste.

## Features

-   Add pantry ingredients with a name, quantity, unit and expiry date.
-   View and search saved pantry ingredients.
-   Edit and delete pantry records.
-   Display expiry information, including expired and expiring-soon
    statuses.
-   View recipe suggestions and open recipe details.
-   Adjust the expiring-soon alert preference in Settings.
-   Store pantry and recipe data locally using SQLite.

## Technology

-   **Language:** Java
-   **IDE:** Android Studio
-   **Database:** SQLite through Android's `SQLiteOpenHelper` API
-   **UI:** Android XML layouts and Java-created views
-   **Version control:** Git and GitHub

SQLite is appropriate for this project because pantry data is local to
the user and should remain available after the app closes, without
requiring a separate server.

## Project screens

-   **Pantry (`MainActivity`)** --- lists and searches pantry
    ingredients and provides Add, Edit, Delete and Find Recipes actions.
-   **Add Ingredient (`AddIngredientActivity`)** --- records a new
    pantry item.
-   **Edit Ingredient (`EditIngredientActivity`)** --- updates an
    existing pantry item.
-   **Recipe Suggestions (`RecipeSuggestionsActivity`)** --- checks
    recipes against pantry ingredients.
-   **Recipe Details (`RecipeDetailActivity`)** --- shows the selected
    recipe's information and method.
-   **Settings (`SettingsActivity`)** --- stores the expiring-soon alert
    preference.

## Recipe matching

The suggestions screen is intended to show a recipe only when all
required ingredients are present in sufficient quantities. The shared
implementation checks ingredient names without regard to case, requires
the units to match, and checks that pantry quantity is at least the
required quantity.

**Known limitation:** the code shared during development uses exact
ingredient-name matching apart from letter case. It does not normalize
singular/plural names or convert between units. Verify this against your
final code.

## Requirements

-   Android Studio with the Android SDK installed.
-   An Android emulator or Android device.
-   The complete project source code.

## How to open and run

1.  Clone or download this repository.
2.  Open Android Studio and choose **Open**.
3.  Select the project folder containing `settings.gradle` or
    `settings.gradle.kts`.
4.  Allow Gradle to sync and install any SDK components requested.
5.  Start an emulator or connect an Android device.
6.  Click **Run** (the green triangle).
7.  Test the pantry, CRUD actions, search, expiry labels, recipe
    suggestions, recipe details and Settings.

If the project does not build, inspect the first error in Android
Studio's Build window and verify that the required SDK and Gradle
configuration are installed.

## Repository

GitHub: https://github.com/ROBIX-Engineer/SmartPantryManager

## Testing checklist

-   [ ] Add an ingredient and confirm it appears in the pantry.
-   [ ] Edit an ingredient and confirm the changes are saved.
-   [ ] Delete an ingredient and confirm it is removed.
-   [ ] Search for an existing and a non-existing ingredient.
-   [ ] Test empty/invalid form input and capture the validation
    message.
-   [ ] Restart the app and confirm pantry data persists.
-   [ ] Add all ingredients required by a known recipe and confirm it
    appears in suggestions.
-   [ ] Remove one required ingredient or reduce its quantity and
    confirm the recipe disappears from strict suggestions.
-   [ ] Open recipe details.
-   [ ] Change the Settings preference and check whether it persists.

## Items to verify before submission

-   The assignment requires a `RecyclerView` or `ListView` with a custom
    adapter. The shared `MainActivity` code creates ingredient cards in
    a `LinearLayout`; check whether you have implemented a
    RecyclerView/ListView elsewhere.
-   Confirm the seeded recipe collection meets the assignment's
    requirement for 15--20 recipes.
-   Confirm the repository URL above is the one you will submit.
