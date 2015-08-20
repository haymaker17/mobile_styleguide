package com.concur.mobile.platform.ui.common.view;

import android.content.Context;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.widget.EditText;
import com.concur.mobile.platform.ui.common.R;
import com.concur.mobile.platform.ui.common.util.FormatUtil;
import com.concur.mobile.platform.util.Parse;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
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
    private boolean isDecimal;
    private char decimalSeparator;
    private char groupingSeparator;

    public MoneyFormField(Context context, Locale loc, String currencyCode) {
        super(context);
        this.locale = loc;
        addTextChangedListener(this);
        applyCurrency(currencyCode);
    }

    @Override
    public int getInputType() {
        return isDecimal ?
                (InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL) :
                InputType.TYPE_CLASS_NUMBER;
    }

    private void applyCurrency(String currencyCode) {
        isDecimal = false;
        decimalSeparator = groupingSeparator = ' ';
        if (currencyCode != null) {
            currency = Currency.getInstance(currencyCode);
            if (currency != null) {
                isDecimal = currency.getDefaultFractionDigits() > 0;
                final DecimalFormatSymbols formatSymbols = ((DecimalFormat) NumberFormat.getCurrencyInstance(locale))
                        .getDecimalFormatSymbols();
                decimalSeparator = formatSymbols.getDecimalSeparator();
                groupingSeparator = formatSymbols.getGroupingSeparator();
            }
        }
        // DigitsKeyListener.getInstance(false, isDecimal) seems KO as it does not work contrarily to what follows.
        this.setKeyListener(isDecimal ?
                DigitsKeyListener.getInstance("0123456789.,") :
                DigitsKeyListener.getInstance("0123456789"));
    }

    /**
     * Set the currency code of the currency in use. This will apply the corresponding currency to the amount
     * string rendered on screen ($###,###.## / ### ###,##€ / whatever) depending on the user's locale & the code
     * received.
     * => The string format depends on the locale while the currency within depends on the code received.
     *
     * @param currencyCode
     */
    public void setCurrencyCode(String currencyCode) {
        if (currencyCode == null) {
            return;
        }
        if (currency == null) {
            // --- Value set
            applyCurrency(currencyCode);
            afterTextChanged(getText());
        } else if (!currencyCode.equals(currency.getCurrencyCode())) {
            // --- Value changed
            if (getText().length() > 0) {
                // --- remove old currency symbol first
                final String s = cleanInput(getText().toString());

                applyCurrency(currencyCode);
                setText(s);
            } else {
                applyCurrency(currencyCode);
            }
        }
    }

    /**
     * Basically removes the formatting applied by FormatUtil.formatAmout() to extract the double value displayed
     *
     * @param input
     * @return
     */
    private String cleanInput(String input) {
        return input.replaceAll(Matcher.quoteReplacement("[" + currency.getSymbol() + groupingSeparator + " ]"), "");
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
        String inputString = editable.toString();
        // --- disable listener
        removeTextChangedListener(this);
        final int cursorPosition = getSelectionStart();
        final String nextChar = cursorPosition < inputString.length() ?
                inputString.substring(cursorPosition, cursorPosition + 1) :
                null;
        final String inputChar = cursorPosition > 0 ? inputString.substring(cursorPosition - 1, cursorPosition) : null;
        final String sep = String.valueOf(decimalSeparator);
        if (currency != null && sep != null && inputChar != null) {
            if (inputChar.equals(sep)) {
                // --- input = previousChar = decimalSeparator => we remove the first one
                if (sep.equals(nextChar)) {
                    // --- ###..### => we just remove the dot added
                    inputString = inputString.substring(0, cursorPosition - 1) + inputString.substring(cursorPosition);
                } else if (inputString.substring(cursorPosition).contains(sep)) {
                    // --- ###.[???] => we remove the second dot on the right side
                    final int secondSepPos = cursorPosition + inputString.substring(cursorPosition).indexOf(sep);
                    inputString = inputString.substring(0, secondSepPos - 1) + inputString.substring(secondSepPos + 1);
                } else if (inputString.substring(0, cursorPosition - 1).contains(sep)) {
                    // --- [???].### => we just remove the dot added
                    inputString = inputString.substring(0, cursorPosition - 1) + inputString.substring(cursorPosition);
                }
            }
        }

        final int inputLength = inputString.length();

        // --- clean currency input
        String s = currency != null ? cleanInput(inputString) : inputString;
        amountValue = Parse.safeParseDouble(s);
        if (amountValue == null && s.length() > 0) {
            setError(getResources().getString(R.string.general_field_value_invalid));
        } else {
            if (amountValue == null) {
                amountValue = 0d;
            }
            if (inputLength > 0) {
                if (locale != null && currency != null) {
                    s = FormatUtil.formatAmount(amountValue, locale, currency.getCurrencyCode(), true, true);
                }
                final int originSeparatorPosition = getDecimalSeparatorPosition(inputString);
                final int cleanedSeparatorPosition = getDecimalSeparatorPosition(s);

                int lengthDif = s.length() - inputLength;

                // --- was a decimal separator symbol added ?
                if (cleanedSeparatorPosition > 0 && originSeparatorPosition < 0) {
                    // --- we apply the decimal block to lengthDif to remove it from the cursorPosition idx
                    // this will make the cursor keep his position right
                    lengthDif -= (s.length() - cleanedSeparatorPosition);
                } else if (cursorPosition > originSeparatorPosition) {
                    lengthDif = 0;
                } else if (lengthDif == 0 && nextChar != null && nextChar.equals(" ")) {
                    // --- 0 = input at the left of a whitespace
                    lengthDif++;
                }
                setText(s);
                setSelection(Math.min(Math.max(cursorPosition + lengthDif, 0), inputLength - 1));
            }
        }

        // --- enable listener
        addTextChangedListener(this);

    }

    private int getDecimalSeparatorPosition(String s) {
        if (s != null && s.length() > 0) {
            final String sSeparator = String.valueOf(decimalSeparator);
            return s.indexOf(sSeparator);
        }
        return -1;
    }

    public Double getAmountValue() {
        return amountValue;
    }
}
