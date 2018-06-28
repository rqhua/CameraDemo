package com.example.administrator.camerademo.zxing;

import android.app.Activity;
import android.graphics.Rect;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.example.administrator.camerademo.Logger;
import com.example.administrator.camerademo.R;
import com.google.zxing.Result;

public class ScanActivity extends AppCompatActivity implements View.OnClickListener {
    PreviewSurface previewSurface;
    private PreviewCallback previewCallback;
    private SensorManager mSensorManager;
    private Sensor mSensor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        previewSurface = new PreviewSurface(this, findViewById(R.id.capture_crop_view));
        final FrameLayout preview = (FrameLayout) findViewById(R.id.preview);
        preview.addView(previewSurface);
        previewCallback = new PreviewCallback(new Decode.DecodeCallback() {
            @Override
            public void onSuccess(Result result) {
                Logger.debug("=========result========= " + result);
                try {
                    TextView textView = (TextView) findViewById(R.id.result);
                    textView.setText(result.getText() + "");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                previewSurface.stop();
            }

            @Override
            public void onFail() {
                Logger.debug("onFail");
            }
        });
        previewSurface.setPreviewCallback(previewCallback);
        findViewById(R.id.btn_rescan).setOnClickListener(this);
        initSensorManager();
    }

    private void initSensorManager() {
        mSensorManager = (SensorManager) getSystemService(Activity.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_rescan:
                previewSurface.requestPreviewFrame(previewCallback);
                break;
        }
    }
}