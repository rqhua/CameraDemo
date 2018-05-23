package com.example.administrator.camerademo;

import android.content.Context;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

/**
 * Created by Administrator on 2018/5/23.
 */

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private static final String TAG = "CameraPreview";
    private Context mContext;
    //相机操作
    private CameraHelper cameraHelper;
    private SurfaceHolder mHolder;

    private CameraHelper getCameraHelper() {
        return cameraHelper;
    }

    public CameraPreview(Context context) {
        super(context);
        mContext = context;
        mHolder = getHolder();
        mHolder.addCallback(this);
        cameraHelper = new CameraHelper();
        if (!getCameraHelper().checkCameraHardware(context)) {
            Toast.makeText(mContext, "没有相机", Toast.LENGTH_SHORT).show();
            return;
        }
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Logger.debug("surfaceCreated");
        getCameraHelper().setPreviewDisplayAndStart(mHolder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Logger.debug("surfaceChanged");
        //预览大小等发生变化时，先停止预览
        if (mHolder.getSurface() == null) {
            // preview surface does not exist
            return;
        }

        // stop preview before making changes
        try {
            getCameraHelper().stopPreView();
        } catch (Exception e) {
            // ignore: tried to stop a non-existent preview
        }

        //重新设置预览大小等属性
        // set preview size and make any resize, rotate or
        // reformatting changes here

        //新属性开始预览
        // start preview with new settings
        getCameraHelper().startPreview();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Logger.debug("surfaceDestroyed");
        //releasing the Camera preview
        getCameraHelper().stopPreviewAndFreeCamera();
    }

    public void capture(CameraHelper.CaptureCallback callback) {
        getCameraHelper().capture(callback);
    }

    public void onPause() {
        getCameraHelper().stopPreviewAndFreeCamera();
    }

    public void onResume() {
        getCameraHelper().open(new CameraHelper.OpenCallback() {
            @Override
            public void onSuccess() {
                getCameraHelper().setPreviewDisplayAndStart(mHolder);
                Logger.debug("onResume onSuccess: ");
            }

            @Override
            public void onFail() {
                Logger.error("打开相机失败");
            }
        });
    }
}
