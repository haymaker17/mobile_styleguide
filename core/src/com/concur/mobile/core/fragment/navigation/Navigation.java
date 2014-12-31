/**
 * 
 */
package com.concur.mobile.core.fragment.navigation;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.concur.core.R;
import com.concur.mobile.core.fragment.BaseFragment;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.ViewUtil;
import com.concur.mobile.core.widget.SimpleLayout;

/**
 * An extension of <code>BaseFragment</code> providing a navigation menu.
 * 
 * @author andy
 */
public class Navigation extends BaseFragment {

    private static final String CLS_TAG = "Navigation";

    /**
     * Contains whether or not the navigation menu is visible.
     */
    protected boolean open;

    /**
     * Contains whether or not the navigation menu is in the process of opening.
     */
    protected boolean opening;

    /**
     * Contains whether or not the navigation menu is in the process of closing.
     */
    protected boolean closing;

    /**
     * Contains the width of the navigation menu.
     */
    protected int navWidth;

    /**
     * Contains the right layout margin for the navigator menu from the screen.
     */
    protected int rightLayoutMargin = -1;

    /**
     * Contains the reference to the navigation menu view.
     */
    protected ViewGroup navView;

    /**
     * Contains the transparent overlay.
     */
    protected View overlay;

    /**
     * Contains a reference to the view that has been pushed to the right to uncover the navigation menu.
     */
    protected View mainView;

    /**
     * Contains the list of navigation items.
     */
    protected List<NavigationItem> navItems = null;

    /**
     * Contains a <code>NavigationItem</code> to be displayed in the ad container.
     */
    protected NavigationItem adNavItem = null;

    /**
     * Contains the <code>View.OnClickListener</code> for the navigation item view objects.
     */
    protected NavigationItemOnClickListener navItemClickListener;

    // Contains whether or not 'AnimationListener.onAnimationEnd' has been
    // called for
    // an open/close animation.
    private boolean animationEnded;

    /**
     * An interface describing events on a navigation item.
     */
    public interface NavigationListener extends FragmentListener {

        /**
         * Provides a notification that a navigation item has been selected.
         * 
         * @param item
         *            contains the navigation item that was selected.
         */
        public void onItemSelected(NavigationItem item);

    }

    /**
     * An interface describing a navigation item. <br>
     * <b>NOTE:</b><br>
     * Activities using this fragment should use a sub-interface of <code>NavigationItem</code>.
     */
    public interface NavigationItem {

        /**
         * Gets the resource ID of the layout that should be inflated to populate the navigation item. <br>
         * <br>
         * <b>NOTE:</b><br>
         * If a layout resource id is provided, then the layout must contain a <code>TextView</code> object with id
         * <code>R.id.text</code> and an <code>ImageView</code> object with id <code>R.id.icon</code>.
         * 
         * @return returns the resource ID of the layout that should be inflated to populate the navigation item. If a value of
         *         <code>-1</code> is returned, then the layout <code>R.layout.navigation_item</code> will be used as a default
         *         navigation layout item.
         */
        int getLayoutResId();

        /**
         * Gets whether this navigation item is selectable.
         * 
         * @return whether this navigation item is selectable.
         */
        boolean isSelectable();

        /**
         * Gets an integer-based value that will be set as the <code>View.Id</code> attribute on the constructed view.
         * 
         * @return the integer-based value that will be set as the <code>View.Id</code> attribute on the constructed view.
         */
        int getId();

    }

    /**
     * An interface extending <code>NavigationItem</code> that provides a non-selectable text entry.
     */
    public interface TextNavigationItem extends NavigationItem {

        /**
         * Gets the text resource id of the text that should be displayed.
         * 
         * @return the resource id of the text displayed in the navigation item.
         */
        public int getTextResId();
    }

    /**
     * An interface extending <code>TextNavigationItem</code> that provides a selectable navigation item.
     */
    public interface SimpleNavigationItem extends TextNavigationItem {

        /**
         * Gets the icon resource ID associated with this navigation item.
         * 
         * @return the icon resource ID associated with this navigation item.
         */
        public int getIconResourceId();

        /**
         * Gets the visiblity of the icon specified by one of the values <code>View.VISIBLE</code>, <code>View.INVISIBLE</code> or
         * <code>View.GONE</code>
         * 
         * @return returns the icon visibility setting as <code>View.VISIBLE</code>, <code>View.INVISIBLE</code> or
         *         <code>View.GONE</code>.
         */
        public int getIconVisibility();

        /**
         * Gets the visiblity of the navigatonItem specified by one of the values <code>View.VISIBLE</code>,
         * <code>View.INVISIBLE</code> or <code>View.GONE</code>
         * 
         * @return returns the icon visibility setting as <code>View.VISIBLE</code>, <code>View.INVISIBLE</code> or
         *         <code>View.GONE</code>.
         */
        public int getNavItemVisibility();
    }

