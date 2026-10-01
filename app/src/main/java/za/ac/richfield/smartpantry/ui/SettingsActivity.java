package za.ac.richfield.smartpantry.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.google.android.material.switchmaterial.SwitchMaterial;

import za.ac.richfield.smartpantry.R;
import za.ac.richfield.smartpantry.data.PantryRepository;

/**
 * Persists lightweight display preferences separately from relational pantry data.
 */
public final class SettingsActivity extends BaseActivity {
    private PantryRepository repository;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        repository = new PantryRepository(this);
        SharedPreferences preferences = getSharedPreferences(PantryActivity.PREFS_NAME, MODE_PRIVATE);

        SwitchMaterial expirySwitch = findViewById(R.id.expiry_warning_switch);
        Spinner defaultUnitSpinner = findViewById(R.id.default_unit_spinner);
        TextView recipeCount = findViewById(R.id.recipe_count_text);

        expirySwitch.setChecked(preferences.getBoolean(PantryActivity.PREF_SHOW_EXPIRY, true));
        selectUnit(defaultUnitSpinner, preferences.getString(PantryActivity.PREF_DEFAULT_UNIT, "g"));
        recipeCount.setText(getString(R.string.seeded_recipe_count, repository.getRecipeCount()));

        expirySwitch.setOnCheckedChangeListener((button, checked) -> preferences.edit()
                .putBoolean(PantryActivity.PREF_SHOW_EXPIRY, checked)
                .apply());

        defaultUnitSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Object selected = parent.getItemAtPosition(position);
                if (selected != null) {
                    preferences.edit()
                            .putString(PantryActivity.PREF_DEFAULT_UNIT, selected.toString())
                            .apply();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // The spinner always retains a valid unit selection.
            }
        });

        configureBottomNavigation(R.id.nav_settings);
    }

    @Override
    protected void onDestroy() {
        repository.close();
        super.onDestroy();
    }

    private void selectUnit(Spinner spinner, String unit) {
        ArrayAdapter<?> adapter = (ArrayAdapter<?>) spinner.getAdapter();
        for (int index = 0; index < adapter.getCount(); index++) {
            Object option = adapter.getItem(index);
            if (option != null && option.toString().equalsIgnoreCase(unit)) {
                spinner.setSelection(index);
                return;
            }
        }
    }
}
