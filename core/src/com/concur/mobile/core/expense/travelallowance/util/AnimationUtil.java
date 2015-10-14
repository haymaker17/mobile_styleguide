package com.concur.mobile.core.expense.travelallowance.util;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.os.Build;
import android.view.View;
import android.view.ViewAnimationUtils;

/**
 * Created by D049515 on 13.10.2015.
 */
public class AnimationUtil {



   public static void fabToolbarAnimation(final View fab, final int fabTargetVisibility, final View toolbar, int toolbarTargetVisibility) {
       if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {
           fab.setVisibility(fabTargetVisibility);
           toolbar.setVisibility(toolbarTargetVisibility);
       } else {
           // Center coordinates for clipping circle.
           int cxFab;
           int cyFab;
           int radiusFab;
           int cxToolb;
           int cyToolb;
           int radiusToolb;

           cxFab = fab.getWidth() / 2;
           cyFab = fab.getHeight();
           radiusFab = fab.getWidth();

           cxToolb = toolbar.getWidth();
           cyToolb = toolbar.getHeight();
           radiusToolb = toolbar.getWidth();

           final Animator fabAnim;
           final Animator toolbAnim;

           if (fabTargetVisibility == View.GONE) {
               fabAnim =
                       ViewAnimationUtils.createCircularReveal(fab, cxFab, cyFab, radiusFab, 0);
               fabAnim.addListener(new AnimatorListenerAdapter() {
                   @Override
                   public void onAnimationEnd(Animator animation) {
                       super.onAnimationEnd(animation);
                       fab.setVisibility(fabTargetVisibility);
                   }
               });
           } else {
               fabAnim =
                       ViewAnimationUtils.createCircularReveal(fab, cxFab, cyFab, 0, radiusFab);
               fab.setVisibility(View.VISIBLE);
           }




           if (toolbarTargetVisibility == View.GONE) {
               toolbAnim =
                       ViewAnimationUtils.createCircularReveal(toolbar, cxToolb, cyToolb, radiusToolb, 0);
               toolbAnim.addListener(new AnimatorListenerAdapter() {
                   @Override
                   public void onAnimationEnd(Animator animation) {
                       super.onAnimationEnd(animation);
                       toolbar.setVisibility(View.GONE);
                   }
               });
           } else {
               toolbAnim =
                       ViewAnimationUtils.createCircularReveal(toolbar, cxToolb, cyToolb, 0, radiusToolb);
               toolbar.setVisibility(View.VISIBLE);
           }


           if (fabTargetVisibility == View.GONE) {
               fabAnim.start();
               toolbAnim.start();
           } else {
               toolbAnim.start();
               fabAnim.start();
           }

       }
   }



    public static void goneAnimation(final View v) {

        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {
            v.setVisibility(View.GONE);

        } else {

// get the center for the clipping circle
            int cx = v.getWidth() / 2;
            int cy = v.getHeight() / 2;

// get the initial radius for the clipping circle
            int initialRadius = v.getWidth();

// create the animation (the final radius is zero)
            Animator anim =
                    ViewAnimationUtils.createCircularReveal(v, cx, cy, initialRadius, 0);

// make the view invisible when the animation is done
            anim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    v.setVisibility(View.GONE);
                }
            });

// start the animation
            anim.start();
        }
    }


    public static void visibleAnimation(View v) {
        // previously invisible view

        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {
            v.setVisibility(View.GONE);

        } else {

// get the center for the clipping circle
            int cx = v.getWidth() - 10;
            int cy = v.getHeight() / 2;

// get the final radius for the clipping circle
            int finalRadius = Math.max(v.getWidth(), v.getHeight());

// create the animator for this view (the start radius is zero)
            Animator anim =
                    ViewAnimationUtils.createCircularReveal(v, cx, cy, 0, finalRadius);

// make the view visible and start the animation
            v.setVisibility(View.VISIBLE);
            anim.start();
        }
    }

}
