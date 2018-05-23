package com.example.administrator.camerademo;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class CameraActivity extends AppCompatActivity {
    private CameraSurfaceView mCameraSurfaceView;
    private static final String PATH_RESULT = "path_result";

    public static String getPicPath(Intent intent) {
        if (intent == null)
            return null;
        return intent.getStringExtra(PATH_RESULT);
    }

    private void setPicPathResult(String picPath) {
        Intent intent = new Intent();
        intent.putExtra(PATH_RESULT, picPath);
        setResult(RESULT_OK, intent);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        setUpView();
    }


    protected void setUpView() {
        mCameraSurfaceView = findViewById(R.id.surfaceview);
        findViewById(R.id.take_photo).setOnClickListener(new View.OnClickListener() {
            boolean front = false;

            @Override
            public void onClick(View v) {
                mCameraSurfaceView.takePhoto(new CameraSurfaceView.PictureTakenCallback() {
                    @Override
                    public void onPictureTaken(String picturePath) {
                        Logger.debug("picturePath " + picturePath);
                        setPicPathResult(picturePath);
                        onBackPressed();
                    }
                });
            }
        });
    }


    @Override
    protected void onStop() {
        super.onStop();
        if (mCameraSurfaceView != null)
            mCameraSurfaceView.stopPreviewDisplay();
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCameraSurfaceView != null)
            mCameraSurfaceView.release();
    }
}