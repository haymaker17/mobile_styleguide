package com.concur.mobile.platform.util;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;

/**
 * Contains constants and animation functions that can apply to Expenseit and other Views.
 *
 * @author Elliott Jacobsen-Watts
 */
public class AnimationUtil {

    static final float FACTOR = 1.2f;
    static final int ANIMATION_DURATION_BASIC = 300;
    static final int ANIMATION_DURATION_ROTATE = 1000;
    static final int ANIMATION_DURATION_SLIDE_TO_LEFT = 500;
    static final int ANIMATION_DURATION_SLIDE_TO_BOTTOM = 200;
    static final int ROTATION_DEGREE = 360;
    static final float ROTATION_PIVOT_VALUE = 0.5f;

    public static void rotateAnimation(View view) {
        RotateAnimation animation = new RotateAnimation(0, ROTATION_DEGREE, Animation.RELATIVE_TO_SELF, ROTATION_PIVOT_VALUE, Animation.RELATIVE_TO_SELF, ROTATION_PIVOT_VALUE);
        animation.setInterpolator(new LinearInterpolator());
        animation.setRepeatMode(Animation.RESTART);
        animation.setRepeatCount(Animation.INFINITE);
        animation.setDuration(ANIMATION_DURATION_ROTATE);
        view.startAnimation(animation);
    }
}
