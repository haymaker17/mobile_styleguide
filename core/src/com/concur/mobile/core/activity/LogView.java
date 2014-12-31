/**
 * 
 */
package com.concur.mobile.core.activity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.TimeZone;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.util.Const;
import com.concur.core.R;

/**
 * An activity for viewing/sending application log statements.
 * 
 * @author AndrewK
 */
public class LogView extends Activity {

    private static final String CLS_TAG = LogView.class.getSimpleName();

    private static final int MSG_NEWLINE = 1;

    protected int colorWhiteStripe;
    protected int colorBlueStripe;

    /**
     * A reference to the log reader thread.
     */
    private LogReader logReader;

    /**
     * A reference to the handler for receiving log message updates.
     */
    private final Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MSG_NEWLINE:
                handleMessageNewline(msg);
                break;
            default:
                super.handleMessage(msg);
            }
        }
    };

    private LogMessageAdapter logMsgAdapter;

    private ListView logView;

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.log_view);

        logView = (ListView) findViewById(R.id.log_list_view);
        if (logView != null) {
            logMsgAdapter = new LogMessageAdapter();
            logView.setAdapter(logMsgAdapter);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".onCreate: missing list view!");
        }
        colorWhiteStripe = getResources().getColor(R.color.ListStripeWhite);
        colorBlueStripe = getResources().getColor(R.color.ListStripeBlue);
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onStart()
     */
    @Override
    protected void onStart() {
        super.onStart();
        logReader = new LogReader();
        logReader.start();
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onStop()
     */
    @Override
    protected void onStop() {
        super.onStop();
        logReader.shutdown();
        logReader = null;
    }

    /**
     * Will send the application log to a pre-configured email address.
     * 
     * @param view
     *            the view upon which the click event occurred.
     */
    public void sendLog(View view) {
        ArrayList<SpannableString> logMessages = logMsgAdapter.getLogMessages();
        if (logMessages != null && logMessages.size() > 0) {
            StringBuilder strBldr = new StringBuilder();

            // Add the Concur App version information.
            ConcurCore app = (ConcurCore) getApplication();
            String prodName = app.getProduct().getName();
            StringBuilder versionString;
            PackageInfo pi;
            try {
                pi = app.getPackageManager().getPackageInfo(app.getPackageName(), 0);
                versionString = new StringBuilder(pi.versionName);
                versionString.append(" (").append(pi.versionCode).append(')');
            } catch (NameNotFoundException e) {
                versionString = new StringBuilder();
            }
            strBldr.append("Android ");
            strBldr.append(prodName);
            strBldr.append(" - ");
            strBldr.append(versionString);
            strBldr.append('\n');

            // Add User Information
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            strBldr.append("UserName: ");
            strBldr.append(Preferences.getLogin(prefs, "unknown"));
            strBldr.append('\n');
            strBldr.append("EntityType: ");
            strBldr.append(prefs.getString(Const.PREF_ENTITY_TYPE, "unknown"));
            strBldr.append('\n');

            // Add device information.
            strBldr.append("Build.BOARD: ");
            strBldr.append(Build.BOARD);
            strBldr.append('\n');
            strBldr.append("Build.BRAND: ");
            strBldr.append(Build.BRAND);
            strBldr.append('\n');
            strBldr.append("Build.DEVICE: ");
            strBldr.append(Build.DEVICE);
            strBldr.append('\n');
            strBldr.append("Build.DISPLAY: ");
            strBldr.append(Build.DISPLAY);
            strBldr.append('\n');
            strBldr.append("Build.MANUFACTURER: ");
            strBldr.append(Build.MANUFACTURER);
            strBldr.append('\n');
            strBldr.append("Build.MODEL: ");
            strBldr.append(Build.MODEL);
            strBldr.append('\n');
            strBldr.append("Build.PRODUCT: ");
            strBldr.append(Build.PRODUCT);
            strBldr.append('\n');

            strBldr.append("Build.Version.CODENAME: ");
            strBldr.append(VERSION.CODENAME);
            strBldr.append('\n');
            strBldr.append("Build.Version.RELEASE: ");
            strBldr.append(VERSION.RELEASE);
            strBldr.append('\n');
            strBldr.append("Build.Version.SDK: ");
            switch (VERSION.SDK_INT) {
            case VERSION_CODES.BASE:
                strBldr.append("Android 1.0");
                break;
            case VERSION_CODES.BASE_1_1:
                strBldr.append("Android 1.1");
                break;
            case VERSION_CODES.CUPCAKE:
                strBldr.append("Android 1.5 (Cupcake)");
                break;
            case VERSION_CODES.DONUT:
                strBldr.append("Android 1.6 (Donut)");
                break;
            case 5:
                strBldr.append("Android 2.0 (Eclair)");
                break;
            case 6:
                strBldr.append("Android 2.0.1 (Eclair)");
                break;
            case 7:
                strBldr.append("Android 2.1 (Eclair MR1");
                break;
            case 8:
                strBldr.append("Android 2.2 (Froyo)");
                break;
            case 9:
                strBldr.append("Android 2.3 (Gingerbread)");
                break;
            case 10:
                strBldr.append("Android 2.3.3 (Gingerbread MR1)");
                break;
            case 11:
                strBldr.append("Android 3.0 (Honeycomb)");
                break;
            case 12:
                strBldr.append("Android 3.1 (Honeycomb MR1)");
                break;
            default:
                strBldr.append("unknown (").append(VERSION.SDK_INT).append(")");
                break;
            }
            strBldr.append('\n');

            Locale loc = getResources().getConfiguration().locale;
            strBldr.append("Locale Country: ");
            strBldr.append(loc.getCountry());
            strBldr.append('\n');
            strBldr.append("Locale Language: ");
            strBldr.append(loc.getLanguage());
            strBldr.append('\n');

            strBldr.append("TZ : ");
            strBldr.append(TimeZone.getDefault().getDisplayName(Locale.US));
            strBldr.append('\n');
            strBldr.append("TZ Offset (min): ");
            strBldr.append(TimeZone.getDefault().getRawOffset() / 60000);
            strBldr.append('\n');

            // Iterate through the list of messages and append them to the string.
            Iterator<SpannableString> msgIter = logMessages.iterator();
            while (msgIter.hasNext()) {
                SpannableString spanStr = msgIter.next();
                strBldr.append(spanStr.subSequence(0, spanStr.length()));
                strBldr.append('\n');
            }
            // Send off the message.
            Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
            emailIntent.setType("text/html");
            emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL,
                    new String[] { Const.CONCUR_MOBILE_SUPPORT_EMAIL_ADDRESS });

            emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Android Mobile Log");
            emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, strBldr.toString());
            Intent chooserIntent = Intent.createChooser(emailIntent, "Email Application Log");
            startActivity(chooserIntent);
        }
    }

    /**
     * Handles a new line.
     * 
     * @param msg
     */
    private void handleMessageNewline(Message msg) {
        String line = (String) msg.obj;
        logMsgAdapter.addLogMessage(new LogMessageString(line));
        logMsgAdapter.notifyDataSetChanged();
        logView.setSelection(logMsgAdapter.getCount() - 1);
    }

    /**
     * An extension of <code>BaseAdapter</code> for providing log messages to the view.
     * 
     * @author AndrewK
     */
    class LogMessageAdapter extends BaseAdapter {

        private final String CLS_TAG = LogView.CLS_TAG + "." + LogMessageAdapter.class.getSimpleName();

        ArrayList<SpannableString> logMessages = new ArrayList<SpannableString>();

        /*
         * (non-Javadoc)
         * 
         * @see android.widget.Adapter#getCount()
         */
        public int getCount() {
            return logMessages.size();
        }

        /*
         * (non-Javadoc)
         * 
         * @see android.widget.Adapter#getItem(int)
         */
        public Object getItem(int position) {
            return logMessages.get(position);
        }

        /*
         * (non-Javadoc)
         * 
         * @see android.widget.Adapter#getItemId(int)
         */
        public long getItemId(int position) {
            return position;
        }

        /*
         * (non-Javadoc)
         * 
         * @see android.widget.BaseAdapter#isEnabled(int)
         */
        @Override
        public boolean isEnabled(int position) {
            return false;
        }

        /*
         * (non-Javadoc)
         * 
         * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
         */
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(LogView.this);
                convertView = inflater.inflate(R.layout.log_view_row, null);
            }
            TextView txtView = (TextView) convertView.findViewById(R.id.log_line_number);
            if (txtView != null) {
                txtView.setText(Integer.toString(position));
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".getView: unable to locate log line number view!");
            }
            txtView = (TextView) convertView.findViewById(R.id.log_message);
            if (txtView != null) {
                txtView.setText(logMessages.get(position));
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".getView: unable to locate log messsage view!");
            }
            if ((position % 2) == 0) {
                convertView.setBackgroundColor(colorBlueStripe);
            } else {
                convertView.setBackgroundColor(colorWhiteStripe);
            }
            return convertView;
        }

        /**
         * Adds a log message to the view.
         * 
         * @param logMsg
         */
        public void addLogMessage(SpannableString logMsg) {
            logMessages.add(logMsg);
        }

        /**
         * Gets the list of log messages.
         * 
         * @return the list of log messages.
         */
        public ArrayList<SpannableString> getLogMessages() {
            return logMessages;
        }
    }

    /**
     * An extension of <code>SpannableString</code> for coloring log messages.
     * 
     * @author AndrewK
     */
    private static class LogMessageString extends SpannableString {

        public static final HashMap<Character, Integer> LABEL_COLOR_MAP;

        public LogMessageString(String line) {
            super(line);
            try {

                // First check for the application log tag.
                int tagInd = line.indexOf(Const.LOG_TAG);
                if (tagInd == -1) {
                    // App log tag not found, check for Android runtime tag.
                    tagInd = line.indexOf(LogReader.ANDROID_RUNTIME_TAG);
                }
                if (tagInd != -1 && tagInd > 1) {

                    Integer labelColor = LABEL_COLOR_MAP.get(line.charAt(tagInd - 2));

                    if (labelColor == null) {
                        labelColor = LABEL_COLOR_MAP.get('E');
                    }

                    setSpan(new ForegroundColorSpan(labelColor), tagInd - 2, tagInd - 1, 0);
                    setSpan(new StyleSpan(Typeface.BOLD), tagInd - 2, tagInd - 1, 0);
                    int rightIdx;
                    if ((rightIdx = line.indexOf(':', tagInd)) >= 0) {
                        setSpan(new ForegroundColorSpan(labelColor), tagInd, rightIdx, 0);
                        setSpan(new StyleSpan(Typeface.ITALIC), tagInd, rightIdx, 0);
                    }
                } else {
                    // No-op...can't locate tag! Don't apply any mark-up.
                }
            } catch (RuntimeException e) {
                setSpan(new ForegroundColorSpan(0xffddaacc), 0, length(), 0);
            }
        }

        static {
            LABEL_COLOR_MAP = new HashMap<Character, Integer>();
            LABEL_COLOR_MAP.put('D', 0xff2e8b57); // sea green
            LABEL_COLOR_MAP.put('V', 0xfff4a460); // sandy brown
            LABEL_COLOR_MAP.put('I', 0xffffa07a); // light salmon
            LABEL_COLOR_MAP.put('E', 0xffff0000); // red
            LABEL_COLOR_MAP.put('W', 0xffffff00); // yellow
        }
    }

    /**
     * An thread for the purposes of reading the output from the logcat command.
     * 
     * @author AndrewK
     */
    class LogReader extends Thread {

        public final static String ANDROID_RUNTIME_TAG = "AndroidRuntime";

        public final String[] LOGCAT_CMD =
        // "-d",
        new String[] { "logcat", "-v", "time", ANDROID_RUNTIME_TAG + ":E " + Const.LOG_TAG + ":V *:S" };

        private static final int BUFFER_SIZE = 1024;

        private int mLines = 0;
        protected Process logCatProc = null;

        public void run() {
            try {
                logCatProc = Runtime.getRuntime().exec(LOGCAT_CMD);
            } catch (IOException ioExc) {
                onError("Can't start " + LOGCAT_CMD[0], ioExc);
                return;
            }
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new InputStreamReader(logCatProc.getInputStream()), BUFFER_SIZE);
                String line;
                while ((line = reader.readLine()) != null) {
                    onNewline(line);
                    mLines++;
                }
            } catch (IOException e) {
                onError("Error reading from process " + LOGCAT_CMD[0], e);
            } finally {
                if (reader != null)
                    try {
                        reader.close();
                    } catch (IOException e) {
                    }
                shutdown();
            }
        }

        public void shutdown() {
            if (logCatProc == null)
                return;
            logCatProc.destroy();
            logCatProc = null;
        }

        public int getLineCount() {
            return mLines;
        }

        public void onError(final String msg, Throwable e) {
            runOnUiThread(new Runnable() {

                public void run() {
                    Toast.makeText(LogView.this, msg, Toast.LENGTH_LONG).show();
                }
            });
        }

        public void onNewline(String line) {
            Message msg = mHandler.obtainMessage(MSG_NEWLINE);
            msg.obj = line;
            mHandler.sendMessage(msg);
        }
    }

}
