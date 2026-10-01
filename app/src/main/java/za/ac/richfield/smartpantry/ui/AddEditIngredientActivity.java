package za.ac.richfield.smartpantry.ui;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Calendar;

import za.ac.richfield.smartpantry.R;
import za.ac.richfield.smartpantry.data.PantryRepository;
import za.ac.richfield.smartpantry.model.PantryItem;
import za.ac.richfield.smartpantry.util.DateUtils;
import za.ac.richfield.smartpantry.util.DisplayFormatters;

/**
 * Reuses one validated form for create and update modes and confirms destructive deletion.
 */
public final class AddEditIngredientActivity extends BaseActivity {
    public static final String EXTRA_PANTRY_ID = "extra_pantry_id";

    private PantryRepository repository;
    private TextInputLayout nameLayout;
    private TextInputLayout quantityLayout;
    private TextInputLayout expiryLayout;
    private TextInputEditText nameInput;
    private TextInputEditText quantityInput;
    private TextInputEditText expiryInput;
    private Spinner unitSpinner;
    private long pantryId = -1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_ingredient);

        repository = new PantryRepository(this);
        pantryId = getIntent().getLongExtra(EXTRA_PANTRY_ID, -1);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(pantryId == -1 ? R.string.add_ingredient : R.string.edit_ingredient);
        }

        TextView heading = findViewById(R.id.form_heading);
        heading.setText(pantryId == -1 ? R.string.add_ingredient : R.string.edit_ingredient);

        nameLayout = findViewById(R.id.name_input_layout);
        quantityLayout = findViewById(R.id.quantity_input_layout);
        expiryLayout = findViewById(R.id.expiry_input_layout);
        nameInput = findViewById(R.id.name_input);
        quantityInput = findViewById(R.id.quantity_input);
        expiryInput = findViewById(R.id.expiry_input);
        unitSpinner = findViewById(R.id.unit_spinner);

        expiryInput.setOnClickListener(view -> showDatePicker());
        expiryLayout.setEndIconOnClickListener(view -> showDatePicker());

        MaterialButton saveButton = findViewById(R.id.save_button);
        MaterialButton cancelButton = findViewById(R.id.cancel_button);
        MaterialButton clearExpiryButton = findViewById(R.id.clear_expiry_button);
        saveButton.setOnClickListener(view -> saveItem());
        cancelButton.setOnClickListener(view -> finish());
        clearExpiryButton.setOnClickListener(view -> expiryInput.setText(""));

        if (pantryId == -1) {
            selectDefaultUnit();
        } else {
            loadExistingItem();
        }
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

    private void loadExistingItem() {
        PantryItem item = repository.getPantryItem(pantryId);
        if (item == null) {
            Toast.makeText(this, R.string.record_not_found, Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        nameInput.setText(item.getName());
        quantityInput.setText(DisplayFormatters.quantity(item.getQuantity()));
        expiryInput.setText(item.getExpiryDate() == null ? "" : item.getExpiryDate());
        selectUnit(item.getUnit());
    }

    private void selectDefaultUnit() {
        SharedPreferences preferences = getSharedPreferences(PantryActivity.PREFS_NAME, MODE_PRIVATE);
        selectUnit(preferences.getString(PantryActivity.PREF_DEFAULT_UNIT, "g"));
    }

    private void selectUnit(String unit) {
        ArrayAdapter<?> adapter = (ArrayAdapter<?>) unitSpinner.getAdapter();
        for (int index = 0; index < adapter.getCount(); index++) {
            Object option = adapter.getItem(index);
            if (option != null && option.toString().equalsIgnoreCase(unit)) {
                unitSpinner.setSelection(index);
                return;
            }
        }
    }

    private void saveItem() {
        nameLayout.setError(null);
        quantityLayout.setError(null);
        expiryLayout.setError(null);

        String name = textOf(nameInput);
        String quantityText = textOf(quantityInput);
        String expiryDate = textOf(expiryInput);
        boolean valid = true;

        if (name.isEmpty()) {
            nameLayout.setError(getString(R.string.required_field));
            valid = false;
        }

        double quantity = 0;
        try {
            quantity = Double.parseDouble(quantityText);
            if (!Double.isFinite(quantity) || quantity <= 0) {
                throw new NumberFormatException("Quantity is not positive");
            }
        } catch (NumberFormatException exception) {
            quantityLayout.setError(getString(R.string.invalid_quantity));
            valid = false;
        }

        if (!DateUtils.isValidIsoDate(expiryDate)) {
            expiryLayout.setError(getString(R.string.invalid_date));
            valid = false;
        }

        if (!valid) {
            return;
        }

        String unit = unitSpinner.getSelectedItem().toString();
        boolean saved;
        if (pantryId == -1) {
            saved = repository.createPantryItem(name, quantity, unit, expiryDate) != -1;
        } else {
            saved = repository.updatePantryItem(pantryId, name, quantity, unit, expiryDate);
        }

        if (saved) {
            Toast.makeText(this, R.string.item_saved, Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        }
    }

    private void showDatePicker() {
        Calendar now = Calendar.getInstance();
        new DatePickerDialog(
                this,
                (view, year, month, day) -> expiryInput.setText(
                        DateUtils.formatIsoDate(year, month, day)),
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH))
                .show();
    }

    private String textOf(TextInputEditText input) {
        return input.getText() == null ? "" : input.getText().toString().trim();
    }
}
