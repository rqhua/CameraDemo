package com.example.administrator.camerademo;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Handler;
import android.os.Message;
import android.view.SurfaceHolder;

import java.io.IOException;

/**
 * Created by Administrator on 2018/5/23.
 */

public class CameraHelper {
    private static final String TAG = "CameraHelper";
    private Camera mCamera = null;
    private boolean opened = false;
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
                            opened = true;
                            ((OpenCallback) msg.obj).onSuccess();
                        } else {
                            opened = false;
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
                    Logger.error("Camera.onResume() Fail ", e);
                } finally {
                    mHandler.sendMessage(msg);
                }
            }
        }).start();
    }

    public void startPreview() {
        if (mCamera != null) {
            mCamera.startPreview();
        }
    }

    //开启预览
    public void setPreviewDisplayAndStart(SurfaceHolder holder) {

        try {
            if (mCamera != null) {
                mCamera.setPreviewDisplay(holder);
                mCamera.startPreview();
                Logger.debug("setPreviewDisplayAndStart");
            }
        } catch (IOException e) {
            e.printStackTrace();
            Logger.error("setDisplayAndStart: ", e);
        } catch (Exception e) {
            Logger.error("setDisplayAndStart: ", e);
        }

    }

    public void stopPreView() {
        if (mCamera != null) {
            Logger.debug("stopPreView");
            // Call stopPreview() to stop updating the preview surface.
            mCamera.stopPreview();
        }
    }

    //停止预览并释放相机
    public void stopPreviewAndFreeCamera() {

        if (mCamera != null) {
            // Call stopPreview() to stop updating the preview surface.
            mCamera.stopPreview();
            // Important: Call release() to release the camera for use by other
            // applications. Applications should release the camera immediately
            // during onPause() and re-onResume() it during onResume()).
            mCamera.release();
            opened = false;
            mCamera = null;
            Logger.debug("stopPreviewAndFreeCamera");
        }
    }

    public void takePicture(final CaptureCallback callback) {
        if (callback == null) {
            throw new NullPointerException("callback Can not be Null");
        }
        if (mCamera == null) {
            Logger.error("takePicture: Fail,Camera == null");
            callback.onFail();
            return;
        }
        mCamera.takePicture(null, null, new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                Logger.debug("onPictureTaken: Success");
                /*File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
                if (pictureFile == null){
                    Log.d(TAG, "Error creating media file, check storage permissions: " +
                            e.getMessage());
                    return;
                }

                try {
                    FileOutputStream fos = new FileOutputStream(pictureFile);
                    fos.write(data);
                    fos.close();
                } catch (FileNotFoundException e) {
                    Log.d(TAG, "File not found: " + e.getMessage());
                } catch (IOException e) {
                    Log.d(TAG, "Error accessing file: " + e.getMessage());
                }*/
                callback.onSuccess();
            }
        });
    }

    /**
     * 检查设备是否是有相机
     */
    public boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            return true;
        } else {
            return false;
        }
    }

    //打开相机回调
    public interface OpenCallback {
        void onSuccess();

        void onFail();
    }

    //拍照回调
    public interface CaptureCallback {
        void onSuccess();

        void onFail();
    }
}