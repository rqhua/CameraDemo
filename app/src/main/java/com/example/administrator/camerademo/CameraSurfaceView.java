package com.example.administrator.camerademo;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Created by Rqhua on 2018/1/22.
 */
public class CameraSurfaceView extends SurfaceView implements SurfaceHolder.Callback, Camera.ShutterCallback, Camera.PictureCallback {
    private static final int FRONT = 1;
    private static final int BACK = 0;
    private int cameraType = BACK;
    private Context mContext;
    /**
     * 相机Id
     */
    private int mCurrentCameraId = -1;
    private int mNumberOfCameras = 0;
    private Camera mCamera;
    private SurfaceHolder mSurfaceHolder;
    /**
     * 闪光灯配置参数
     */
    private String mFlashMode;

    /**
     * 对焦模式
     */
    private String mFocusMode;

    /**
     * 场景模式配置
     */
    private String mSceneMode;

    /**
     * 预览旋转角度
     */
    private int mDisplayOrientation;

    private int mViewWidth;
    private int mViewHeight;


    private boolean previewing;
    private boolean stoping;

//    private CameraConfigurationManager configManager;

    public CameraSurfaceView(Context context) {
        this(context, null);
    }

    public CameraSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CameraSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
//        mSurfaceHolder = this.getHolder();
//        mSurfaceHolder.addCallback(this);
//        this.configManager = new CameraConfigurationManager(context);
        this.post(new Runnable() {

            @Override

            public void run() {
                mViewWidth = getWidth();
                mViewHeight = getHeight();
                mSurfaceHolder = getHolder();
                mSurfaceHolder.addCallback(CameraSurfaceView.this);
                initCamera();
                startPreviewDisplay();
            }
        });
    }

    private void initCamera(int type) {
        openCamera(type);
        setCameraParameters();
        followScreenOrientation();
    }

    private void initCamera() {
        initCamera(BACK);
    }


    public void switchCamera() {
        int type = BACK;
        if (cameraType == FRONT)
            type = BACK;
        else if (cameraType == BACK)
            type = FRONT;
        stopPreviewDisplay();
        initCamera(type);
        startPreviewDisplay();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Logger.debug("surfaceCreated");
        initCamera(cameraType);
        /*openCamera();
        setCameraParameters();*/
        startPreviewDisplay();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Logger.debug("surfaceChanged");
        if (mSurfaceHolder.getSurface() == null) {
            return;
        }
        followScreenOrientation();
        stopPreviewDisplay();
        startPreviewDisplay();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Logger.debug("surfaceDestroyed");
        stopPreviewDisplay();
        release();
    }

    private void openCamera(int type) {
        try {
            if (!hasCameraDevice()) {
                return;
            }
            cameraType = type;
            stopPreviewDisplay();
            mNumberOfCameras = Camera.getNumberOfCameras();
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            for (int i = 0; i < mNumberOfCameras; i++) {
                Camera.getCameraInfo(i, cameraInfo);
                if (type == BACK && cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    mCurrentCameraId = i;
                } else if (type == FRONT && cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    mCurrentCameraId = i;
                }
            }

            if (mCurrentCameraId == -1) {
                Toast.makeText(getContext(), "没有摄像头", Toast.LENGTH_SHORT).show();
                return;
            }
            Logger.debug("当前相机ID " + mCurrentCameraId);
            mCamera = Camera.open(mCurrentCameraId);
            startPreviewDisplay();
        } catch (Exception e) {
            Logger.debug("打开相机异常", e);
        }
    }

    /**
     * 打开相机
     *
     * @return
     */
    public void openCamera() {
        openCamera(BACK);
    }

    /**
     * 设置相机
     */
    public void setCameraParameters() {
        if (mCamera == null)
            return;
        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setPictureFormat(ImageFormat.JPEG);
        parameters.setPreviewFormat(ImageFormat.NV21);
        //闪光灯配置
        String flashMode = parameters.getFlashMode();
        if (Camera.Parameters.FLASH_MODE_OFF.equals(flashMode)) {
            mFlashMode = Camera.Parameters.FLASH_MODE_AUTO;
            parameters.setFlashMode(mFlashMode);
        }

        //聚焦模式

        String focusMode = parameters.getFocusMode();
        if (isAutoFocusSupported(parameters) && !Camera.Parameters.FLASH_MODE_AUTO.equals(focusMode)) {
            mFocusMode = Camera.Parameters.FLASH_MODE_AUTO;
            parameters.setFocusMode(mFocusMode);
        }


        //场景模式配置
        String sceneMode = parameters.getSceneMode();
        if (!Camera.Parameters.SCENE_MODE_ACTION.equals(sceneMode)) {
            mSceneMode = Camera.Parameters.SCENE_MODE_ACTION;
            parameters.setSceneMode(mSceneMode);
        }

//        configManager.initFromCameraParameters(mCamera);
//        configManager.setDesiredCameraParameters(mCamera, true);
        // 设置图片宽高
//        List<Camera.Size> pictureSizes = parameters.getSupportedPictureSizes();
        List<Camera.Size> sizes = parameters.getSupportedPreviewSizes();
        Camera.Size mPicSize = CamParaUtil.getInstance().getOptimalSize(sizes, mViewWidth, mViewHeight);
//        if (mPicSize != null && mViewHeight != 0 && mViewWidth != 0) {
//            parameters.setPictureSize(mViewWidth, mViewHeight);
////            parameters.setPictureSize(mPicSize.width, mPicSize.height);
//            Logger.debug("图片宽：" + mPicSize.width + " 图片高：" + mPicSize.height);
//        }
        // 设置相机宽高
//        List<Camera.Size> sizes = parameters.getSupportedPreviewSizes();
        Camera.Size optimalSize = CamParaUtil.getInstance().getOptimalSize(sizes, mViewWidth, mViewHeight);
        if (optimalSize != null && mViewHeight != 0 && mViewWidth != 0) {
//            parameters.setPictureSize(optimalSize.width, optimalSize.height);
            parameters.setPreviewSize(optimalSize.width, optimalSize.height);
//            parameters.setPreviewSize(optimalSize.width, optimalSize.height);
            Logger.debug("mViewWidth：" + mViewWidth + " mViewHeight：" + mViewHeight);
            Logger.debug("相机宽：" + optimalSize.width + " 相机高：" + optimalSize.height);
        }
        mCamera.setParameters(parameters);
    }


    /**
     * 相机跟随设备的方向
     */
    public void followScreenOrientation() {
        if (mCamera == null)
            return;
        final int orientation = getContext().getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            //横屏
            mDisplayOrientation = 0;
            mCamera.setDisplayOrientation(mDisplayOrientation);
        } else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            //竖屏
            mDisplayOrientation = 90;
            mCamera.setDisplayOrientation(mDisplayOrientation);
        }
    }

    public void startPreviewDisplay() {
        if (mCamera == null) {
            if (stoping) {
                initCamera(cameraType);
            } else {
                return;
            }
        }
        try {
            mCamera.setPreviewDisplay(mSurfaceHolder);
            if (!previewing) {
                mCamera.startPreview();
                previewing = true;
            }
        } catch (IOException e) {
            Logger.error("Error while START preview for mCamera", e);
        }
    }

    public void stopPreviewDisplay() {
        if (mCamera == null)
            return;
        try {
            if (previewing) {
                mCamera.stopPreview();
                previewing = false;
                stoping = true;
                mCamera.release();
                mCamera = null;
            }
        } catch (Exception e) {
            Logger.error("Error while STOP preview for mCamera", e);
        }
    }

    private PictureTakenCallback callback;

    public void takePhoto(PictureTakenCallback callback) {
        if (mCamera == null)
            return;
        if (!previewing) {
            startPreviewDisplay();
            return;
        }
        this.callback = callback;
        mCamera.takePicture(this, null, this);
    }

    @Override
    public void onShutter() {
        Logger.debug("onShutter");
        previewing = false;
    }

    /**
     * 文件保存文件夹
     */
    public static final String fileDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath() + "/Register/";

    /**
     * 文件名
     */
    public static final String fileName = "my_photo.jpg";

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        Logger.debug("onPictureTaken");
        new SavePictureTask(data).execute();
