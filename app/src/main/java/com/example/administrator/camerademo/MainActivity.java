package com.example.administrator.camerademo;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.example.administrator.camerademo.define.Camera0Activity;
import com.example.administrator.camerademo.system.CallSystemCameraActivity;

public class MainActivity extends AppCompatActivity {
    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
            PermissionCompatUtil.checkCameraPermission(MainActivity.this, new PermissionCompatUtil.OnPermissionCallback() {
                @Override
                public void onGrantCameraResult(boolean granted) {
                    if (granted) {
                        PermissionCompatUtil.checkWritePermission(MainActivity.this, new PermissionCompatUtil.OnPermissionCallback() {
                            @Override
                            public void onGrantWriteResult(boolean granted) {
                                if (granted) {
                                    switch (v.getId()) {
                                        case R.id.btn_define_capture:
                                            startActivityForResult(new Intent(MainActivity.this, Camera0Activity.class), 2);
                                            break;
                                        case R.id.btn_system_capture:
                                            startActivity(new Intent(MainActivity.this, CallSystemCameraActivity.class));
                                            break;
                                    }
                                }
                            }
                        });
                    } /*else {
                    onBackPressed();
                }*/
                }
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn_define_capture).setOnClickListener(onClickListener);
        findViewById(R.id.btn_system_capture).setOnClickListener(onClickListener);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //REQUEST_CODE_CAMERA = 999;
        PermissionCompatUtil.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }

}
