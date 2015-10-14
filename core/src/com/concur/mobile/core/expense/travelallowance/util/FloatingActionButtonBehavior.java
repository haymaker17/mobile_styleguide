package com.concur.mobile.core.expense.travelallowance.util;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.concur.core.R;

/**
 * Created by D049515 on 08.10.2015.
 */
public class FloatingActionButtonBehavior extends FloatingActionButton.Behavior {

    public FloatingActionButtonBehavior(Context context, AttributeSet attrs) {

    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, FloatingActionButton child, View dependency) {
        if (dependency instanceof Toolbar) {
            return true;
        } else {
            return super.layoutDependsOn(parent, child, dependency);
        }
    }


    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, FloatingActionButton child, View dependency) {
        if (dependency instanceof Toolbar) {
            float translationY = Math.min(0, dependency.getTranslationY() - dependency.getHeight());
            if (dependency.getVisibility() == View.GONE) {
                child.setTranslationY(0);
                return true;
            }
            child.setTranslationY(translationY);
            return true;
        } else {
            return super.onDependentViewChanged(parent, child, dependency);
        }

    }
}
