package com.concur.mobile.core.util;

import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.style.CharacterStyle;
import android.text.style.StyleSpan;

public class StyleableSpannableStringBuilder extends SpannableStringBuilder {

    public StyleableSpannableStringBuilder appendWithStyle(CharacterStyle c, CharSequence text) {
        super.append(text);
        int startPos = length() - text.length();
        setSpan(c, startPos, length(), 0);
        return this;
    }

    public StyleableSpannableStringBuilder appendBold(CharSequence text) {
        return appendWithStyle(new StyleSpan(Typeface.BOLD), text);
    }

}
