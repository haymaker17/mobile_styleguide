package com.concur.mobile.platform.ui.common.view;

import android.content.Context;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.widget.EditText;
import com.concur.mobile.platform.ui.common.util.FormatUtil;
import com.concur.mobile.platform.util.Parse;

import java.util.Currency;
import java.util.Locale;
import java.util.regex.Matcher;

/**
 * Created by OlivierB on 05/02/2015.
 */
public class MoneyFormField extends EditText implements TextWatcher {

    private Locale locale;
    private Currency currency;
    private Double amountValue;

    public MoneyFormField(Context context, Locale loc, String currencyCode) {
        super(context);
        this.setInputType(InputType.TYPE_CLASS_NUMBER);
        this.locale = loc;
        if (currencyCode != null) {
            currency = Currency.getInstance(currencyCode);
        }
    }

    public void setCurrencyCode(String currencyCode) {
        if (currencyCode != null && (currency == null || !currencyCode.equals(currency.getCurrencyCode()))) {
            // --- removes the previous currency symbol
            if (getText().length() > 0 && currency != null) {
                final String s = getText().toString().replaceAll(Matcher.quoteReplacement(currency.getSymbol()), "");
                currency = Currency.getInstance(currencyCode);
                setText(s);
            } else {
                currency = Currency.getInstance(currencyCode);
                afterTextChanged(getText());
            }
        }
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    @Override public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
        // --- ntd
    }

    @Override public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
        // --- ntd
    }

    @Override public void afterTextChanged(Editable editable) {
        // --- disable listener
        removeTextChangedListener(this);
        final int cursorPosition = getSelectionStart();
        final int inputLength = editable.toString().length();

        // --- clean currency input
        String s = editable.toString().replaceAll(Matcher.quoteReplacement("[" + currency.getSymbol() + ",]"), "");
        amountValue = Parse.safeParseDouble(s);
        if (amountValue == null) {
            setError("error");
        } else {
            if (locale != null && currency != null) {
                s = FormatUtil.formatAmount(amountValue, locale, currency.getCurrencyCode(), true, true);
            }
            setText(s);
            int lengthDif = s.length() - inputLength;
            setSelection(Math.max(cursorPosition + lengthDif, 0));
        }

        // --- enable listener
        addTextChangedListener(this);
    }

    public Double getAmountValue() {
        return amountValue;
    }
}