//        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
//        Matrix matrix = new Matrix();
//        matrix.setRotate(mDisplayOrientation, bitmap.getWidth() / 2, bitmap.getHeight() / 2);
//        Bitmap bmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
//        if (null != bitmap) {
//            bitmap.recycle();
//        }
//
//        if (bmp != null) {
//            savePicture(bmp, fileDir, fileName, Bitmap.CompressFormat.JPEG, 90);
//            if (callback != null) {
//                callback.onPictureTaken(fileDir + fileName);
//            }
//        } else {
//            startPreviewDisplay();
//        }
    }

    private class SavePictureTask extends AsyncTask<Void, Void, Integer> {
        private byte[] data;

        public SavePictureTask(byte[] data) {
            this.data = data;
        }

        @Override
        protected Integer doInBackground(Void... voids) {
//            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            Bitmap bitmap = compress(data);
            Matrix matrix = new Matrix();
            if (cameraType == FRONT) {
                matrix.setRotate(-mDisplayOrientation, bitmap.getWidth() / 2, bitmap.getHeight() / 2);
            } else if (cameraType == BACK) {
                matrix.setRotate(mDisplayOrientation, bitmap.getWidth() / 2, bitmap.getHeight() / 2);
            }

//            matrix.setRotate(mDisplayOrientation, bitmap.getWidth() / 2, bitmap.getHeight() / 2);
            Bitmap bmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

            if (null != bitmap) {
                bitmap.recycle();
            }

            if (bmp != null && cameraType == FRONT) {
                //前置摄像头拍照，左右对称变换，使图片与预览图效果一致
                float[] values = {-1f, 0.0f, 0.0f, 0.0f, 1f, 0.0f, 0.0f, 0.0f, 1.0f};
                matrix.setValues(values);
                Bitmap bmp1 = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
                bmp.recycle();
                if (bmp1 != null) {
                    savePicture(bmp1, fileDir, fileName, Bitmap.CompressFormat.JPEG, 80);
                    return 1;
                }
            }


            if (bmp != null) {
                savePicture(bmp, fileDir, fileName, Bitmap.CompressFormat.JPEG, 80);
                return 1;
            }
            return -1;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            if (integer == 1) {
                if (callback != null) {
                    callback.onPictureTaken(fileDir + fileName);
                }
            } else {
                startPreviewDisplay();
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

    public boolean savePicture(Bitmap bmp, String fileDir, String name, Bitmap.CompressFormat format, int quality) {
        File dir = new File(fileDir);
        if (dir != null) {
            try {

                if (!dir.exists()) {
                    boolean mkdirs = dir.mkdirs();
                    Logger.debug("" + mkdirs);
                }

                FileOutputStream bos = new FileOutputStream(fileDir + name);
                bmp.compress(format, quality, bos);
                bos.flush();
                bos.close();
                Logger.debug("图片保存完成");
                return true;
            } catch (FileNotFoundException e) {
                Logger.error(e.getMessage(), e);
            } catch (IOException e) {
                Logger.error(e.getMessage(), e);
            }
        } else {
            Toast.makeText(mContext, "SDCard Error", Toast.LENGTH_SHORT).show();
        }

        Logger.error("图片保存失败");
        return false;
    }

    public void release() {
        if (mCamera == null)
            return;
        mCamera.release();
    }

    /**
     * 是否有相机设备
     *
     * @return
     */
    public boolean hasCameraDevice() {
        return getContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    /**
     * 是否支持自动对焦
     *
     * @param params
     * @return
     */
    public boolean isAutoFocusSupported(Camera.Parameters params) {
        List<String> modes = params.getSupportedFocusModes();
        return modes.contains(Camera.Parameters.FOCUS_MODE_AUTO);
    }

    //    //将图片添加进手机相册
//    private void galleryAddPic(){
//        Intent mediaScanIntent=new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
//        mediaScanIntent.setData(photoUri);
//        this.sendBroadcast(mediaScanIntent);
//    }
    public interface PictureTakenCallback {
        void onPictureTaken(String picturePath);
    }
}