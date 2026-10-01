package za.ac.richfield.smartpantry.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.Locale;

import za.ac.richfield.smartpantry.util.IngredientNormalizer;
import za.ac.richfield.smartpantry.util.UnitConverter;

/**
 * Owns the versioned SQLite schema and inserts the fixed recipe catalogue on first creation.
 */
public final class PantryDbHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "smart_pantry.db";
    public static final int DATABASE_VERSION = 1;

    public static final String TABLE_INGREDIENTS = "ingredients";
    public static final String TABLE_ALIASES = "ingredient_aliases";
    public static final String TABLE_PANTRY = "pantry_items";
    public static final String TABLE_RECIPES = "recipes";
    public static final String TABLE_RECIPE_INGREDIENTS = "recipe_ingredients";

    public static final String COL_ID = "id";
    public static final String COL_CANONICAL_NAME = "canonical_name";
    public static final String COL_DISPLAY_NAME = "display_name";
    public static final String COL_ALIAS_KEY = "alias_key";
    public static final String COL_INGREDIENT_ID = "ingredient_id";
    public static final String COL_QUANTITY = "quantity";
    public static final String COL_UNIT = "unit_code";
    public static final String COL_EXPIRY_DATE = "expiry_date";
    public static final String COL_RECIPE_ID = "recipe_id";
    public static final String COL_REQUIRED_QUANTITY = "required_quantity";
    public static final String COL_NAME = "name";
    public static final String COL_METHOD = "method_text";

    public PantryDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        // Android does not enable SQLite foreign-key checks for every connection by default.
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_INGREDIENTS + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_CANONICAL_NAME + " TEXT NOT NULL UNIQUE COLLATE NOCASE, "
                + COL_DISPLAY_NAME + " TEXT NOT NULL)");

        db.execSQL("CREATE TABLE " + TABLE_ALIASES + " ("
                + COL_ALIAS_KEY + " TEXT PRIMARY KEY COLLATE NOCASE, "
                + COL_INGREDIENT_ID + " INTEGER NOT NULL, "
                + "FOREIGN KEY(" + COL_INGREDIENT_ID + ") REFERENCES "
                + TABLE_INGREDIENTS + "(" + COL_ID + ") ON DELETE CASCADE)");

        db.execSQL("CREATE TABLE " + TABLE_PANTRY + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_INGREDIENT_ID + " INTEGER NOT NULL, "
                + COL_QUANTITY + " REAL NOT NULL CHECK(" + COL_QUANTITY + " > 0), "
                + COL_UNIT + " TEXT NOT NULL, "
                + COL_EXPIRY_DATE + " TEXT, "
                + "created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP, "
                + "updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP, "
                + "FOREIGN KEY(" + COL_INGREDIENT_ID + ") REFERENCES "
                + TABLE_INGREDIENTS + "(" + COL_ID + "))");

        db.execSQL("CREATE TABLE " + TABLE_RECIPES + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_NAME + " TEXT NOT NULL UNIQUE, "
                + COL_METHOD + " TEXT NOT NULL)");

        db.execSQL("CREATE TABLE " + TABLE_RECIPE_INGREDIENTS + " ("
                + COL_RECIPE_ID + " INTEGER NOT NULL, "
                + COL_INGREDIENT_ID + " INTEGER NOT NULL, "
                + COL_REQUIRED_QUANTITY + " REAL NOT NULL CHECK("
                + COL_REQUIRED_QUANTITY + " > 0), "
                + COL_UNIT + " TEXT NOT NULL, "
                + "PRIMARY KEY(" + COL_RECIPE_ID + ", " + COL_INGREDIENT_ID + "), "
                + "FOREIGN KEY(" + COL_RECIPE_ID + ") REFERENCES " + TABLE_RECIPES
                + "(" + COL_ID + ") ON DELETE CASCADE, "
                + "FOREIGN KEY(" + COL_INGREDIENT_ID + ") REFERENCES "
                + TABLE_INGREDIENTS + "(" + COL_ID + "))");

        db.execSQL("CREATE INDEX idx_pantry_ingredient ON " + TABLE_PANTRY
                + "(" + COL_INGREDIENT_ID + ")");
        db.execSQL("CREATE INDEX idx_recipe_ingredient_recipe ON "
                + TABLE_RECIPE_INGREDIENTS + "(" + COL_RECIPE_ID + ")");

        seedAliases(db);
        seedRecipes(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Version 1 is the first release. Future upgrades must migrate data here.
    }

    long ensureIngredient(SQLiteDatabase db, String rawName) {
        String canonicalName = IngredientNormalizer.normalize(rawName);
        if (canonicalName.isEmpty()) {
            throw new IllegalArgumentException("Ingredient name cannot be empty");
        }

        try (Cursor cursor = db.query(
                TABLE_ALIASES,
                new String[]{COL_INGREDIENT_ID},
                COL_ALIAS_KEY + " = ?",
                new String[]{canonicalName},
                null,
                null,
                null,
                "1")) {
            if (cursor.moveToFirst()) {
                return cursor.getLong(cursor.getColumnIndexOrThrow(COL_INGREDIENT_ID));
            }
        }

        ContentValues ingredientValues = new ContentValues();
        ingredientValues.put(COL_CANONICAL_NAME, canonicalName);
        ingredientValues.put(COL_DISPLAY_NAME, toDisplayName(canonicalName));
        long ingredientId = db.insertWithOnConflict(
                TABLE_INGREDIENTS,
                null,
                ingredientValues,
                SQLiteDatabase.CONFLICT_IGNORE);

        if (ingredientId == -1) {
            try (Cursor cursor = db.query(
                    TABLE_INGREDIENTS,
                    new String[]{COL_ID},
                    COL_CANONICAL_NAME + " = ?",
                    new String[]{canonicalName},
                    null,
                    null,
                    null,
                    "1")) {
                if (!cursor.moveToFirst()) {
                    throw new IllegalStateException("Unable to resolve ingredient " + rawName);
                }
                ingredientId = cursor.getLong(cursor.getColumnIndexOrThrow(COL_ID));
            }
        }

        ContentValues aliasValues = new ContentValues();
        aliasValues.put(COL_ALIAS_KEY, canonicalName);
        aliasValues.put(COL_INGREDIENT_ID, ingredientId);
        db.insertWithOnConflict(
                TABLE_ALIASES,
                null,
                aliasValues,
                SQLiteDatabase.CONFLICT_IGNORE);
        return ingredientId;
    }

    private void seedAliases(SQLiteDatabase db) {
        seedAlias(db, "tomatoes", "tomato");
        seedAlias(db, "potatoes", "potato");
        seedAlias(db, "eggs", "egg");
        seedAlias(db, "bread slices", "bread slice");
        seedAlias(db, "garlic cloves", "garlic clove");
        seedAlias(db, "capsicum", "bell pepper");
        seedAlias(db, "bell peppers", "bell pepper");
        seedAlias(db, "chickpeas", "chickpea");
        seedAlias(db, "garbanzo beans", "chickpea");
    }

    private void seedAlias(SQLiteDatabase db, String alias, String canonical) {
        long ingredientId = ensureIngredient(db, canonical);
        ContentValues values = new ContentValues();
        values.put(COL_ALIAS_KEY, IngredientNormalizer.normalize(alias));
        values.put(COL_INGREDIENT_ID, ingredientId);
        db.insertWithOnConflict(TABLE_ALIASES, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    private void seedRecipes(SQLiteDatabase db) {
        // Seed data belongs in onCreate so reopening an existing database cannot duplicate recipes.
        seedRecipe(db, "Scrambled Eggs",
                "1. Beat the eggs with the milk.\n2. Melt the butter over medium heat.\n"
                        + "3. Add the egg mixture and stir gently until just set.",
                req("egg", 2, "count"), req("milk", 30, "ml"), req("butter", 10, "g"));

        seedRecipe(db, "Cheese Omelette",
                "1. Beat the eggs.\n2. Melt the butter and add the eggs.\n"
                        + "3. Sprinkle over the cheese, fold and cook until set.",
                req("egg", 2, "count"), req("cheese", 40, "g"), req("butter", 10, "g"));

        seedRecipe(db, "Tomato Omelette",
                "1. Chop and soften the tomato in oil.\n2. Add beaten eggs.\n"
                        + "3. Cook until set and fold before serving.",
                req("egg", 2, "count"), req("tomato", 1, "count"), req("oil", 10, "ml"));

        seedRecipe(db, "Fried Egg Toast",
                "1. Toast the bread.\n2. Heat the oil and fry the egg.\n"
                        + "3. Place the egg on the toast and serve.",
                req("egg", 1, "count"), req("bread slice", 2, "count"), req("oil", 5, "ml"));

        seedRecipe(db, "Cheese Toastie",
                "1. Put the cheese between the bread slices.\n2. Butter the outside.\n"
                        + "3. Toast both sides in a pan until golden.",
                req("bread slice", 2, "count"), req("cheese", 50, "g"), req("butter", 10, "g"));

        seedRecipe(db, "Tomato Cheese Toast",
                "1. Toast the bread lightly.\n2. Add sliced tomato and cheese.\n"
                        + "3. Heat until the cheese melts.",
                req("bread slice", 2, "count"), req("tomato", 1, "count"), req("cheese", 40, "g"));

        seedRecipe(db, "Peanut Butter Banana Toast",
                "1. Toast the bread.\n2. Spread with peanut butter.\n"
                        + "3. Add sliced banana and serve.",
                req("bread slice", 2, "count"), req("peanut butter", 30, "g"), req("banana", 1, "count"));

        seedRecipe(db, "Tuna Mayo Sandwich",
                "1. Mix the tuna and mayonnaise.\n2. Spread onto one bread slice.\n"
                        + "3. Top with the second slice and cut in half.",
                req("bread slice", 2, "count"), req("tuna", 120, "g"), req("mayonnaise", 30, "g"));

        seedRecipe(db, "Banana Oatmeal",
                "1. Simmer the oats in milk until soft.\n2. Slice the banana.\n"
                        + "3. Stir half through and use the rest on top.",
                req("oats", 50, "g"), req("milk", 200, "ml"), req("banana", 1, "count"));

        seedRecipe(db, "Apple Oatmeal",
                "1. Simmer the oats in milk.\n2. Chop the apple finely.\n"
                        + "3. Stir the apple through and cook until tender.",
                req("oats", 50, "g"), req("milk", 200, "ml"), req("apple", 1, "count"));

        seedRecipe(db, "Simple Pancakes",
                "1. Whisk the flour, egg and milk into a smooth batter.\n"
                        + "2. Heat the oil.\n3. Cook small portions until golden on both sides.",
                req("flour", 100, "g"), req("egg", 1, "count"), req("milk", 150, "ml"), req("oil", 10, "ml"));

        seedRecipe(db, "Simple Flatbread",
                "1. Mix flour, water and oil.\n2. Knead briefly and divide.\n"
                        + "3. Roll thinly and cook in a dry pan.",
                req("flour", 150, "g"), req("water", 90, "ml"), req("oil", 10, "ml"));

        seedRecipe(db, "Tomato Pasta",
                "1. Cook the dry pasta.\n2. Chop and soften the tomatoes in oil.\n"
                        + "3. Drain the pasta and combine with the sauce.",
                req("pasta", 100, "g"), req("tomato", 2, "count"), req("oil", 10, "ml"));

        seedRecipe(db, "Tuna Pasta",
                "1. Cook and drain the dry pasta.\n2. Mix the tuna and mayonnaise.\n"
                        + "3. Combine with the pasta and serve.",
                req("pasta", 100, "g"), req("tuna", 120, "g"), req("mayonnaise", 30, "g"));

        seedRecipe(db, "Chicken Tomato Pasta",
                "1. Cook the dry pasta.\n2. Cook the chicken in oil.\n"
                        + "3. Add chopped tomato, then combine with pasta.",
                req("chicken", 200, "g"), req("pasta", 100, "g"), req("tomato", 2, "count"), req("oil", 10, "ml"));

        seedRecipe(db, "Chicken Rice Bowl",
                "1. Cook the dry rice.\n2. Cut and cook the chicken in oil.\n"
                        + "3. Serve the chicken over the rice.",
                req("chicken", 200, "g"), req("rice", 100, "g"), req("oil", 10, "ml"));

        seedRecipe(db, "Egg Fried Rice",
                "1. Cook the dry rice and allow it to cool slightly.\n2. Scramble the egg in oil.\n"
                        + "3. Add the rice and soy sauce, then stir-fry.",
                req("rice", 100, "g"), req("egg", 1, "count"), req("oil", 10, "ml"), req("soy sauce", 15, "ml"));

        seedRecipe(db, "Tomato Rice",
                "1. Cook the dry rice.\n2. Fry chopped onion and tomato in oil.\n"
                        + "3. Stir the cooked rice through the vegetables.",
                req("rice", 100, "g"), req("tomato", 2, "count"), req("onion", 1, "count"), req("oil", 10, "ml"));

        seedRecipe(db, "Mashed Potatoes",
                "1. Peel, cut and boil the potatoes.\n2. Drain thoroughly.\n"
                        + "3. Mash with the milk and butter until smooth.",
                req("potato", 300, "g"), req("milk", 60, "ml"), req("butter", 20, "g"));

        seedRecipe(db, "Roast Potatoes",
                "1. Cut the potatoes into even pieces.\n2. Coat with oil.\n"
                        + "3. Roast until tender and crisp.",
                req("potato", 300, "g"), req("oil", 15, "ml"));
    }

    private Requirement req(String name, double quantity, String unit) {
        return new Requirement(name, quantity, unit);
    }

    private void seedRecipe(SQLiteDatabase db, String name, String method, Requirement... requirements) {
        ContentValues recipeValues = new ContentValues();
        recipeValues.put(COL_NAME, name);
        recipeValues.put(COL_METHOD, method);
        long recipeId = db.insertWithOnConflict(
                TABLE_RECIPES,
                null,
                recipeValues,
                SQLiteDatabase.CONFLICT_IGNORE);

        if (recipeId == -1) {
            try (Cursor cursor = db.query(
                    TABLE_RECIPES,
                    new String[]{COL_ID},
                    COL_NAME + " = ?",
                    new String[]{name},
                    null,
                    null,
                    null,
                    "1")) {
                if (!cursor.moveToFirst()) {
                    throw new IllegalStateException("Unable to seed recipe " + name);
                }
                recipeId = cursor.getLong(cursor.getColumnIndexOrThrow(COL_ID));
            }
        }

        for (Requirement requirement : requirements) {
            ContentValues values = new ContentValues();
            values.put(COL_RECIPE_ID, recipeId);
            values.put(COL_INGREDIENT_ID, ensureIngredient(db, requirement.name));
            values.put(COL_REQUIRED_QUANTITY, requirement.quantity);
            values.put(COL_UNIT, UnitConverter.normalizeUnit(requirement.unit));
            db.insertWithOnConflict(
                    TABLE_RECIPE_INGREDIENTS,
                    null,
                    values,
                    SQLiteDatabase.CONFLICT_REPLACE);
        }
    }

    private String toDisplayName(String canonical) {
        String[] words = canonical.split(" ");
        StringBuilder builder = new StringBuilder();
        for (String word : words) {
            if (word.isEmpty()) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append(word.substring(0, 1).toUpperCase(Locale.ROOT));
            builder.append(word.substring(1));
        }
        return builder.toString();
    }

    private static final class Requirement {
        private final String name;
        private final double quantity;
        private final String unit;

        private Requirement(String name, double quantity, String unit) {
            this.name = name;
            this.quantity = quantity;
            this.unit = unit;
        }
    }
}
