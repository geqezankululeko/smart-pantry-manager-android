package za.ac.richfield.smartpantry.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.List;

import za.ac.richfield.smartpantry.R;
import za.ac.richfield.smartpantry.data.PantryRepository;
import za.ac.richfield.smartpantry.model.PantryItem;

/**
 * Displays persistent pantry records and routes create, edit and delete actions.
 */
public final class PantryActivity extends BaseActivity implements PantryAdapter.PantryActions {
    public static final String PREFS_NAME = "smart_pantry_preferences";
    public static final String PREF_SHOW_EXPIRY = "show_expiry_warnings";
    public static final String PREF_DEFAULT_UNIT = "default_unit";

    private PantryRepository repository;
    private PantryAdapter adapter;
    private TextView emptyText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantry);

        repository = new PantryRepository(this);
        adapter = new PantryAdapter(this);
        emptyText = findViewById(R.id.empty_pantry_text);

        RecyclerView recyclerView = findViewById(R.id.pantry_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        ExtendedFloatingActionButton addButton = findViewById(R.id.add_ingredient_button);
        addButton.setOnClickListener(view -> startActivity(
                new Intent(this, AddEditIngredientActivity.class)));

        configureBottomNavigation(R.id.nav_pantry);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshPantry();
    }

    @Override
    public void onEdit(PantryItem item) {
        Intent intent = new Intent(this, AddEditIngredientActivity.class);
        intent.putExtra(AddEditIngredientActivity.EXTRA_PANTRY_ID, item.getId());
        startActivity(intent);
    }

    @Override
    public void onDelete(PantryItem item) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_item_title)
                .setMessage(getString(R.string.delete_item_message, item.getName()))
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    if (repository.deletePantryItem(item.getId())) {
                        Toast.makeText(this, R.string.item_deleted, Toast.LENGTH_SHORT).show();
                        refreshPantry();
                    }
                })
                .show();
    }

    @Override
    protected void onDestroy() {
        repository.close();
        super.onDestroy();
    }

    private void refreshPantry() {
        // Reload in onResume so returning from the form always reflects the latest database state.
        List<PantryItem> items = repository.getAllPantryItems();
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        adapter.submitList(items, preferences.getBoolean(PREF_SHOW_EXPIRY, true));
        emptyText.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
