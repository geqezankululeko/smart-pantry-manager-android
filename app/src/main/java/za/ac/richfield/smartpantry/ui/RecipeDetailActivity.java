package za.ac.richfield.smartpantry.ui;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;

import za.ac.richfield.smartpantry.R;
import za.ac.richfield.smartpantry.data.PantryRepository;
import za.ac.richfield.smartpantry.model.Recipe;
import za.ac.richfield.smartpantry.model.RecipeIngredient;
import za.ac.richfield.smartpantry.util.DisplayFormatters;

/**
 * Resolves a recipe identifier from an explicit Intent and renders its full requirements and method.
 */
public final class RecipeDetailActivity extends BaseActivity {
    public static final String EXTRA_RECIPE_ID = "extra_recipe_id";

    private PantryRepository repository;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.recipe_detail);
        }

        repository = new PantryRepository(this);
        long recipeId = getIntent().getLongExtra(EXTRA_RECIPE_ID, -1);
        Recipe recipe = repository.getRecipe(recipeId);
        if (recipe == null) {
            Toast.makeText(this, R.string.record_not_found, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        TextView name = findViewById(R.id.recipe_detail_name);
        TextView ingredients = findViewById(R.id.recipe_detail_ingredients);
        TextView method = findViewById(R.id.recipe_detail_method);
        name.setText(recipe.getName());
        ingredients.setText(buildIngredientList(recipe));
        method.setText(recipe.getMethod());
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        repository.close();
        super.onDestroy();
    }

    private String buildIngredientList(Recipe recipe) {
        StringBuilder result = new StringBuilder();
        for (RecipeIngredient ingredient : recipe.getIngredients()) {
            result.append("• ")
                    .append(ingredient.getName())
                    .append(": ")
                    .append(DisplayFormatters.quantity(ingredient.getQuantity()))
                    .append(' ')
                    .append(ingredient.getUnit())
                    .append('\n');
        }
        return result.toString().trim();
    }
}
