package com.example.administrator.camerademo;

import android.util.Log;

public class Logger {

    private static int level = Log.DEBUG;
    private static boolean debug = true;
    private static boolean debugThis = false;

    public static void debug(String msg) {
        if (!debug)
            return;
        if (debugThis)
            Log.d("Logger", msg);
        if (level <= Log.DEBUG) {
            String tag = createTag();
            int maxLen = 2001 - tag.length();
            //大于4000时
            while (msg.length() > maxLen) {
                Log.d(tag, msg.substring(0, maxLen));
                msg = msg.substring(maxLen);
            }
            //剩余部分
            Log.d(tag, msg);
        }
    }

    public static void debug(String msg, Throwable tr) {
        if (!debug)
            return;

        if (debugThis)
            Log.d("Logger", msg, tr);
        if (level <= Log.DEBUG) {
            String tag = createTag();
            int maxLen = 2001 - tag.length();
            //大于4000时
            while (msg.length() > maxLen) {
                Log.d(tag, msg.substring(0, maxLen), tr);
                msg = msg.substring(maxLen);
            }
            //剩余部分
            Log.d(tag, msg, tr);
        }
    }

    public static void info(String msg) {
        if (!debug)
            return;
        if (level <= Log.INFO) {
            String tag = createTag();
            int maxLen = 2001 - tag.length();
            //大于4000时
            while (msg.length() > maxLen) {
                Log.i(tag, msg.substring(0, maxLen));
                msg = msg.substring(maxLen);
            }
            //剩余部分
            Log.i(tag, msg);
        }
    }

    public static void info(String msg, Throwable tr) {
        if (!debug)
            return;
        if (level <= Log.INFO) {
            String tag = createTag();
            int maxLen = 2001 - tag.length();
            //大于4000时
            while (msg.length() > maxLen) {
                Log.i(tag, msg.substring(0, maxLen), tr);
                msg = msg.substring(maxLen);
            }
            //剩余部分
            Log.i(tag, msg, tr);
        }
    }

    public static void warn(String msg) {
        if (!debug)
            return;
        if (level <= Log.WARN) {
            String tag = createTag();
            int maxLen = 2001 - tag.length();
            //大于4000时
            while (msg.length() > maxLen) {
                Log.w(tag, msg.substring(0, maxLen));
                msg = msg.substring(maxLen);
            }
            //剩余部分
            Log.w(tag, msg);
        }
    }

    public static void warn(String msg, Throwable tr) {
        if (!debug)
            return;
        if (level <= Log.WARN) {
            String tag = createTag();
            int maxLen = 2001 - tag.length();
            //大于4000时
            while (msg.length() > maxLen) {
                Log.w(tag, msg.substring(0, maxLen), tr);
                msg = msg.substring(maxLen);
            }
            //剩余部分
            Log.w(tag, msg, tr);
        }
    }

    public static void error(String msg) {
        if (!debug)
            return;
        if (debugThis)
            Log.e("Logger", msg);
        if (level <= Log.ERROR){
            String tag = createTag();
            int maxLen = 2001 - tag.length();
            //大于4000时
            while (msg.length() > maxLen) {
                Log.e(tag, msg.substring(0, maxLen));
                msg = msg.substring(maxLen);
            }
            //剩余部分
            Log.e(tag, msg);
        }
    }

    public static void error(String msg, Throwable tr) {
        if (!debug)
            return;
        if (debugThis)
            Log.e("Logger", msg, tr);
        if (level <= Log.ERROR){
            String tag = createTag();
            int maxLen = 2001 - tag.length();
            //大于4000时
            while (msg.length() > maxLen) {
                Log.e(tag, msg.substring(0, maxLen), tr);
                msg = msg.substring(maxLen);
            }
            //剩余部分
            Log.e(tag, msg, tr);
        }
    }

    private static String createTag() {
        StackTraceElement[] sts = Thread.currentThread().getStackTrace();
        if (sts == null) {
            return null;
        }
        for (StackTraceElement st : sts) {
            if (st.isNativeMethod()) {
                continue;
            }
            if (st.getClassName().equals(Thread.class.getName())) {
                continue;
            }
            if (st.getClassName().equals(Logger.class.getName())) {
                continue;
            }
            return "Logger-" + st.getFileName() + ":" + st.getLineNumber();
        }
        return "";
    }

    public static void i(String tag, String msg) {  //信息太长,分段打印
        //因为String的length是字符数量不是字节数量所以为了防止中文字符过多，
        //  把4*1024的MAX字节打印长度改为2001字符数
        int maxLen = 2001 - tag.length();
        //大于4000时
        while (msg.length() > maxLen) {
            Log.i(tag, msg.substring(0, maxLen));
            msg = msg.substring(maxLen);
        }
        //剩余部分
        Log.i(tag, msg);
    }

}
