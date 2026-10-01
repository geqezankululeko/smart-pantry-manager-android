package za.ac.richfield.smartpantry.ui;

import android.content.Intent;

import androidx.annotation.IdRes;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import za.ac.richfield.smartpantry.R;

/**
 * Centralises navigation shared by the three top-level application destinations.
 */
public abstract class BaseActivity extends AppCompatActivity {
    protected void configureBottomNavigation(@IdRes int selectedItemId) {
        BottomNavigationView navigation = findViewById(R.id.bottom_navigation);
        navigation.setSelectedItemId(selectedItemId);
        navigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == selectedItemId) {
                return true;
            }

            Class<?> destination;
            if (itemId == R.id.nav_pantry) {
                destination = PantryActivity.class;
            } else if (itemId == R.id.nav_recipes) {
                destination = SuggestedRecipesActivity.class;
            } else if (itemId == R.id.nav_settings) {
                destination = SettingsActivity.class;
            } else {
                return false;
            }

            Intent intent = new Intent(this, destination);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
            return true;
        });
    }
}
