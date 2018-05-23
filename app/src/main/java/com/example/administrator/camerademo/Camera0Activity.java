package com.example.administrator.camerademo;

import android.hardware.Camera;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.Toast;

public class Camera0Activity extends AppCompatActivity {
    private CameraHelper cameraHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera0);
        setViewEnable(R.id.btn_capture, false);
        cameraHelper = new CameraHelper();
        cameraHelper.open(new CameraHelper.OpenCallback() {
            @Override
            public void onSuccess(Camera camera) {
                CameraPreview cameraPreview = new CameraPreview(getThis(), camera);
                FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
                preview.addView(cameraPreview);
                setViewEnable(R.id.btn_capture, true);
            }

            @Override
            public void onFail() {
                Toast.makeText(getThis(), "打开相机失败", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setViewEnable(int viewId, boolean enable) {
        findViewById(viewId).setEnabled(enable);
    }

    private Camera0Activity getThis() {
        return this;
    }
}
