package com.example.administrator.camerademo;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.example.administrator.camerademo.compress.Compress;
import com.rqhua.demo.fileprovider.FileProvider7;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import top.zibin.luban.Luban;
import top.zibin.luban.OnCompressListener;

public class CallSystemCameraActivity extends AppCompatActivity {
    ImageView mImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_system_camera);
        mImageView = (ImageView) findViewById(R.id.imageview);
        findViewById(R.id.btn_take_photo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });
    }

    private String mCurrentPhotoPath;
    static final int REQUEST_IMAGE_CAPTURE = 1;

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        String filename = System.currentTimeMillis() + ".png";
        File file = new File(getFilesDir(), filename);
        mCurrentPhotoPath = file.getAbsolutePath();
        //7.0以后对应用间共享文件，需要通过FileProvider操作，使用系统相机从新定义图片保存路径属于应用间共享文件，适配如下
        //4.4以下版本使用Provider需要授权
        //Uri授权
        //Uri fileUri = FileProvider.getUriForFile(this, getPackageName() + ".provider", file);
        // List<ResolveInfo> resInfoList = getPackageManager()
        //.queryIntentActivities(takePictureIntent, PackageManager.MATCH_DEFAULT_ONLY);
        //for (ResolveInfo resolveInfo : resInfoList) {
        //String packageName = resolveInfo.activityInfo.packageName;
        //grantUriPermission(packageName, fileUri, Intent.FLAG_GRANT_READ_URI_PERMISSION
        //| Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        //}

        //取消授权
        //revokeUriPermission(Uri uri, int modeFlags);
        //授权的另一种操作：
        // intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        //判断版本，适配 >7.0
        //Uri fileUri = null;
        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        //fileUri = FileProvider.getUriForFile(this, getPackageName() + ".provider", file);
        //} else {
        //fileUri = Uri.fromFile(file);
        //}
        //封装适配
        Uri fileUri = FileProvider7.getUriForFile(this, file);
        //保存路径
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
                //缩略图
//                Bundle extras = data.getExtras();
//                Bitmap imageBitmap = (Bitmap) extras.get("data");
//                mImageView.setImageBitmap(imageBitmap);
                mImageView.setImageBitmap(BitmapFactory.decodeFile(mCurrentPhotoPath));
            }
        } catch (Exception e) {
            Logger.error("", e);
        }
    }


}
