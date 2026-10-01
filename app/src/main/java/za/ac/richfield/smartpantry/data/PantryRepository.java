package za.ac.richfield.smartpantry.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import za.ac.richfield.smartpantry.model.PantryItem;
import za.ac.richfield.smartpantry.model.Recipe;
import za.ac.richfield.smartpantry.model.RecipeIngredient;
import za.ac.richfield.smartpantry.util.UnitConverter;

/**
 * Provides the single data-access boundary used by the Activities.
 * SQL details remain outside the presentation layer and Cursors are always closed promptly.
 */
public final class PantryRepository {
    private final PantryDbHelper helper;

    public PantryRepository(Context context) {
        helper = new PantryDbHelper(context.getApplicationContext());
    }

    public long createPantryItem(String name, double quantity, String unit, String expiryDate) {
        SQLiteDatabase db = helper.getWritableDatabase();
        long ingredientId = helper.ensureIngredient(db, name);
        ContentValues values = pantryValues(ingredientId, quantity, unit, expiryDate);
        return db.insertOrThrow(PantryDbHelper.TABLE_PANTRY, null, values);
    }

    public boolean updatePantryItem(long pantryId, String name, double quantity,
                                    String unit, String expiryDate) {
        SQLiteDatabase db = helper.getWritableDatabase();
        long ingredientId = helper.ensureIngredient(db, name);
        ContentValues values = pantryValues(ingredientId, quantity, unit, expiryDate);
        values.put("updated_at", new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss",
                Locale.ROOT).format(new Date()));
        return db.update(
                PantryDbHelper.TABLE_PANTRY,
                values,
                PantryDbHelper.COL_ID + " = ?",
                new String[]{String.valueOf(pantryId)}) == 1;
    }

    public boolean deletePantryItem(long pantryId) {
        return helper.getWritableDatabase().delete(
                PantryDbHelper.TABLE_PANTRY,
                PantryDbHelper.COL_ID + " = ?",
                new String[]{String.valueOf(pantryId)}) == 1;
    }

    public PantryItem getPantryItem(long pantryId) {
        String sql = "SELECT p." + PantryDbHelper.COL_ID + ", i."
                + PantryDbHelper.COL_DISPLAY_NAME + ", p." + PantryDbHelper.COL_QUANTITY
                + ", p." + PantryDbHelper.COL_UNIT + ", p." + PantryDbHelper.COL_EXPIRY_DATE
                + " FROM " + PantryDbHelper.TABLE_PANTRY + " p JOIN "
                + PantryDbHelper.TABLE_INGREDIENTS + " i ON i." + PantryDbHelper.COL_ID
                + " = p." + PantryDbHelper.COL_INGREDIENT_ID
                + " WHERE p." + PantryDbHelper.COL_ID + " = ?";

        try (Cursor cursor = helper.getReadableDatabase().rawQuery(
                sql,
                new String[]{String.valueOf(pantryId)})) {
            return cursor.moveToFirst() ? readPantryItem(cursor) : null;
        }
    }

    public List<PantryItem> getAllPantryItems() {
        List<PantryItem> items = new ArrayList<>();
        String sql = "SELECT p." + PantryDbHelper.COL_ID + ", i."
                + PantryDbHelper.COL_DISPLAY_NAME + ", p." + PantryDbHelper.COL_QUANTITY
                + ", p." + PantryDbHelper.COL_UNIT + ", p." + PantryDbHelper.COL_EXPIRY_DATE
                + " FROM " + PantryDbHelper.TABLE_PANTRY + " p JOIN "
                + PantryDbHelper.TABLE_INGREDIENTS + " i ON i." + PantryDbHelper.COL_ID
                + " = p." + PantryDbHelper.COL_INGREDIENT_ID
                + " ORDER BY i." + PantryDbHelper.COL_DISPLAY_NAME + " COLLATE NOCASE";

        try (Cursor cursor = helper.getReadableDatabase().rawQuery(sql, null)) {
            while (cursor.moveToNext()) {
                items.add(readPantryItem(cursor));
            }
        }
        return items;
    }

    public List<Recipe> getAllRecipes() {
        // The catalogue is intentionally small (20 recipes); each recipe is hydrated with its requirements.
        List<Recipe> recipes = new ArrayList<>();
        try (Cursor cursor = helper.getReadableDatabase().query(
                PantryDbHelper.TABLE_RECIPES,
                new String[]{PantryDbHelper.COL_ID, PantryDbHelper.COL_NAME, PantryDbHelper.COL_METHOD},
                null,
                null,
                null,
                null,
                PantryDbHelper.COL_NAME + " COLLATE NOCASE")) {
            while (cursor.moveToNext()) {
                long recipeId = cursor.getLong(cursor.getColumnIndexOrThrow(PantryDbHelper.COL_ID));
                recipes.add(new Recipe(
                        recipeId,
                        cursor.getString(cursor.getColumnIndexOrThrow(PantryDbHelper.COL_NAME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(PantryDbHelper.COL_METHOD)),
                        getRecipeIngredients(recipeId)));
            }
        }
        return recipes;
    }

    public Recipe getRecipe(long recipeId) {
        try (Cursor cursor = helper.getReadableDatabase().query(
                PantryDbHelper.TABLE_RECIPES,
                new String[]{PantryDbHelper.COL_ID, PantryDbHelper.COL_NAME, PantryDbHelper.COL_METHOD},
                PantryDbHelper.COL_ID + " = ?",
                new String[]{String.valueOf(recipeId)},
                null,
                null,
                null,
                "1")) {
            if (!cursor.moveToFirst()) {
                return null;
            }
            return new Recipe(
                    recipeId,
                    cursor.getString(cursor.getColumnIndexOrThrow(PantryDbHelper.COL_NAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(PantryDbHelper.COL_METHOD)),
                    getRecipeIngredients(recipeId));
        }
    }

    public int getRecipeCount() {
        try (Cursor cursor = helper.getReadableDatabase().rawQuery(
                "SELECT COUNT(*) FROM " + PantryDbHelper.TABLE_RECIPES,
                null)) {
            cursor.moveToFirst();
            return cursor.getInt(0);
        }
    }

    public void close() {
        helper.close();
    }

    private List<RecipeIngredient> getRecipeIngredients(long recipeId) {
        List<RecipeIngredient> ingredients = new ArrayList<>();
        String sql = "SELECT ri.rowid AS requirement_id, ri." + PantryDbHelper.COL_RECIPE_ID
                + ", i." + PantryDbHelper.COL_DISPLAY_NAME + ", ri."
                + PantryDbHelper.COL_REQUIRED_QUANTITY + ", ri." + PantryDbHelper.COL_UNIT
                + " FROM " + PantryDbHelper.TABLE_RECIPE_INGREDIENTS + " ri JOIN "
                + PantryDbHelper.TABLE_INGREDIENTS + " i ON i." + PantryDbHelper.COL_ID
                + " = ri." + PantryDbHelper.COL_INGREDIENT_ID
                + " WHERE ri." + PantryDbHelper.COL_RECIPE_ID + " = ?"
                + " ORDER BY i." + PantryDbHelper.COL_DISPLAY_NAME + " COLLATE NOCASE";

        try (Cursor cursor = helper.getReadableDatabase().rawQuery(
                sql,
                new String[]{String.valueOf(recipeId)})) {
            while (cursor.moveToNext()) {
                ingredients.add(new RecipeIngredient(
                        cursor.getLong(cursor.getColumnIndexOrThrow("requirement_id")),
                        cursor.getLong(cursor.getColumnIndexOrThrow(PantryDbHelper.COL_RECIPE_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(PantryDbHelper.COL_DISPLAY_NAME)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(PantryDbHelper.COL_REQUIRED_QUANTITY)),
                        cursor.getString(cursor.getColumnIndexOrThrow(PantryDbHelper.COL_UNIT))));
            }
        }
        return ingredients;
    }

    private ContentValues pantryValues(long ingredientId, double quantity,
                                        String unit, String expiryDate) {
        ContentValues values = new ContentValues();
        values.put(PantryDbHelper.COL_INGREDIENT_ID, ingredientId);
        values.put(PantryDbHelper.COL_QUANTITY, quantity);
        values.put(PantryDbHelper.COL_UNIT, UnitConverter.normalizeUnit(unit));
        if (expiryDate == null || expiryDate.trim().isEmpty()) {
            values.putNull(PantryDbHelper.COL_EXPIRY_DATE);
        } else {
            values.put(PantryDbHelper.COL_EXPIRY_DATE, expiryDate);
        }
        return values;
    }

    private PantryItem readPantryItem(Cursor cursor) {
        int expiryIndex = cursor.getColumnIndexOrThrow(PantryDbHelper.COL_EXPIRY_DATE);
        return new PantryItem(
                cursor.getLong(cursor.getColumnIndexOrThrow(PantryDbHelper.COL_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(PantryDbHelper.COL_DISPLAY_NAME)),
                cursor.getDouble(cursor.getColumnIndexOrThrow(PantryDbHelper.COL_QUANTITY)),
                cursor.getString(cursor.getColumnIndexOrThrow(PantryDbHelper.COL_UNIT)),
                cursor.isNull(expiryIndex) ? null : cursor.getString(expiryIndex));
    }
}
