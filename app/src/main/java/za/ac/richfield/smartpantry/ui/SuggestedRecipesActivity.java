package za.ac.richfield.smartpantry.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import za.ac.richfield.smartpantry.R;
import za.ac.richfield.smartpantry.data.PantryRepository;
import za.ac.richfield.smartpantry.model.PantryItem;
import za.ac.richfield.smartpantry.model.Recipe;
import za.ac.richfield.smartpantry.util.RecipeMatcher;

/**
 * Shows only recipes that pass strict ingredient, unit and quantity checks.
 */
public final class SuggestedRecipesActivity extends BaseActivity {
    private PantryRepository repository;
    private RecipeAdapter adapter;
    private TextView emptyText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggested_recipes);

        repository = new PantryRepository(this);
        emptyText = findViewById(R.id.no_suggestions_text);
        adapter = new RecipeAdapter(recipe -> {
            Intent intent = new Intent(this, RecipeDetailActivity.class);
            intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID, recipe.getId());
            startActivity(intent);
        });

        RecyclerView recyclerView = findViewById(R.id.recipe_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        configureBottomNavigation(R.id.nav_recipes);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshSuggestions();
    }

    @Override
    protected void onDestroy() {
        repository.close();
        super.onDestroy();
    }

    private void refreshSuggestions() {
        List<PantryItem> pantryItems = repository.getAllPantryItems();
        List<Recipe> matches = new ArrayList<>();
        for (Recipe recipe : repository.getAllRecipes()) {
            if (RecipeMatcher.canMake(recipe, pantryItems)) {
                matches.add(recipe);
            }
        }
        adapter.submitList(matches);
        emptyText.setVisibility(matches.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
