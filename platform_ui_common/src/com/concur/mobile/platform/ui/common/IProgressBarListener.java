package com.concur.mobile.platform.ui.common;

/**
 * Interface used to show/hide a progress bar. 
 * 
 * @author Chris N. Diaz
 *
 */
public interface IProgressBarListener {

    /**
     * Find the attached progressbar and set visibility.
     * */
    public void showProgressBar();

    /**
     * Find the attached progressbar and re-set visibility.
     * */
    public void hideProgressBar();

    /**
     * Find the attached progressbar and check whether found progressbar is shown.
     * */
    public boolean isProgressBarShown();
}
