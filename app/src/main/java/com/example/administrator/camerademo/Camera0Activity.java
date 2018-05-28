package com.example.administrator.camerademo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.io.File;

import top.zibin.luban.CompressionPredicate;
import top.zibin.luban.Luban;
import top.zibin.luban.OnCompressListener;

public class Camera0Activity extends AppCompatActivity implements View.OnClickListener {
    private CameraHelper cameraHelper;
    CameraPreview cameraPreview;

    private static final String TAG = "Camera0Activity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera0);
//        setViewEnable(R.id.btn_capture, false);
//        setViewVisible(R.id.view_empty, View.GONE);
        cameraPreview = new CameraPreview(this);
        findViewById(R.id.btn_capture).setOnClickListener(this);
        findViewById(R.id.btn_switch).setOnClickListener(this);
        findViewById(R.id.btn_smallview).setOnClickListener(this);
        findViewById(R.id.btn_largeview).setOnClickListener(this);
        findViewById(R.id.image).setOnClickListener(this);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(cameraPreview);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onClick(final View v) {
        int visibility = findViewById(R.id.image).getVisibility();
        if (visibility == View.VISIBLE) {
            setViewVisible(R.id.image, View.GONE);
        }
        switch (v.getId()) {
            case R.id.btn_capture:
                cameraPreview.capture(new CameraHelper.CaptureCallback() {
                    @Override
                    public void onSuccess(File file, byte[] data) {
                        Luban.with(Camera0Activity.this)
                                .load(file)
                                .ignoreBy(100)
                                .setTargetDir(getFilesDir() + "/picture_compress.jpeg")
                                .filter(new CompressionPredicate() {
                                    @Override
                                    public boolean apply(String path) {
                                        return !(TextUtils.isEmpty(path) || path.toLowerCase().endsWith(".jpeg"));
                                    }
                                })
                                .setCompressListener(new OnCompressListener() {
                                    @Override
                                    public void onStart() {
                                        // TODO 压缩开始前调用，可以在方法内启动 loading UI
                                    }

                                    @Override
                                    public void onSuccess(File file) {
                                        // TODO 压缩成功后调用，返回压缩后的图片文件
                                        ImageView viewById = (ImageView) findViewById(R.id.image);
                                        viewById.setVisibility(View.VISIBLE);
                                        viewById.setImageBitmap(BitmapFactory.decodeFile(file.getAbsolutePath()));
                                    }

                                    @Override
                                    public void onError(Throwable e) {
                                        // TODO 当压缩过程出现问题时调用
                                    }
                                }).launch();
                    }

                    @Override
                    public void onSuccess(Bitmap bitmap, byte[] data) {
                        Log.d(TAG, "onSuccess: ");
                        ImageView viewById = (ImageView) findViewById(R.id.image);
                        viewById.setVisibility(View.VISIBLE);
                        viewById.setImageBitmap(bitmap);
                    }

                    @Override
                    public void onFail() {
                        Log.e(TAG, "onFail: ");
                    }
                });
                break;
            case R.id.btn_switch:
                cameraPreview.switchCamer();
                break;
            case R.id.btn_smallview:
//                setViewVisible(R.id.view_empty, View.VISIBLE);
                break;
            case R.id.btn_largeview:
//                setViewVisible(R.id.view_empty, View.GONE);
                break;
        }
    }

    private void setViewEnable(int viewId, boolean enable) {
        findViewById(viewId).setEnabled(enable);
    }

    private void setViewVisible(int viewId, int visible) {
        findViewById(viewId).setVisibility(visible);
    }

    private Camera0Activity getThis() {
        return this;
    }
}
