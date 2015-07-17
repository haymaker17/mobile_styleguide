package com.concur.mobile.platform.ui.travel.util.test;

import android.content.Context;
import android.widget.TextView;
import com.concur.mobile.platform.ui.travel.util.ViewUtil;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

/**
 * @author ratank
 */
@Config(manifest = Config.NONE, sdk = 21) @RunWith(RobolectricTestRunner.class) public class ViewUtilTest {

    @Test public void testShowGDSName() {
        // for some reasons this is giving null hence, for time being, will use RuntimeEnvironment.application
        //Context context = PlatformUITravelTestApplication.getApplication();
        Context context = RuntimeEnvironment.application;
        TextView txtView = new TextView(context);
        ViewUtil.showGDSName(context, txtView, "Sabre");
        Assert.assertEquals("GDS Name set in TextView is in-correct", "(Sabre)", txtView.getText());
    }
}
