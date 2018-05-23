package com.example.administrator.camerademo;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

/**
 * Created by Administrator on 2018/5/23.
 */

public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
//        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
//            @Override
//            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
//                Logger.debug(activity.getClass().getName()+ " onActivityCreated");
//            }
//
//            @Override
//            public void onActivityStarted(Activity activity) {
//                Logger.debug(activity.getClass().getName()+ " onActivityStarted");
//            }
//
//            @Override
//            public void onActivityResumed(Activity activity) {
//                Logger.debug(activity.getClass().getName()+ " onActivityResumed");
//            }
//
//            @Override
//            public void onActivityPaused(Activity activity) {
//                Logger.debug(activity.getClass().getName()+ " onActivityPaused");
//            }
//
//            @Override
//            public void onActivityStopped(Activity activity) {
//                Logger.debug(activity.getClass().getName()+ " onActivityStopped");
//            }
//
//            @Override
//            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
//                Logger.debug(activity.getClass().getName()+ " onActivitySaveInstanceState");
//            }
//
//            @Override
//            public void onActivityDestroyed(Activity activity) {
//                Logger.debug(activity.getClass().getName()+ " onActivityDestroyed");
//            }
//        });
    }
}
