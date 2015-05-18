package com.concur.mobile.platform.ui.travel.util;

import android.text.TextPaint;
import android.text.style.URLSpan;

/**
 * util to strip the underline from Linkify text
 *
 * @author tejoa
 */
public class URLSpanNoUnderline extends URLSpan {

    public URLSpanNoUnderline(String url) {

        super(url);

    }

    @Override
    public void updateDrawState(TextPaint tp) {

        super.updateDrawState(tp);
        tp.setUnderlineText(false);

    }
}
