package com.concur.mobile.core.expense.travelallowance.adapter.test;

import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.concur.core.R;
import com.concur.mobile.core.expense.travelallowance.adapter.ItineraryUpdateListAdapter;
import com.concur.mobile.core.expense.travelallowance.datamodel.Itinerary;
import com.concur.mobile.core.expense.travelallowance.service.GetTAItinerariesRequest;
import com.concur.mobile.core.expense.travelallowance.testutils.FileRequestTaskWrapper;
import com.concur.mobile.core.expense.travelallowance.util.BundleId;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.List;

import testconfig.CoreTestApplication;
import testconfig.CoreTestRunner;

import static org.junit.Assert.assertNotEquals;

/**
 * Created by D028778 on 23.10.2015.
 */
@Config(application = CoreTestApplication.class, manifest = "AndroidManifest.xml", sdk = 21)
@RunWith(CoreTestRunner.class)
public class ItineraryUpdateListAdapterTest {

    private static final String TEST_DATA_PATH = "src/test/java/com/concur/mobile/core/expense/travelallowance/testdata";

    @Test
    public void showMessageAreaNotVisible() {
        FileRequestTaskWrapper requestWrapper = new FileRequestTaskWrapper(new GetTAItinerariesRequest(null, null, null, false));
        Bundle resultData = requestWrapper.parseFile(TEST_DATA_PATH, "ItineraryReadXMLWithTimeZoneOffset.xml");
        List<Itinerary> itineraries = (List<Itinerary>) resultData.getSerializable(BundleId.ITINERARY_LIST);
        ItineraryUpdateListAdapter adapter = new ItineraryUpdateListAdapter(RuntimeEnvironment.application, null, null, null, null, null, null, itineraries.get(0).getSegmentList());
        ListView listView = new ListView(RuntimeEnvironment.application);
        listView.setAdapter(adapter);
        listView.layout(0, 0, 100, 10000);
        View vMessageArea = listView.findViewById(R.id.v_message_area);
        int visible = vMessageArea.getVisibility();
        assertNotEquals(View.VISIBLE, visible);
    }

}
