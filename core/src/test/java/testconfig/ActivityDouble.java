package testconfig;

import android.app.Activity;

import com.concur.core.R;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadows.ShadowActivity;

/**
 * Created by D049515 on 17.07.2015.
 */
@Implements(Activity.class)
public class ActivityDouble extends ShadowActivity {

    public ActivityDouble() {}


    @Implementation
    public String getString(int resId) {

        String s = "";

        if (resId == R.string.ta_no_adjustments) {
            s = StringConstants.NO_ADJUSTMEMTS;
        }

        if (resId == R.string.general_yes) {
            s = StringConstants.GENERAL_YES;
        }

        if (resId == R.string.general_no) {
            s = StringConstants.GENERAL_NO;
        }

       return s;
    }

}
