package com.reeman.basebigman.util;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.Thread.UncaughtExceptionHandler;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CrashExceptionHandler implements UncaughtExceptionHandler {
    private static CrashExceptionHandler carshInstance;

    @Override
    public void uncaughtException(Thread td, Throwable tb) {
        writeToSDCard(td, tb);
        if (td.getId() == 1) {// UI异常
            Log.e("UI异常====", tb.getMessage());
            tb.printStackTrace();
        } else {
            Log.e("其他异常====", tb.getMessage());
            tb.printStackTrace();
        }
        System.exit(1);
    }

    public void init() {
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    public void writeToSDCard(Thread td, Throwable tb) {
        boolean sdCardExist = Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);
        if (sdCardExist) {
            File logFile = new File(Environment.getExternalStorageDirectory()
                    .getPath() + "/reeman/crashlog");
            if (!logFile.exists()) {
                if (!logFile.mkdirs()) {
                    System.out.println("create crash file fail");
                }
            }
            String logPath = Environment.getExternalStorageDirectory()
                    .getPath()
                    + "/reeman/crashlog/"
                    + new SimpleDateFormat("yyMMddHHmmss", Locale.getDefault())
                    .format(new Date()) + ".txt";
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(logPath);
                PrintStream ps = new PrintStream(fos);
                tb.printStackTrace(ps);
                ps.flush();
                ps.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public CrashExceptionHandler () {

    }

    public static CrashExceptionHandler getCrashInstance() {
        if (carshInstance == null) {
            carshInstance = new CrashExceptionHandler();
        }
        return carshInstance;
    }
}
