package com.example.administrator.camerademo.zxing;

import android.app.Activity;
import android.graphics.Rect;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
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

    private View scanCropView;
    private Rect mCropRect = null;

    private PreviewCameraHelper getCameraHelper() {
        return cameraHelper;
    }

    public PreviewSurface(Activity context, View scanCropView) {
        super(context);
        mContext = context;
        this.cameraHelper = new PreviewCameraHelper(context);
        if (!getCameraHelper().checkCameraHardware(mContext)) {
            Toast.makeText(mContext, "没有相机", Toast.LENGTH_SHORT).show();
            return;
        }

        this.scanCropView = scanCropView;
        mHolder = getHolder();
        mHolder.addCallback(this);
    }

    private void initCrop() {

        /** 获取布局中扫描框的位置信息 */
        int[] location = new int[2];
        mCropRect = new Rect();
//        scanCropView.getDrawingRect(mCropRect);
//        if (true)
//            return;
        scanCropView.getLocationInWindow(location);
        Logger.debug("scanCropView location[0] = " + location[0]);
        Logger.debug("scanCropView location[1] = " + location[1]);
        int cropLeft = location[0];
        int cropTop = location[1];
//        int cropTop = location[1] - getStatusBarHeight();

        int cropWidth = scanCropView.getMeasuredWidth();
        int cropHeight = scanCropView.getMeasuredHeight();

//        /** 获取布局容器的宽高 */
//        int containerWidth = scanContainer.getWidth();
//        int containerHeight = scanContainer.getHeight();
//
//        /** 计算最终截取的矩形的左上角顶点x坐标 */
//        int x = cropLeft * cameraWidth / containerWidth;
//        /** 计算最终截取的矩形的左上角顶点y坐标 */
//        int y = cropTop * cameraHeight / containerHeight;
//
//        /** 计算最终截取的矩形的宽度 */
//        int width = cropWidth * cameraWidth / containerWidth;
//        /** 计算最终截取的矩形的高度 */
//        int height = cropHeight * cameraHeight / containerHeight;

        /** 计算最终截取的矩形的左上角顶点x坐标 */
//        int x = cropLeft;
        /** 计算最终截取的矩形的左上角顶点y坐标 */
//        int y = cropTop;

        /** 计算最终截取的矩形的宽度 */
//        int width = cropWidth;
        /** 计算最终截取的矩形的高度 */
//        int height = cropHeight;

        /** 生成最终的截取的矩形 */
        mCropRect = new Rect(cropLeft, cropTop, cropWidth + cropLeft, cropHeight + cropTop);
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
        initCrop();
        getCameraHelper().initCamera(mWidth, mHeight, mCropRect, openCallback);

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
        getCameraHelper().requestPreviewFrame(callback);
    }

    public void capture(PreviewCameraHelper.CaptureCallback callback) {
        getCameraHelper().capture(callback);
    }

    public void stop() {
        getCameraHelper().stopPreView();
    }

    public void switchCamer() {
        getCameraHelper().switchCamera(openCallback);
    }
}
