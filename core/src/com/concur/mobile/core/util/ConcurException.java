package com.concur.mobile.core.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.Thread.UncaughtExceptionHandler;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.util.Log;

import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.service.ConcurService;

public class ConcurException {

    private static final String STACK_DIR = "stacks";

    private static class ConcurUncaughtExceptionHandler implements UncaughtExceptionHandler {

        private final File filesDir;
        private final String versionName;
        private final UncaughtExceptionHandler originalHandler;

        public ConcurUncaughtExceptionHandler(File filesDir, String versionName,
                UncaughtExceptionHandler originalHandler) {

            this.filesDir = filesDir;
            this.originalHandler = originalHandler;

            this.versionName = versionName;

            // Setup the directory for stack saves
            File dir = new File(filesDir, STACK_DIR);
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    Log.e(Const.LOG_TAG, "Unable to initialize stack log directory!");
                }
            }

        }

        @Override
        public void uncaughtException(Thread thread, Throwable ex) {
            try {
                writeException(ex);
            } catch (Exception e) {
                // Nothing to be done. Log it.
                Log.e(Const.LOG_TAG, "Failed to write stack file", e);
            }

            if (originalHandler != null) {
                originalHandler.uncaughtException(thread, ex);
            }
        }

        private void writeException(Throwable t) throws IOException {

            File dir = new File(filesDir, STACK_DIR);
            if (dir.exists()) {
                String fileName = Long.toString(System.currentTimeMillis());
                File stackFile = new File(dir, fileName);
                BufferedWriter bw = null;
                try {
                    bw = new BufferedWriter(new FileWriter(stackFile));
                    bw.write(versionName);
                    bw.newLine();
                    t.printStackTrace(new PrintWriter(bw));
                } finally {
                    if (bw != null) {
                        bw.close();
                    }
                }
            }
        }
    }

    public static UncaughtExceptionHandler getUncaughtExceptionHandler(Context ctx,
            UncaughtExceptionHandler originalHandler) {

        String versionName;
        try {
            versionName = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0).versionName;
        } catch (NameNotFoundException e) {
            versionName = "0.0.0";
        }

        return new ConcurUncaughtExceptionHandler(ctx.getFilesDir(), versionName, originalHandler);
    }

    public static void processSavedExceptions(ConcurCore app) {

        File filesDir = app.getFilesDir();
        // Setup the directory for stack saves
        File stackDir = new File(filesDir, STACK_DIR);
        if (!stackDir.exists()) {
            stackDir = null;
        }

        new AsyncTask<Object, Void, Void>() {

            @Override
            protected Void doInBackground(Object... params) {
                ConcurService service = (ConcurService) params[0];
                // Iterate the stack files and upload any of them
                File stackDir = (File) params[1];
                if (stackDir != null) {
                    File[] files = stackDir.listFiles();
                    if (files != null && files.length > 0) {
                        for (File stackFile : files) {
                            BufferedReader br = null;
                            try {
                                br = new BufferedReader(new FileReader(stackFile));

                                // Start the read. The first line is the version
                                // name.
                                String line = br.readLine();
                                String version = line;
                                StringBuilder stackSB = new StringBuilder();
                                while (line != null) {
                                    line = br.readLine();
                                    if (line != null) {
                                        stackSB.append(line).append('\n');
                                    }
                                }
                                String stack = stackSB.toString();

                                // Close up and fire the crash over the wall.
                                br.close();
                                stackFile.delete();
                                service.sendPostCrashLogRequest(version, stack);
                            } catch (Exception e) {
                                // Ah well.
                            }
                        }
                    }
                }
                return null;
            }
        }.execute(app.getService(), stackDir);
    }
}
