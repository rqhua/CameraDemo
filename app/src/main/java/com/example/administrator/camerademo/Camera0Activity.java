package com.example.administrator.camerademo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

public class Camera0Activity extends AppCompatActivity {
    private CameraHelper cameraHelper;
    CameraPreview cameraPreview;

    private static final String TAG = "Camera0Activity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera0);
        setViewEnable(R.id.btn_capture, false);
        cameraPreview = new CameraPreview(this);
        findViewById(R.id.btn_capture).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraPreview.capture(new CameraHelper.CaptureCallback() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "onSuccess: ");
                    }

                    @Override
                    public void onFail() {
                        Log.e(TAG, "onFail: ");
                    }
                });
            }
        });
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(cameraPreview);
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraPreview.onResume();
    }

    private void setViewEnable(int viewId, boolean enable) {
        findViewById(viewId).setEnabled(enable);
    }

    private Camera0Activity getThis() {
        return this;
    }

    @Override
    protected void onPause() {
        super.onPause();
        cameraPreview.onPause();
    }

}
