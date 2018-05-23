package com.example.administrator.camerademo;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;

/**
 * Created by Rqhua on 2018/1/19.
 */
public class PermissionCompatUtil {
    private static final int REQUEST_CODE_CAMERA = 999;
    private static final int REQUEST_CODE_WRITE = 998;
    private static OnPermissionCallback mPermissionCallback;


    /**
     * 检测摄像头权限 没有就会申请
     *
     * @param context
     * @param callback 申请权限的结果回调
     * @return
     */
    public static boolean checkCameraPermission(Context context, OnPermissionCallback callback) {
        mPermissionCallback = callback;
        boolean granted = true;
        //魅族或者6.0以下
        if (isFlyme() || Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            Camera mCamera = null;
            try {
                mCamera = Camera.open();
                // setParameters 是针对魅族MX5 做的。MX5 通过Camera.open() 拿到的Camera
                // 对象不为null
                Camera.Parameters mParameters = mCamera.getParameters();
                mCamera.setParameters(mParameters);
            } catch (Exception e) {
                granted = false;
            }
            if (mCamera != null) {
                mCamera.release();
            }
        } else {
            granted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PermissionChecker.PERMISSION_GRANTED;
        }
        if (granted) {
            if (mPermissionCallback != null) {
                mPermissionCallback.onGrantCameraResult(true);
            }
        } else {
            if (context instanceof Activity) {
                ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.CAMERA}, REQUEST_CODE_CAMERA);
            }
        }
        return granted;
    }

    public static boolean checkWritePermission(Context context, OnPermissionCallback callback) {
        mPermissionCallback = callback;
        boolean granted = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PermissionChecker.PERMISSION_GRANTED;
        if (granted) {
            if (mPermissionCallback != null) {
                mPermissionCallback.onGrantWriteResult(true);
            }
        } else {
            if (context instanceof Activity) {
                ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_WRITE);
            }
        }
        return granted;
    }

    public static void onRequestPermissionsResult(Context context, int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_WRITE:
                if (mPermissionCallback != null) {
                    mPermissionCallback.onGrantWriteResult(checkWritePermission(context, null));
                }
                break;
            case REQUEST_CODE_CAMERA:
                if (mPermissionCallback != null) {
                    mPermissionCallback.onGrantCameraResult(checkCameraPermission(context, null));
                }
                break;
        }
    }

    private static boolean isFlyme() {
        if (Build.BRAND.contains("Meizu")) {
            return true;
        } else {
            return false;
        }
    }

    public static abstract class OnPermissionCallback {
        public void onGrantCameraResult(boolean granted) {
        }

        public void onGrantWriteResult(boolean granted) {
        }
    }
}
