package com.example.administrator.camerademo.zxing;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.view.Surface;
import android.view.SurfaceHolder;

import com.example.administrator.camerademo.Logger;
import com.example.administrator.camerademo.define.LubanCompress;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created by Administrator on 2018/5/23.
 */

public class PreviewCameraHelper {
    private static final String TAG = "CameraHelper";
    private Activity mActivity;
    private Camera mCamera = null;
    private int displayOrientation = -1;
    private int mCurrentId = -1;
    private int mWidth;
    private int mHeight;

    private boolean isFrontCamera = false;

    private boolean previewing = false;

    public PreviewCameraHelper(Activity activity) {
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
                if (mCurrentId == front)
                    isFrontCamera = true;
                initCamera(mCurrentId, callback);
                return;
            }
        }

        if (back != -1) {
            isFrontCamera = false;
            mCurrentId = back;
        } else if (front != -1) {
            isFrontCamera = true;
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
                    isFrontCamera = true;
                }
            } else if (mCurrentId == front) {
                if (back != -1) {
                    mCurrentId = back;
                    isFrontCamera = false;
                }
            }
        } else {
            if (front != -1) {
                mCurrentId = front;
                isFrontCamera = true;
            } else if (back != -1) {
                mCurrentId = back;
                isFrontCamera = false;
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
            Logger.error("", e);
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
        List<String> supportedSceneModes = parameters.getSupportedSceneModes();
        if (supportedSceneModes != null && supportedSceneModes.contains(Camera.Parameters.SCENE_MODE_ACTION)) {
            if (sceneMode != null && !sceneMode.equals(Camera.Parameters.SCENE_MODE_ACTION)) {
                parameters.setSceneMode(Camera.Parameters.SCENE_MODE_ACTION);
            }
        }
        //闪光模式
        String flashMode = parameters.getFlashMode();
        List<String> supportedFlashModes = parameters.getSupportedFlashModes();
        if (supportedFlashModes != null && supportedFlashModes.contains(Camera.Parameters.FLASH_MODE_AUTO)) {
            if (flashMode != null && !flashMode.equals(Camera.Parameters.FLASH_MODE_AUTO)) {
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
            }
        }
        //对焦模式
        String focusMode = parameters.getFocusMode();
        List<String> supportedFocusModes = parameters.getSupportedFocusModes();
        if (supportedFocusModes != null && supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
            if (focusMode != null && !focusMode.equals(Camera.Parameters.FOCUS_MODE_AUTO)) {
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            }
        }

        List<Camera.Size> supportedPreviewSizes = parameters.getSupportedPreviewSizes();
        //设置预览尺寸，选择SurfaceView的宽高比例，与支持的宽高比例最相近的一组

        previewSize = getOptimalPreviewSize(supportedPreviewSizes, mWidth, mHeight);
        if (previewSize != null)
            parameters.setPreviewSize(previewSize.width, previewSize.height);
        List<Camera.Size> supportedPictureSizes = parameters.getSupportedPictureSizes();
        //设置图片尺寸
        Camera.Size pictureSize = getOptimalPreviewSize(supportedPictureSizes, mWidth, mHeight);
        if (pictureSize != null)
            parameters.setPictureSize(pictureSize.width, pictureSize.height);
        camera.setParameters(parameters);
    }

    Camera.Size previewSize;

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

            displayOrientation = result;
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
            try {
                mCamera.autoFocus(new Camera.AutoFocusCallback() {
                    @Override
                    public void onAutoFocus(boolean success, Camera camera) {
                        Logger.debug("onAutoFocus " + (success ? "success" : "fail"));
                    }
                });
            } catch (Exception e) {
                Logger.error("Exception", e);
            }
        }
    }

    private PreviewCallback previewCallback;

    public void setOneShotPreviewCallback(PreviewCallback callback) {
        if (previewCallback == null) {
            this.previewCallback = callback;
            previewCallback.setPreviewSize(previewSize);
        }
        if (previewCallback != null)
            mCamera.setPreviewCallback(previewCallback);
//            mCamera.setOneShotPreviewCallback(previewCallback);
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
            mCamera.setOneShotPreviewCallback(null);
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
                new SaveTask(data, mActivity.getFilesDir() + "/picture.jpeg", callback).execute();
                /*try {
                    if (displayOrientation == -1) {
                        displayOrientation = 0;
                    }
                    if (isFrontCamera && displayOrientation > 0) {
                        displayOrientation = -displayOrientation;
                    }

                    File file = new File(mActivity.getFilesDir() + "/picture.jpeg");
                    if (!file.exists())
                        file.createNewFile();
                    LubanCompress compress = new LubanCompress();
                    //压缩
                    Bitmap srcBitmap = compress(data);
                    //旋转
                    srcBitmap = compress.rotate(displayOrientation, srcBitmap);
                    if (isFrontCamera) {
                        //前置摄像头拍照，左右对调
                        srcBitmap = compress.reverseLR(srcBitmap);
                    }
                    compress.bitmapToFile(srcBitmap, file);
                    callback.onSuccess(file);
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.onFail();
                }*/
            }
        });
    }

    private class SaveTask extends AsyncTask<Void, Void, Integer> {
        private byte[] data;
        private CaptureCallback captureCallback;
        private File file;
        private String filePath;

        public SaveTask(byte[] data, String filePath, CaptureCallback captureCallback) {
            this.data = data;
            this.captureCallback = captureCallback;
            this.filePath = filePath;
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            try {
                if (displayOrientation == -1) {
                    displayOrientation = 0;
                }
                if (isFrontCamera && displayOrientation > 0) {
                    displayOrientation = -displayOrientation;
                }

                file = new File(filePath);
                if (!file.exists())
                    file.createNewFile();
                LubanCompress compress = new LubanCompress();
                //压缩
                Bitmap srcBitmap = compress(data);
                //旋转
                srcBitmap = compress.rotate(displayOrientation, srcBitmap);
                if (isFrontCamera) {
                    //前置摄像头拍照，左右对调
                    srcBitmap = compress.reverseLR(srcBitmap);
                }
                compress.bitmapToFile(srcBitmap, file);
                return 1;
            } catch (Exception e) {
                e.printStackTrace();
                return -1;
            }
        }

        @Override
        protected void onPostExecute(Integer integer) {
            if (integer != null && integer == 1) {
                captureCallback.onSuccess(file);
            } else {
                captureCallback.onFail();
            }
        }
    }

    public Bitmap compress(byte[] data) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length, opts);
        opts.inJustDecodeBounds = false;

        int w = opts.outWidth;
        int h = opts.outHeight;
        float standardW = 480f;
        float standardH = 800f;

        int zoomRatio = 1;
        if (w > h && w > standardW) {
            zoomRatio = (int) (w / standardW);
        } else if (w < h && h > standardH) {
            zoomRatio = (int) (h / standardH);
        }
        if (zoomRatio <= 0)
            zoomRatio = 1;
        opts.inSampleSize = zoomRatio;
        bmp = BitmapFactory.decodeByteArray(data, 0, data.length, opts);
        return bmp;
    }

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
                continue;
            }
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
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
        void onSuccess(File file);

        void onFail();
    }

}