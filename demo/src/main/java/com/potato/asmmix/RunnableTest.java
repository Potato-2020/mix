package com.potato.asmmix;


import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;

public class RunnableTest {
    public void runTest() {
        Handler handler = new Handler() {

            @Override
            public void handleMessage(@NonNull Message msg) {
            }
        };
        MyRunnable runnable = new MyRunnable();
        handler.post(runnable);
    }
    public static class MyRunnable implements Runnable {

        @Override
        public void run() {

        }
    }
}
