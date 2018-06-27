package com.example.administrator.camerademo.zxing;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.FrameLayout;

import com.example.administrator.camerademo.Logger;
import com.example.administrator.camerademo.R;
import com.google.zxing.Result;

public class ScanActivity extends AppCompatActivity {
    PreviewSurface previewSurface;
    private PreviewCallback previewCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        previewSurface = new PreviewSurface(this);
        final FrameLayout preview = (FrameLayout) findViewById(R.id.preview);
        preview.addView(previewSurface);
        previewCallback = new PreviewCallback(new Decode.DecodeCallback() {
            @Override
            public void onSuccess(Result result) {
                Logger.debug("=========result========= " + result);
                previewSurface.stop();
            }

            @Override
            public void onFail() {
                Logger.debug("onFail");
//                previewSurface.setPreviewCallback(previewCallback);
            }
        });
        previewSurface.setPreviewCallback(previewCallback);
    }

}
