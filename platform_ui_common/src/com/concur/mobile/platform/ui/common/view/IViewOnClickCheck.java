package com.concur.mobile.platform.ui.common.view;

/**
 * An interface describing a view launcher check that can determine whether the intent should be launched.
 * 
 * @author AndrewK
 */
public interface IViewOnClickCheck {

    /**
     * Determines whether the intent associated with a view should be launched at this time.
     * 
     * @return <code>true</code> if the intent should be launched; <code>false</code> otherwise.
     */
    public boolean onClickCheck();

}
