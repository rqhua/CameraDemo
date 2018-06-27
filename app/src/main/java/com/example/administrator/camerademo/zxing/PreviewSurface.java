package com.example.administrator.camerademo.zxing;

import android.app.Activity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import com.example.administrator.camerademo.Logger;


/**
 * Created by Administrator on 2018/5/23.
 */

public class PreviewSurface extends SurfaceView implements SurfaceHolder.Callback {
    private static final String TAG = "CameraPreview";
    private Activity mContext;
    //相机操作
    private PreviewCameraHelper cameraHelper;
    private SurfaceHolder mHolder;
    private int mWidth;
    private int mHeight;

    private PreviewCameraHelper getCameraHelper() {
        return cameraHelper;
    }

    public PreviewSurface(Activity context) {
        super(context);
        mContext = context;
        this.cameraHelper = new PreviewCameraHelper(context);
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
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Logger.debug("surfaceDestroyed");
        getCameraHelper().stopPreviewAndFreeCamera();
    }

    private PreviewCameraHelper.OpenCallback openCallback = new PreviewCameraHelper.OpenCallback() {
        @Override
        public void onSuccess() {
            getCameraHelper().setPreviewDisplayAndStart(mHolder);
            Logger.debug("OpenCallback.onSuccess()");
            requestPreviewFrame(previewCallback);
        }

        @Override
        public void onFail() {
            Logger.error("OpenCallback.onFail()");
        }
    };


    private PreviewCallback previewCallback;

    public PreviewCallback getPreviewCallback() {
        return previewCallback;
    }

    public void setPreviewCallback(PreviewCallback previewCallback) {
        this.previewCallback = previewCallback;
    }

    public void requestPreviewFrame(PreviewCallback callback) {
        getCameraHelper().setOneShotPreviewCallback(callback);
    }

    public void capture(PreviewCameraHelper.CaptureCallback callback) {
        getCameraHelper().capture(callback);
    }

    public void stop(){
        getCameraHelper().stopPreviewAndFreeCamera();
    }

    public void switchCamer() {
        getCameraHelper().switchCamera(openCallback);
    }
}