    /**
     * An interface extending <code>NavigationItem</code> that provides a custom view.
     */
    public interface CustomNavigationItem extends NavigationItem {

        /**
         * Gets the custom view to be displayed for this navigation item.
         * 
         * @return a custom view for this navigation item.
         */
        public View getView();
    }

    // Contains the navigation listener.
    private NavigationListener navListener;

    /**
     * Sets the list of <code>NavigationItem</code> objects on the menu and an optional <code>NavigationItem</code> aligned at the
     * bottom of the menu in the ad container.
     * 
     * @param items
     *            contains the list of <code>NavigationItem</code> objects on the menu.
     * @param adNavItem
     *            contains the <code>NavigationItem</code> displayed in the ad container.
     */
    public void setNavigationItems(List<NavigationItem> items, NavigationItem adNavItem) {
        setNavigationItems(items, adNavItem, -1);
    }

    /**
     * Sets the list of <code>NavigationItem</code> objects on the menu and an optional <code>NavigationItem</code> aligned at the
     * bottom of the menu in the ad container.
     * 
     * @param items
     *            contains the list of <code>NavigationItem</code> objects on the menu.
     * @param adNavItem
     *            contains the <code>NavigationItem</code> displayed in the ad container.
     * @param rightNavMargin
     *            contains the right layout margin between the right edge of the navigation menu and the screen.
     */
    public void setNavigationItems(List<NavigationItem> items, NavigationItem adNavItem, int rightLayoutMargin) {
        this.navItems = items;
        this.adNavItem = adNavItem;
        this.rightLayoutMargin = rightLayoutMargin;
        LayoutInflater inflater = getActivity().getLayoutInflater();
        ViewGroup itemContainer = (ViewGroup) navView.findViewById(R.id.item_container);
        ViewGroup adContainer = (ViewGroup) navView.findViewById(R.id.ad_container);
        addNavigationItems(itemContainer, adContainer, inflater);
        installPreDrawListener();
        navView.postInvalidate();
        navView.requestLayout();
    }

    private void installPreDrawListener() {
        ViewTreeObserver vtObs = navView.getViewTreeObserver();
        if (vtObs != null) {
            vtObs.addOnPreDrawListener(new InitialPreDrawListener());
        }
    }

    class InitialPreDrawListener implements ViewTreeObserver.OnPreDrawListener {

        @Override
        public boolean onPreDraw() {
            // Obtain the initial populated navigation menu with.
            navWidth = navView.getWidth();
            // Determine the preferred navigation menu width.
            int prefMenuWidth = getPreferredNavigationMenuWidth(getScreenWidth(), navWidth);
            ViewGroup.LayoutParams layoutParams = navView.getLayoutParams();
            layoutParams.width = prefMenuWidth;
            Log.d(Const.LOG_TAG, CLS_TAG + ".ViewTreeObserver.onPreDraw: initial nav width = " + navWidth + ".");
            Log.d(Const.LOG_TAG, CLS_TAG + ".ViewTreeObserver.onPreDraw: preferredMenuWidth: " + prefMenuWidth);
            // Remove the current on pre draw listener.
            navView.getViewTreeObserver().removeOnPreDrawListener(this);
            // Install a new post-initial re-draw listener.
            navView.getViewTreeObserver().addOnPreDrawListener(new PostInitialPreDrawListener());
            // Set the new layout params and request a new layout.
            navView.setLayoutParams(layoutParams);
            navView.requestLayout();
            return true;
        }
    }

    class PostInitialPreDrawListener implements ViewTreeObserver.OnPreDrawListener {

        @Override
        public boolean onPreDraw() {
            navWidth = navView.getWidth();
            Log.d(Const.LOG_TAG, CLS_TAG + ".ViewTreeObserver.onPreDraw: post-initial nav width = " + navWidth + ".");
            navView.getViewTreeObserver().removeOnPreDrawListener(this);
            navView.setVisibility(View.INVISIBLE);
            return true;
        }
    }

