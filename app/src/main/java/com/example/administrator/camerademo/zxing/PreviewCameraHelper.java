package com.example.administrator.camerademo.zxing;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.View;

import com.example.administrator.camerademo.Logger;
import com.example.administrator.camerademo.define.LubanCompress;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2018/5/23.
 */

public class PreviewCameraHelper implements SensorEventListener, Camera.AutoFocusCallback {
    private static final String TAG = "CameraHelper";
    private Activity mActivity;
    private Camera mCamera = null;
    private int displayOrientation = -1;
    private int mCurrentId = -1;
    private int mWidth;
    private int mHeight;
    private SensorManager mSensorManager;
    private Sensor mSensor;

    private boolean isFrontCamera = false;

    private boolean previewing = false;
    private boolean focusing = false;

    private View scanCropView = null;
    private Rect mCropRect = null;

    public PreviewCameraHelper(Activity activity) {
        mActivity = activity;
        mSensorManager = (SensorManager) mActivity.getSystemService(Activity.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    /**
     * A safe way to get an instance of the Camera object.
     */
    public void initCamera(int width, int height, Rect mCropRect, final OpenCallback callback) {
        if (callback == null) {
            throw new NullPointerException("callback Can not be Null");
        }
        mWidth = width;
        mHeight = height;
        this.mCropRect = mCropRect;
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
                mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_FASTEST);
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
        int focusAreas = parameters.getMaxNumFocusAreas();
        Logger.debug("222 focusAreas " + focusAreas);
        camera.setParameters(parameters);
    }


    /**
     * 设置对焦区域
     */
    private Rect getFocusAreas(Rect rect) {
        if (rect == null)
            return null;
        Logger.debug("222 source " + rect.left + " " + rect.top + " " + rect.right + " " + rect.bottom + " ");

        if (previewSize == null)
            return null;

        int widthR = 2000 / previewSize.width;
        int heightR = 2000 / previewSize.height;
        //坐标系平移
        Rect resultRect = new Rect(
                rect.left * widthR - 1000,
                rect.top * heightR - 1000,
                rect.right * widthR - 1000,
                rect.bottom * heightR - 1000);
//        Rect resultRect = new Rect(rect.left - 1000, rect.top - 1000, rect.right - 1000, rect.bottom - 1000);
        Logger.debug("222 source " + resultRect.left + " " + resultRect.top + " " + resultRect.right + " " + resultRect.bottom + " ");
        return resultRect;
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
        }
    }

    private int mX;
    private int mY;
    private int mZ;
    private long lastFocusMillis = 0;

    //传感器回调
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor == null) {
            return;
        }

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            int x = (int) event.values[0];
            int y = (int) event.values[1];
            int z = (int) event.values[2];

            int px = Math.abs(mX - x);
            int py = Math.abs(mY - y);
            int pz = Math.abs(mZ - z);

            double value = Math.sqrt(px * px + py * py + pz * pz);
            if (value > 1.4) {
                lastFocusMillis = System.currentTimeMillis();
            } else {

                long currentMillis = System.currentTimeMillis();
                if (currentMillis - lastFocusMillis > 800) {
                    lastFocusMillis = currentMillis;

                    focus();
                }
            }
            mX = x;
            mY = y;
            mZ = z;
        }
    }

    //传感器回调
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void focus() {
        if (mCamera != null && previewing) {
            Logger.debug("聚焦");
            /*Camera.Parameters parameters = mCamera.getParameters();
            List<Camera.Area> areas = new ArrayList<>();
            Rect focusAreas1 = getFocusAreas(mCropRect);
            if (focusAreas1 != null) {
                areas.add(new Camera.Area(focusAreas1, 1000));
                parameters.setFocusAreas(areas);
            }
            mCamera.setParameters(parameters);*/
            mCamera.autoFocus(this);
        }
    }

    //自动对焦回调
    @Override
    public void onAutoFocus(boolean success, Camera camera) {

        if (mCamera != null) {
            mCamera.cancelAutoFocus();
        }
        Logger.debug("==========onAutoFocus " + (success ? "success" : "fail"));
    }

    private PreviewCallback previewCallback;

    public void requestPreviewFrame(PreviewCallback callback) {
        if (previewCallback == null) {
            this.previewCallback = callback;
            previewCallback.setPreviewSize(previewSize);
            previewCallback.setScopRect(mCropRect);
        }
        if (previewCallback != null)
            mCamera.setPreviewCallback(previewCallback);
        startPreview();
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
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            previewing = false;
        }
    }

    //停止预览并释放相机
    public void stopPreviewAndFreeCamera() {
        try {
            if (mCamera != null) {
                mSensorManager.unregisterListener(this, mSensor);
                stopPreView();
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