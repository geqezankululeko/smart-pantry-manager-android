package za.ac.richfield.smartpantry.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import za.ac.richfield.smartpantry.model.PantryItem;

@RunWith(AndroidJUnit4.class)
public final class PantryRepositoryInstrumentedTest {
    private Context context;
    private PantryRepository repository;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        context.deleteDatabase(PantryDbHelper.DATABASE_NAME);
        repository = new PantryRepository(context);
    }

    @After
    public void tearDown() {
        repository.close();
        context.deleteDatabase(PantryDbHelper.DATABASE_NAME);
    }

    @Test
    public void seedsTwentyRecipesOnce() {
        assertEquals(20, repository.getRecipeCount());
        repository.close();
        repository = new PantryRepository(context);
        assertEquals(20, repository.getRecipeCount());
    }

    @Test
    public void performsPantryCrudWithPersistence() {
        long id = repository.createPantryItem("Tomatoes", 0.5, "kg", "2030-01-10");
        PantryItem created = repository.getPantryItem(id);
        assertNotNull(created);
        assertEquals("Tomato", created.getName());

        assertTrue(repository.updatePantryItem(id, "Tomato", 600, "g", "2030-01-11"));
        PantryItem updated = repository.getPantryItem(id);
        assertNotNull(updated);
        assertEquals(600.0, updated.getQuantity(), 0.0001);

        repository.close();
        repository = new PantryRepository(context);
        assertNotNull(repository.getPantryItem(id));
        assertTrue(repository.deletePantryItem(id));
        assertEquals(0, repository.getAllPantryItems().size());
    }
}
