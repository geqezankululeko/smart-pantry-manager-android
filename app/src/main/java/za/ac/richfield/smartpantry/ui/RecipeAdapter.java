package za.ac.richfield.smartpantry.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import za.ac.richfield.smartpantry.R;
import za.ac.richfield.smartpantry.model.Recipe;
import za.ac.richfield.smartpantry.model.RecipeIngredient;
import za.ac.richfield.smartpantry.util.DisplayFormatters;

/**
 * Binds matched recipes to the suggestion list and forwards detail selections.
 */
public final class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {
    public interface RecipeClickListener {
        void onRecipeClick(Recipe recipe);
    }

    private final List<Recipe> recipes = new ArrayList<>();
    private final RecipeClickListener listener;

    public RecipeAdapter(RecipeClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<Recipe> newRecipes) {
        recipes.clear();
        recipes.addAll(newRecipes);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recipe, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        Recipe recipe = recipes.get(position);
        holder.name.setText(recipe.getName());
        holder.ingredients.setText(buildIngredientSummary(recipe));
        holder.itemView.setContentDescription(holder.itemView.getContext().getString(
                R.string.open_recipe_content_description,
                recipe.getName()));
        holder.itemView.setOnClickListener(view -> listener.onRecipeClick(recipe));
    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }

    private String buildIngredientSummary(Recipe recipe) {
        StringBuilder summary = new StringBuilder();
        for (RecipeIngredient ingredient : recipe.getIngredients()) {
            if (summary.length() > 0) {
                summary.append(" • ");
            }
            summary.append(ingredient.getName())
                    .append(' ')
                    .append(DisplayFormatters.quantity(ingredient.getQuantity()))
                    .append(' ')
                    .append(ingredient.getUnit());
        }
        return summary.toString();
    }

    static final class RecipeViewHolder extends RecyclerView.ViewHolder {
        private final TextView name;
        private final TextView ingredients;

        private RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.recipe_name);
            ingredients = itemView.findViewById(R.id.recipe_ingredient_summary);
        }
    }
}