    private int getScreenWidth() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) getActivity().getApplicationContext().getSystemService(
                Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;
        Log.d(Const.LOG_TAG, CLS_TAG + ".getScreenWidth: screenWidth -> " + Integer.toString(screenWidth));
        return screenWidth;
    }

    /**
     * Gets the preferred navigation menu width based on the screen width and populated navigation width.
     * 
     * @param screenWidth
     *            contains the current screen width
     * @param navWidth
     *            contains the current populated navigation menu width.
     * @return returns the preferred navigation menu width.
     */
    protected int getPreferredNavigationMenuWidth(int screenWidth, int navWidth) {
        int prefWidth = Math.round((screenWidth * 0.80F));
        if (rightLayoutMargin == -1) {
            if (navWidth <= prefWidth) {
                prefWidth = navWidth;
            }
        } else {
            prefWidth = screenWidth - rightLayoutMargin;
        }
        return prefWidth;
    }

    /**
     * Will create a transparent overlay that will cover the sized and position such that it will cover the right side of the
     * screen not covered by the navigation menu. Clicking on the overlay will result in the menu being closed.
     * 
     * @return returns an instance of <View> with a transparent background sized and positioned such that when visible will cover
     *         the screen where the navigation menu is not present. Clicking on the overlay will result in the menu being closed.
     */
    protected View createOverlay() {
        View overlay = null;
        LayoutInflater inflater = getActivity().getLayoutInflater();
        ViewGroup overlayParent = (ViewGroup) getActivity().findViewById(R.id.home_frame);
        overlay = inflater.inflate(R.layout.transparent_overlay, overlayParent, false);
        overlay.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // Close the menu.
                close(mainView);
            }
        });
        try {
            SimpleLayout.SimpleLayoutParams layoutParams = (SimpleLayout.SimpleLayoutParams) overlay.getLayoutParams();
            layoutParams.x = navWidth;
            // Android 2.2/2.3 requires setting of gravity to 'Gravity.TOP' in
            // order
            // for the layout params to be honored by the FrameLayout!
            // layoutParams.gravity = Gravity.TOP;
            overlay.setLayoutParams(layoutParams);
            overlayParent.addView(overlay);
        } catch (ClassCastException ccExc) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".createOverlay: frame layout params expected!", ccExc);
        }

        return overlay;
    }

    protected void showOverlay() {
        if (overlay == null) {
            overlay = createOverlay();
        }
        if (overlay != null) {
            overlay.setVisibility(View.VISIBLE);
        }
    }

    protected void hideOverlay() {
        if (overlay != null) {
            overlay.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * Will open the navigation menu by animating the change of the left position of a passed in view.
     * 
     * @param mainView
     *            contains the <code>View</code> that will have it's left position altered such that the underlying navigation
     *            menu is displayed.
     */
    public void open(View mainView) throws IllegalArgumentException {
        if (!opening) {
            navView.setVisibility(View.VISIBLE);
            this.mainView = mainView;
            opening = true;
            toggle(mainView);
        }
    }

    /**
     * Gets whether the menu is currently open.
     * 
     * @return whether or not the menu is currently open.
     */
    public boolean isOpen() {
        return open;
    }

    /**
     * Gets whether the menu is in the process of opening.
     * 
     * @return whether the menu is in the process of opening.
     */
    public boolean isOpening() {
        return opening;
    }

    /**
     * Gets whether the menu is in the process of closing.
     * 
     * @return whether the menu is in the process of closing.
     */
    public boolean isClosing() {
        return closing;
    }

    /**
     * Will close the navigation menu by animating the change of the left position of a passed in view.
     * 
     * @param mainView
     *            contains the <code>View</code> that will have it's left position altered such that the underlying navigation
     *            menu is hidden.
     */
    public void close(View mainView) throws IllegalArgumentException {
        if (!closing) {
            this.mainView = mainView;
            closing = true;
            toggle(mainView);
        }
    }

    /**
     * Toggles whether the navigation menu is visible.
     * 
     * @param mainView
     *            contains the main view that will have it's left position changed.
     */
    private void toggle(View mainView) {
        Animation anim = null;
        ViewGroup.LayoutParams lp = mainView.getLayoutParams();
        SimpleLayout.SimpleLayoutParams layoutParams = (SimpleLayout.SimpleLayoutParams) lp;
        if (layoutParams.x > 0) {
            anim = new TranslateAnimation(0.0F, -navWidth, 0.0F, 0.0F);
        } else {
            anim = new TranslateAnimation(0.0F, navWidth, 0.0F, 0.0F);
        }
        anim.setDuration(300L);
        anim.setInterpolator(new AccelerateDecelerateInterpolator());
        final View mv = mainView;
        anim.setFillAfter(true);
        anim.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {

                // NOTE: On Android 2.2.1 devices (Droid X), this is required in
                // order to ensure
                // the 'onAnimationEnd' code doesn't run twice!
                if (animationEnded) {
                    return;
                } else {
                    animationEnded = true;
                    opening = false;
                    closing = false;
                }

                // Call to ensure the animation actually stops.
                mv.clearAnimation();
                SimpleLayout.SimpleLayoutParams layoutParams = (SimpleLayout.SimpleLayoutParams) mv.getLayoutParams();
                int mvWidth = mv.getWidth();
                int mvHeight = mv.getHeight();
                if (layoutParams.x > 0) {
                    navView.setVisibility(View.INVISIBLE);
                    layoutParams.x -= navWidth;
                    layoutParams.width = FrameLayout.LayoutParams.MATCH_PARENT;
                    layoutParams.height = FrameLayout.LayoutParams.MATCH_PARENT;
                    open = false;
                    hideOverlay();
                } else {
                    layoutParams.x += navWidth;
                    layoutParams.width = mvWidth;
                    layoutParams.height = mvHeight;
                    open = true;
                    showOverlay();
                }
                mv.setLayoutParams(layoutParams);
            }
        });
        animationEnded = false;
        mainView.startAnimation(anim);
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        try {
            navItemClickListener = new NavigationItemOnClickListener();
            navView = (ViewGroup) inflater.inflate(R.layout.navigation, container, false);
        } catch (ClassCastException ccExc) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".onCreateView: navigation container is not an instance of ViewGroup!");
        }
        return navView;
    }

    /**
     * Will add any child navigation items views to the navigator parent container.
     * 
     * @param parent
     */
    protected void addNavigationItems(ViewGroup parent, ViewGroup adContainer, LayoutInflater inflater) {
        if (navItems != null) {
            for (NavigationItem navItem : navItems) {
                if ((navItem instanceof TextNavigationItem) || (navItem instanceof SimpleNavigationItem)) {
                    int layResId = R.layout.navigation_item;
                    if (navItem.getLayoutResId() != -1) {
                        layResId = navItem.getLayoutResId();
                    }
                    View navView = inflater.inflate(layResId, null, false);
                    navView.setId(navItem.getId());
                    int txtResId = -1;
                    if (navItem instanceof TextNavigationItem) {
                        txtResId = ((TextNavigationItem) navItem).getTextResId();
                    }
                    if (txtResId != -1) {
                        TextView txtView = (TextView) navView.findViewById(R.id.text);
                        if (txtView != null) {
                            txtView.setText(txtResId);
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG
                                    + ".addNavigationItems: unable to locate 'text' view in 'navigation_item' layout!");
                        }
                    }
                    int iconResId = -1;
                    int iconVisibility = View.VISIBLE;
                    if (navItem instanceof SimpleNavigationItem) {
                        iconVisibility = ((SimpleNavigationItem) navItem).getIconVisibility();
                        if (iconVisibility == View.VISIBLE) {
                            iconResId = ((SimpleNavigationItem) navItem).getIconResourceId();
                            ImageView imgView = (ImageView) navView.findViewById(R.id.icon);
                            if (imgView != null) {
                                imgView.setImageResource(iconResId);
                            } else {
                                Log.e(Const.LOG_TAG,
                                        CLS_TAG
                                                + ".addNavigationItems: unable to locate 'icon' view in 'navigation_item' layout!");
                            }
                        } else {
                            ViewUtil.setVisibility(navView, R.id.icon, View.GONE);
                        }
                    } else {
                        ViewUtil.setVisibility(navView, R.id.icon, View.GONE);
                    }
                    navView.setTag(navItem);
                    parent.addView(navView);
                    // Is the item selectable?
                    if (navItem.isSelectable()) {
                        navView.setOnClickListener(navItemClickListener);
                    }
                } else if (navItem instanceof CustomNavigationItem) {
                    View customView = ((CustomNavigationItem) navItem).getView();
                    if (customView != null) {
                        customView.setTag(navItem);
                        // Is the item selectable?
                        if (navItem.isSelectable()) {
                            customView.setOnClickListener(navItemClickListener);
                        }
                        parent.addView(customView);
                    }
                }
            }
        }
        // Add the navigation ad item.
        if (adNavItem != null) {
            int layResId = R.layout.navigation_ad;
            if (adNavItem.getLayoutResId() != -1) {
                layResId = adNavItem.getLayoutResId();
            }
            View adNavView = inflater.inflate(layResId, null, false);
            adNavView.setTag(adNavItem);
            adContainer.addView(adNavView);
            // Is the item selectable?
            if (adNavItem.isSelectable()) {
                adNavView.setOnClickListener(navItemClickListener);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.support.v4.app.Fragment#onAttach(android.app.Activity)
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof NavigationListener) {
            navListener = (NavigationListener) activity;
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".onAttach: activity is not an instance of NavigationListener!");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.support.v4.app.Fragment#onDetach()
     */
    @Override
    public void onDetach() {
        super.onDetach();
        navListener = null;
    }

    /**
     * An implementation of <code>OnClickListener</code> to handle navigation item touch events.
     */
    class NavigationItemOnClickListener implements OnClickListener {

        private final String CLS_TAG = Navigation.CLS_TAG + "." + "NavigationItemOnClickListener";

        @Override
        public void onClick(View view) {
            if (view.getTag() != null) {
                try {
                    NavigationItem navItem = (NavigationItem) view.getTag();
                    if (navListener != null) {
                        navListener.onItemSelected(navItem);
                    }
                } catch (ClassCastException ccExc) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".onClick: view tag object not instance of 'NavigationItem'!");
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".onClick: view has null tag!");
            }
        }
    }

}
