package com.example.administrator.camerademo;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.jump).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PermissionCompatUtil.checkCameraPermission(MainActivity.this, new PermissionCompatUtil.OnPermissionCallback() {
                    @Override
                    public void onGrantCameraResult(boolean granted) {
                        if (granted) {
                            PermissionCompatUtil.checkWritePermission(MainActivity.this, new PermissionCompatUtil.OnPermissionCallback() {
                                @Override
                                public void onGrantWriteResult(boolean granted) {
                                    if (granted)
                                        startActivityForResult(new Intent(MainActivity.this, Camera0Activity.class), 2);
                                }
                            });
                        } /*else {
                    onBackPressed();
                }*/
                    }
                });
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2 && resultCode == RESULT_OK) {
            String picPath = CameraActivity.getPicPath(data);
            ImageView imageView = (ImageView) findViewById(R.id.imageview);


            Picasso.with(this)
                    .load("file:" + picPath).networkPolicy(NetworkPolicy.NO_CACHE)
                    .memoryPolicy(MemoryPolicy.NO_CACHE)
                    .into(imageView);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //REQUEST_CODE_CAMERA = 999;
        PermissionCompatUtil.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }



    private void test(){
        Intent intent = new Intent();
//        intent.setData();
//        intent.setType()
        PackageManager packageManager = getPackageManager();
//        packageManager.queryIntentActivities();
    }
}
