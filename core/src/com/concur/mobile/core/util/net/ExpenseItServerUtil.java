/*
* Copyright (c) 2015 Concur Technologies, Inc.
*/
package com.concur.mobile.core.util.net;

import android.content.Context;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.util.Pair;

import com.concur.mobile.core.util.Const;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.util.UUID;

public class ExpenseItServerUtil {

    private static String appId;

    private static String installId;

    private static final String FILE_INSTALLATION_ID = "installationID";

    public enum Server {
        PRODUCTION("Production", "https://api.expenseit.com", "M0NBOTc3QTMzQkI0MzhGRTdBM0EzQUQ2MUE0Q0M4NTg="),
        DEV("Development", "https://api.dev.expenseit.com", "QTJCMURBQjZDMzM5NjczQ0I4RDQwQUMzQjhDNTFGNEE="),
        RQA2("RQA2", "https://api-rqa2-expenseit.concurtech.net", "OTRDOEExRjUzODFCN0UzQUY5QjQ4QUQyMTZDN0E5RDE="),
        RQA3("RQA3", "https://api-rqa3-expenseit.concurtech.net", "MEE1NDc3QjI3RkY2NjQ5NzBGQTYwMEEzNjBGQjU3NkE="),
        RQA5("RQA5", "https://api-rqa5-expenseit.concurtech.net", "RDRDNzc5NTUxMTIxMTRDRjc2RDUyNjU2M0E4NEU3RTg="),
        RQA6("RQA6", "https://api-rqa6-expenseit.concurtech.net", "MDk4MzRFRDk2QUUzOEVFNUEwRjYxOTcxOTMyQTM4QTU="),
        RQA7("RQA7", "https://api-rqa7-expenseit.concurtech.net", "RkE3N0U1OEQ1RDU5QjMyMTE4QTdCNzE5NkRGMkI0QTc="),
        DEMO("DEMO", "http://10.25.47.189:9000", "OTRDOEExRjUzODFCN0UzQUY5QjQ4QUQyMTZDN0E5RDE="),
        BAT("BAT", "https://api-bat-expenseit.concurtech.net", "QzE2M0M1QzdEMDA3REU5QTM5MkYxRDk5NTc2MjA0Rjg=");

        private final String name;
        private final String server;
        private final String key;

        private Server(String name, String server, String key) {
            this.name = name;
            this.server = server;
            this.key = key;
        }

        public static Server getExpenseItServer(Server name) {
            for (Server server : values()) {
                if (server == name) {
                    return server;
                }
            }
            return PRODUCTION;
        }

        public String getName() {
            return name;
        }

        public String getServer() {
            return server;
        }

        public String getKey() {
            return unobfuscate(key);
        }

        private static String unobfuscate(String obfuscated) {
            String decoded = null;
            try {
                decoded = new String(decode(obfuscated), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                // don't know what i should do here
                // UTF-8 is supported
                e.printStackTrace();
            }
            return flip(decoded != null ? decoded.toCharArray() : new char[0]);
        }

        private static byte[] decode(String encoded) {
            return Base64.decode(encoded, Base64.DEFAULT);
        }

        private static String flip(char[] obfuscated) {
            int j = obfuscated.length / 2;
            for (int i = 0; i < j; i++, j--) {
                char tmp = obfuscated[j];
                obfuscated[j] = obfuscated[i];
                obfuscated[i] = tmp;
            }

            return new String(obfuscated);
        }

        public String toString() {
            return getName();
        }
    }

    public static Pair<String, String> getMatchingConcurExpenseItServer(String server) {
        Server useServer;
        if (TextUtils.isEmpty(server)) {
            useServer = Server.PRODUCTION;
        }
        else {
            String concurServer = server.toLowerCase();
            if (concurServer.indexOf("rqa2") != -1) {
                useServer = Server.RQA2;
            } else if (concurServer.indexOf("rqa3") != -1) {
                useServer = Server.RQA3;
            } else if (concurServer.indexOf("rqa5") != -1) {
                useServer = Server.RQA5;
            } else if (concurServer.indexOf("rqa6") != -1) {
                useServer = Server.RQA6;
            } else if (concurServer.indexOf("rqa7") != -1) {
                useServer = Server.RQA7;
            } else if (concurServer.indexOf("bat") != -1) {
                useServer = Server.BAT;
            } else if (concurServer.indexOf("demo") != -1) {
                useServer = Server.DEMO;
            } else if (concurServer.indexOf("dev") != -1) {
                useServer = Server.DEV;
            } else {
                useServer = Server.PRODUCTION;
            }
        }
        return getExpenseItServer(useServer);
    }

    public static Pair<String, String> getExpenseItServer(Server server) {
        Server expenseIt = Server.getExpenseItServer(server);
        return new Pair<>(expenseIt.getServer(), expenseIt.getKey());
    }

    /*
    * XXX: http://android-developers.blogspot.in/2011/03/identifying-app-installations.html
    */
    public static String getAppId(Context context) {
        if (appId != null) {
            return appId;
        }

        Log.v(Const.LOG_TAG, "getAppID: Attempting Secure.ANDROID_ID");
        appId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        if (TextUtils.isEmpty(appId)) {
            return appId;
        }

        try {
            Log.v(Const.LOG_TAG, "getAppID: Attempting InstallationId");
            appId = getInstallID(context);
            if (appId != null) {
                return appId;
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to find a suitable AppID: " + e.toString());
        }

        throw new RuntimeException("Unable to find a suitable AppID: all methods failed");
    }

    private synchronized static String getInstallID(Context context) {
        if (installId == null) {
            File installation = new File(context.getFilesDir(), FILE_INSTALLATION_ID);
            try {
                if (!installation.exists()) {
                    writeInstallationFile(installation);
                }
                installId = readInstallationFile(installation);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return installId;
    }

    private static String readInstallationFile(File installation) throws IOException {
        RandomAccessFile f = new RandomAccessFile(installation, "r");
        byte[] bytes = new byte[(int) f.length()];
        f.readFully(bytes);
        f.close();
        return new String(bytes);
    }

    private static void writeInstallationFile(File installation) throws IOException {
        FileOutputStream out = new FileOutputStream(installation);
        String id = UUID.randomUUID().toString();
        out.write(id.getBytes());
        out.close();
    }

}
