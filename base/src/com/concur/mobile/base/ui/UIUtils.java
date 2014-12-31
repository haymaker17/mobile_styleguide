package com.concur.mobile.base.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;

/**
 * Contains static generic utility methods for UI operations.
 * 
 * @author westonw
 * 
 */
public class UIUtils {

    /**
     * Places the layout from {@code overlayLayoutId} over the current activity. This will overlay the ActionBar as well if
     * applicable. When the dismiss icon view from {@code dismissIconId} is clicked, the overlay will be dismissed using the
     * provided animation.
     * 
     * @param rootView
     *            The decorView from the calling Activity. Call {@code (ViewGroup) getWindow().getDecorView();} to find this.
     * @param overlayLayoutId
     *            The resource Id for the layout to be used as the overlay. If invalid, this will throw an InflateException.
     * @param dismissListener
     *            An {@code OnClickListener} whose {@code onClick()} method will be called when the overlay is dismissed.
     * @param dismissIconId
     *            The resource Id for the {@code View} of the dismiss icon, which will be clickable. Typically an
     *            {@code ImageView}.
     * @param context
     *            The context
     * @param animId
     *            The resource id of the animation to be used
     * @return the inflated Overlay so user can adjust it if needed.
     */
    public static View setupOverlay(final ViewGroup rootView, int overlayLayoutId,
            final OnClickListener dismissListener, int dismissIconId, Context context, final int animId,
            final long animDuration) {

        // The temporary container to hold rootViewContents and Overlay to allow Overlay to cover ActionBar
        final FrameLayout tempContainer = new FrameLayout(context);

        // Grab the Main Layout from the Device Window
        final ViewGroup rootViewContents = (ViewGroup) rootView.getChildAt(0);

        // Remove the rootViewContents from the Device Window and add to temp container
        rootView.removeView(rootView.getChildAt(0));
        tempContainer.addView(rootViewContents);

        // Inflate the overlay layout into the temp container
        LayoutInflater inflater = LayoutInflater.from(context);
        final ViewGroup overlay = (ViewGroup) inflater.inflate(overlayLayoutId, tempContainer);
        // If we have no overlay, bail.
        if (overlay == null) {
            return null;
        }

        View dismissIcon = overlay.findViewById(dismissIconId);
        // If we have no dismiss icon, bail.
        if (dismissIcon == null) {
            return null;
        }

        // User clicks to dismiss the overlay
        dismissIcon.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                Animation fadeOut = AnimationUtils.loadAnimation(rootView.getContext(), animId);
                fadeOut.setFillAfter(true);
                fadeOut.setDuration(animDuration);
                fadeOut.setAnimationListener(new AnimationListener() {

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        overlay.clearAnimation();
                        // The tempContainer is detached from rootView here, empty it.
                        tempContainer.removeAllViews();
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                        // Animation doesn't repeat
                    }

                    @Override
                    public void onAnimationStart(Animation animation) {
                        // no-op
                    }
                });

                overlay.startAnimation(fadeOut);

                // Move the root contents back to the root view and pull the temp container (it will get emptied onAnimationEnd).
                tempContainer.removeView(rootViewContents);
                rootView.removeView(tempContainer);
                rootView.addView(rootViewContents);

                // The listener that is sent in may have calls in onClick that must be made.
                if (dismissListener != null) {
                    dismissListener.onClick(v);
                }

            }
        });

        // Make sure the overlay is on top
        overlay.bringToFront();

        // Add the temp container with root contents and overlay to the root view
        rootView.addView(tempContainer);

        return overlay;
    }
}
