package com.example.administrator.camerademo.define;

import android.app.Activity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import com.example.administrator.camerademo.Logger;


/**
 * Created by Administrator on 2018/5/23.
 */

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private static final String TAG = "CameraPreview";
    private Activity mContext;
    //相机操作
    private CameraHelper cameraHelper;
    private SurfaceHolder mHolder;
    private int mWidth;
    private int mHeight;

    private CameraHelper getCameraHelper() {
        return cameraHelper;
    }

    public CameraPreview(Activity context) {
        super(context);
        mContext = context;
        this.cameraHelper = new CameraHelper(context);
        if (!getCameraHelper().checkCameraHardware(mContext)) {
            Toast.makeText(mContext, "没有相机", Toast.LENGTH_SHORT).show();
            return;
        }

        mHolder = getHolder();
        mHolder.addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Logger.debug("surfaceCreated");
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Logger.debug("surfaceChanged");
        //预览大小等发生变化时，先停止预览
        if (mHolder.getSurface() == null) {
            // preview surface does not exist
            return;
        }
        getCameraHelper().stopPreviewAndFreeCamera();
        mWidth = width;
        mHeight = height;
        getCameraHelper().initCamera(mWidth, mHeight, openCallback);

        mWidth = -1;
        mHeight = -1;
        // stop preview before making changes


        //重新设置预览大小等属性
        // set preview size and make any resize, rotate or
        // reformatting changes here

        //新属性开始预览
        // start preview with new settings
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Logger.debug("surfaceDestroyed");
        //releasing the Camera preview
        getCameraHelper().stopPreviewAndFreeCamera();
    }

    private CameraHelper.OpenCallback openCallback = new CameraHelper.OpenCallback() {
        @Override
        public void onSuccess() {
            getCameraHelper().setPreviewDisplayAndStart(mHolder);
            Logger.debug("OpenCallback.onSuccess()");
        }

        @Override
        public void onFail() {
            Logger.error("OpenCallback.onFail()");
        }
    };

    public void capture( CameraHelper.CaptureCallback callback) {
        getCameraHelper().capture( callback);
    }


    public void switchCamer() {
        getCameraHelper().switchCamera(openCallback);
    }
}
