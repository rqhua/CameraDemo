package com.example.administrator.camerademo;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.Handler;
import android.os.Message;
import android.view.Surface;
import android.view.SurfaceHolder;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Administrator on 2018/5/23.
 */

public class CameraHelper {
    private static final String TAG = "CameraHelper";
    private Activity mActivity;
    private Camera mCamera = null;
    private int mCurrentId = -1;
    private int mWidth;
    private int mHeight;

    private boolean previewing = false;

    public CameraHelper(Activity activity) {
        mActivity = activity;
    }

    /**
     * A safe way to get an instance of the Camera object.
     */
    public void initCamera(int width, int height, final OpenCallback callback) {
        if (callback == null) {
            throw new NullPointerException("callback Can not be Null");
        }
        mWidth = width;
        mHeight = height;
        stopPreviewAndFreeCamera();
        int numberOfCameras = Camera.getNumberOfCameras();
        int back = -1;
        int front = -1;
        if (numberOfCameras > 0) {
            for (int i = 0; i < numberOfCameras; i++) {
                Camera.CameraInfo info = new Camera.CameraInfo();
                Camera.getCameraInfo(i, info);
                if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    back = i;
                } else if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    front = i;
                }
            }
        }

        if (mCurrentId != -1) {
            if (mCurrentId == back || mCurrentId == front) {
                initCamera(mCurrentId, callback);
                return;
            }
        }

        if (back != -1) {
            mCurrentId = back;
        } else if (front != -1) {
            mCurrentId = back;
        }
        if (mCurrentId != -1) {
            initCamera(mCurrentId, callback);
            return;
        }

        callback.onFail();
    }

    //切换摄像头
    public void switchCamera(OpenCallback callback) {
        if (callback == null) {
            throw new NullPointerException("callback Can not be Null");
        }
        stopPreviewAndFreeCamera();
        int numberOfCameras = Camera.getNumberOfCameras();
        int back = -1;
        int front = -1;
        if (numberOfCameras > 0) {
            for (int i = 0; i < numberOfCameras; i++) {
                Camera.CameraInfo info = new Camera.CameraInfo();
                Camera.getCameraInfo(i, info);
                if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    back = i;
                } else if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    front = i;
                }
            }
        }
        if (mCurrentId != -1) {
            if (mCurrentId == back) {
                if (front != -1) {
                    mCurrentId = front;
                }
            } else if (mCurrentId == front) {
                if (back != -1) {
                    mCurrentId = back;
                }
            }
        } else {
            if (front != -1) {
                mCurrentId = front;
            } else if (back != -1) {
                mCurrentId = back;
            }
        }
        if (mCurrentId != -1) {
            initCamera(mCurrentId, callback);
            return;
        }
        callback.onFail();
    }

    //打开相机
    private void initCamera(final int id, final OpenCallback callback) {
        try {
            mCamera = Camera.open(id);
            if (mCamera == null) {
                callback.onFail();
            } else {
                //设置预览方向
                setCameraDisplayOrientation(mActivity, id, mCamera);
                //设置参数
                setParamaters(mCamera);
                callback.onSuccess();
            }
        } catch (Exception e) {
            callback.onFail();
        }
    }

    //设置参数
    public void setParamaters(Camera camera) {
        if (camera == null)
            return;
        Camera.Parameters parameters = camera.getParameters();
        parameters.setPreviewFormat(ImageFormat.NV21);
        parameters.setPictureFormat(ImageFormat.JPEG);
        //场景模式有可能会修改其他的模式的值
        String sceneMode = parameters.getSceneMode();
        if (sceneMode != null && !sceneMode.equals(Camera.Parameters.SCENE_MODE_ACTION)) {
            parameters.setSceneMode(Camera.Parameters.SCENE_MODE_ACTION);
        }
        //闪光模式
        String flashMode = parameters.getFlashMode();
        if (flashMode != null && !flashMode.equals(Camera.Parameters.FLASH_MODE_AUTO)) {
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
        }
        //对焦模式
        String focusMode = parameters.getFocusMode();
        if (!focusMode.equals(Camera.Parameters.FOCUS_MODE_AUTO)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        }

        List<Camera.Size> supportedPreviewSizes = parameters.getSupportedPreviewSizes();
        //设置预览尺寸，选择SurfaceView的宽高比例，与支持的宽高比例最相近的一组
        Camera.Size size = getOptimalPreviewSize(supportedPreviewSizes, mWidth, mHeight);
        if (size != null)
            parameters.setPreviewSize(size.width, size.height);
        camera.setParameters(parameters);
    }

    //设置预览方向
    public void setCameraDisplayOrientation(Activity activity, int cameraId, Camera camera) {
        try {
            if (camera == null)
                return;
            if (activity == null)
                return;
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(cameraId, info);
            int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
            int degrees = 0;
            switch (rotation) {
                case Surface.ROTATION_0:
                    degrees = 0;
                    break;
                case Surface.ROTATION_90:
                    degrees = 90;
                    break;
                case Surface.ROTATION_180:
                    degrees = 180;
                    break;
                case Surface.ROTATION_270:
                    degrees = 270;
                    break;
            }


            int result;
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                result = (info.orientation + degrees) % 360;
                result = (360 - result) % 360;  // compensate the mirror
            } else {  // back-facing
                result = (info.orientation - degrees + 360) % 360;
            }
            Logger.debug("degress " + degrees);
            Logger.debug("CameraInfo.orientation " + info.orientation);
            Logger.debug("result " + result);
            camera.setDisplayOrientation(result);
        } catch (Exception e) {
            Logger.error("setCameraDisplayOrientation ", e);
        }
    }

    //开启预览
    public void startPreview() {
        if (mCamera != null && !previewing) {
            Logger.debug("startPreview");
            mCamera.startPreview();
            previewing = true;
            mCamera.autoFocus(new Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean success, Camera camera) {
                    Logger.debug("onAutoFocus " + (success ? "success" : "fail"));
                }
            });
        }
    }

    public void setPreviewDisplayAndStart(SurfaceHolder holder) {

        try {
            if (mCamera != null) {
                mCamera.setPreviewDisplay(holder);
                startPreview();
                Logger.debug("setPreviewDisplayAndStart");
            }
        } catch (IOException e) {
            e.printStackTrace();
            Logger.error("setDisplayAndStart: ", e);
        } catch (Exception e) {
            Logger.error("setDisplayAndStart: ", e);
        }
    }

    //停止预览
    public void stopPreView() {
        if (mCamera != null && previewing) {
            Logger.debug("stopPreView");
            // Call stopPreview() to stop updating the preview surface.
            mCamera.stopPreview();
            previewing = false;
        }
    }

    //停止预览并释放相机
    public void stopPreviewAndFreeCamera() {
        try {
            if (mCamera != null) {
                // Call stopPreview() to stop updating the preview surface.
                stopPreView();
                // Important: Call release() to release the camera for use by other
                // applications. Applications should release the camera immediately
                // during onPause() and re-onResume() it during onResume()).
                mCamera.release();
                mCamera = null;
                Logger.debug("stopPreviewAndFreeCamera");
            }
        } catch (Exception e) {
            Logger.error("stopPreviewAndFreeCamera ", e);
        }
    }

    //拍照
    public void capture(final CaptureCallback callback) {
        if (!previewing) {
            startPreview();
            return;
        }
        if (callback == null) {
            throw new NullPointerException("callback Can not be Null");
        }
        if (mCamera == null) {
            Logger.error("capture: Fail,Camera == null");
            callback.onFail();
            return;
        }

        mCamera.takePicture(null, null, new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                Logger.debug("onPictureTaken: Success");
                previewing = false;
                callback.onSuccess(data);
            }
        });
    }

    private static final int MAX_PREVIEW_WIDTH = 1920;

    private static final int MAX_PREVIEW_HEIGHT = 1080;

    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int width, int height) {
        int w = Math.max(width, height);
        int h = Math.min(width, height);
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) w / h;
        if (sizes == null) return null;
        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;
        int targetHeight = h;
        // Try to find an size match aspect ratio and size
        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) {
                Logger.debug("continue");
                continue;
            }
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
                Logger.debug("minDiff " + minDiff);
            }
        }
        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    public Camera.Size chooseOptimalSize(Activity activity, List<Camera.Size> choices, int viewWidth, int viewHeight) {
        if (mCamera == null)
            return null;
        Logger.debug("viewWidth " + viewWidth);
        Logger.debug("viewHeight " + viewHeight);
        boolean swappedDimensions = false;
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        switch (rotation) {
            case Surface.ROTATION_0:
            case Surface.ROTATION_180:
                swappedDimensions = true;
                Logger.debug("width <> height");
                break;
            case Surface.ROTATION_90:
            case Surface.ROTATION_270:
                Logger.debug("width no height");
                break;
        }
        int rotatedPreviewWidth = viewWidth;
        int rotatedPreviewHeight = viewHeight;
        if (swappedDimensions) {
            rotatedPreviewWidth = viewHeight;
            rotatedPreviewHeight = viewWidth;
        }
        for (Camera.Size option : choices) {
            Logger.debug("option.width " + option.width);
            Logger.debug("option.height " + option.height);
        }
        Camera.Size size = CamParaUtil.getInstance().getOptimalSize(choices, rotatedPreviewWidth, rotatedPreviewHeight);
        Logger.debug("size.width " + size.width);
        Logger.debug("size.height " + size.height);
//        mCamera.setParameters(parameters);
        return size;
    }

    static class CompareSizesByArea implements Comparator<Camera.Size> {

        @Override
        public int compare(Camera.Size lhs, Camera.Size rhs) {
            // We cast here to ensure the multiplications won't overflow
            return Long.signum((long) lhs.width * lhs.height - (long) rhs.width * rhs.height);
        }

    }

    /**
     * 检查设备是否是有相机
     */
    public boolean checkCameraHardware(Context context) {
        if (context == null)
            throw new NullPointerException("Context can not be null");
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
        void onSuccess(byte[] data);

        void onFail();
    }
}