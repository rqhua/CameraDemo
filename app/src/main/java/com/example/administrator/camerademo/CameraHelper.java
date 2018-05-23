package com.example.administrator.camerademo;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * Created by Administrator on 2018/5/23.
 */

public class CameraHelper {
    private static final String TAG = "CameraHelper";
    private Camera mCamera = null;
    //message.what
    private static final int WHAT_OPEN_CAMERA = 1;

    private static final int FAIL = -1;
    private static final int SUCCESS = 1;
    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            boolean handled = true;
            switch (msg.what) {
                case WHAT_OPEN_CAMERA:
                    if (msg.obj instanceof OpenCallback) {
                        if (msg.arg1 == SUCCESS) {
                            ((OpenCallback) msg.obj).onSuccess(mCamera);
                        } else {
                            ((OpenCallback) msg.obj).onFail();
                        }
                    }
                    break;
                default:
                    handled = false;
            }
            return handled;
        }
    });


    /**
     * A safe way to get an instance of the Camera object.
     */
    public void open(final OpenCallback callback) {
        if (callback == null) {
            throw new NullPointerException("callback Can not be Null");
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                Message msg = new Message();
                msg.what = WHAT_OPEN_CAMERA;
                msg.obj = callback;
                try {
                    // attempt to get a Camera instance
                    mCamera = Camera.open();
                    if (mCamera == null) {
                        msg.arg1 = FAIL;
                    } else {
                        msg.arg1 = SUCCESS;
                    }

                } catch (Exception e) {
                    msg.arg1 = FAIL;
                    Log.e(TAG, "Camera.open() Fail ", e);
                } finally {
                    mHandler.sendMessage(msg);
                }
            }
        }).start();
    }

    /**
     * 检查设备是否是有相机
     */
    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            return true;
        } else {
            return false;
        }
    }

    //打开相机回调
    public interface OpenCallback {
        void onSuccess(Camera camera);

        void onFail();
    }

    //拍照回调
    public interface CaptureCallback {
        void onSuccess();

        void onFail();
    }


}