package com.concur.mobile.core.expense.travelallowance.datamodel.test;

import com.concur.core.R;
import com.concur.mobile.core.expense.travelallowance.datamodel.SynchronizationStatus;

import junit.framework.TestCase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/**
 * Created by D023077 on 10.09.2015.
 */
@Config(manifest = Config.NONE, sdk = 21)
@RunWith(RobolectricTestRunner.class)

public class SynchronizationStatusTest extends TestCase {
    /**
     * Check if the correct resource IDs are returned
     */
    @Test
    public void getResourceId() {
        assertEquals(R.string.general_status_synchronization_failed,  SynchronizationStatus.FAILED.getTextResourceId());
        assertEquals(R.string.general_status_synchronization_pending, SynchronizationStatus.PENDING.getTextResourceId());
        assertEquals(R.string.general_status_synchronized,            SynchronizationStatus.SYNCHRONIZED.getTextResourceId());
    }

    /**
     * Check if the correct entry is selected
     */
    @Test
    public void fromCode(){
        assertEquals(SynchronizationStatus.FAILED,       SynchronizationStatus.fromCode("FAILURE"));
        assertEquals(SynchronizationStatus.PENDING,      SynchronizationStatus.fromCode("PENDING"));
        assertEquals(SynchronizationStatus.SYNCHRONIZED, SynchronizationStatus.fromCode("SUCCESS"));
        assertNull(SynchronizationStatus.fromCode("XLV"));
    }



}
