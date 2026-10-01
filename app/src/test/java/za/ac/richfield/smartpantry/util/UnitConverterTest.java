package za.ac.richfield.smartpantry.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class UnitConverterTest {
    @Test
    public void convertsMassAndVolumeToBaseUnits() {
        assertEquals(500.0, UnitConverter.toBase(0.5, "kg"), 0.0001);
        assertEquals(1000.0, UnitConverter.toBase(1, "l"), 0.0001);
        assertEquals(30.0, UnitConverter.toBase(2, "tbsp"), 0.0001);
    }

    @Test
    public void rejectsDifferentMeasurementDimensions() {
        assertTrue(UnitConverter.areCompatible("kg", "g"));
        assertTrue(UnitConverter.areCompatible("tbsp", "ml"));
        assertFalse(UnitConverter.areCompatible("g", "count"));
    }
}
