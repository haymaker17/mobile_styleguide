package com.concur.mobile.core.expense.travelallowance.datamodel.test;

import com.concur.mobile.core.expense.travelallowance.datamodel.LodgingType;

import junit.framework.TestCase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/**
 * Created by D023077 on 17.09.2015.
 */
@Config(manifest = Config.NONE, sdk = 21)
@RunWith(RobolectricTestRunner.class)

public class LodgingTypeTest extends TestCase {

    @Test
    public void constructorTest() {
        assertNotNull(new LodgingType());
    }

    @Test
    public void setterGetterTest() {
        LodgingType lodgingType = createLodgingType();

        assertEquals("HOTEL", lodgingType.getCode());
        assertEquals("Hotel", lodgingType.getDescription());

        lodgingType.setCode("MOTEL");
        lodgingType.setDescription("Motel");
        assertEquals("MOTEL", lodgingType.getCode());
        assertEquals("Motel", lodgingType.getDescription());

    }

    @Test
    public void equalsTest() {
        LodgingType lodgingTypeBase = createLodgingType();

        LodgingType lodgingTypeComp;
        lodgingTypeComp = null;
        Object o = new Object();

        assertTrue(lodgingTypeBase.equals(lodgingTypeBase));

        assertFalse(lodgingTypeBase.equals(lodgingTypeComp));
        assertFalse(lodgingTypeBase.equals(o));

        lodgingTypeComp = createLodgingType();
        assertTrue(lodgingTypeBase.equals(lodgingTypeComp));

        lodgingTypeComp.setCode("MOTEL");
        assertFalse(lodgingTypeBase.equals(lodgingTypeComp));
        lodgingTypeComp.setCode("HOTEL");

        lodgingTypeComp.setDescription("Motel");
        assertFalse(lodgingTypeBase.equals(lodgingTypeComp));
        lodgingTypeComp.setDescription("Hotel");

    }

    @Test
    public void hashCodeTest() {
        LodgingType lodgingType = createLodgingType();

        assertEquals(-2088224128, lodgingType.hashCode());
    }

    @Test
    public void compareToTest() {
        LodgingType lodgingTypeBase = createLodgingType();
        LodgingType lodgingTypeComp = createLodgingType();

        assertEquals(1, lodgingTypeBase.compareTo(null));

        assertEquals(0, lodgingTypeBase.compareTo(lodgingTypeComp));

        lodgingTypeComp.setCode(null);
        assertEquals(1, lodgingTypeBase.compareTo(lodgingTypeComp));

        lodgingTypeBase.setCode(null);
        assertEquals(0, lodgingTypeBase.compareTo(lodgingTypeComp));

        lodgingTypeComp.setCode("MOTEL");
        assertEquals(-1, lodgingTypeBase.compareTo(lodgingTypeComp));

    }

    @Test
    public void toStringTest(){
        LodgingType lodgingType = createLodgingType();

        assertEquals("Hotel", lodgingType.toString());

        lodgingType.setDescription("");
        assertEquals("HOTEL", lodgingType.toString());

    }

    private LodgingType createLodgingType(){
        LodgingType lT = new LodgingType("HOTEL", "Hotel");

        return lT;
    }
}
