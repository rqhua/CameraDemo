package com.example.administrator.camerademo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

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
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_capture:
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
