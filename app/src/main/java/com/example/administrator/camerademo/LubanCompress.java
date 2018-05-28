package com.example.administrator.camerademo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Administrator on 2018/5/28.
 */

public class LubanCompress {

    public void datasToFile(byte[] srcData, File targetFile) {

        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = false;
            options.inSampleSize = 1;
            Bitmap tagBitmap = BitmapFactory.decodeByteArray(srcData, 0, srcData.length, options);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            tagBitmap.compress(Bitmap.CompressFormat.JPEG, 60, stream);

            FileOutputStream fos = new FileOutputStream(targetFile);
            fos.write(stream.toByteArray());
            fos.flush();
            fos.close();
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param srcData    源压缩数据
     * @param targetFile 压缩完成后保存文件
     * @return
     * @throws IOException
     */
    public File compress(byte[] srcData, int DisplayOrientation, File targetFile) {
        try {
            int sampleSize = computeSize(srcData);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = false;
            options.inSampleSize = sampleSize;

            Bitmap tagBitmap = BitmapFactory.decodeByteArray(srcData, 0, srcData.length, options);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            tagBitmap.compress(Bitmap.CompressFormat.JPEG, 60, stream);

            FileOutputStream fos = new FileOutputStream(targetFile);
            fos.write(stream.toByteArray());
            fos.flush();
            fos.close();
            stream.close();
            return targetFile;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Bitmap compress(byte[] srcData) {
        try {
            int sampleSize = computeSize(srcData);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = false;
            options.inSampleSize = sampleSize;

            Bitmap tagBitmap = BitmapFactory.decodeByteArray(srcData, 0, srcData.length, options);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            tagBitmap.compress(Bitmap.CompressFormat.JPEG, 60, stream);
            Bitmap bitmap = BitmapFactory.decodeStream(new ByteArrayInputStream(stream.toByteArray()));
            stream.flush();
            stream.close();
            tagBitmap.recycle();
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    //旋转
    public Bitmap rotate(int displayOrientation, Bitmap bitmap) {
        Matrix matrix = new Matrix();
        matrix.setRotate(displayOrientation, bitmap.getWidth() / 2, bitmap.getHeight() / 2);
        Bitmap bmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        bitmap.recycle();
        return bmp;
    }

    //左右对调
    public Bitmap reverseLR(Bitmap bitmap) {
        Matrix matrix = new Matrix();
        //前置摄像头拍照，左右对称变换，使图片与预览图效果一致
        float[] values = {-1f, 0.0f, 0.0f, 0.0f, 1f, 0.0f, 0.0f, 0.0f, 1.0f};
        matrix.setValues(values);
        Bitmap bmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        bitmap.recycle();
        return bmp;
    }


    private int computeSize(byte[] srcData) {
        int srcWidth = 0;
        int srcHeight = 0;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        options.inSampleSize = 1;
        BitmapFactory.decodeByteArray(srcData, 0, srcData.length, options);
        srcWidth = options.outWidth;
        srcHeight = options.outHeight;
        options.inJustDecodeBounds = false;
        srcWidth = srcWidth % 2 == 1 ? srcWidth + 1 : srcWidth;
        srcHeight = srcHeight % 2 == 1 ? srcHeight + 1 : srcHeight;

        int longSide = Math.max(srcWidth, srcHeight);
        int shortSide = Math.min(srcWidth, srcHeight);

        float scale = ((float) shortSide / longSide);
        if (scale <= 1 && scale > 0.5625) {
            if (longSide < 1664) {
                return 1;
            } else if (longSide < 4990) {
                return 2;
            } else if (longSide > 4990 && longSide < 10240) {
                return 4;
            } else {
                return longSide / 1280 == 0 ? 1 : longSide / 1280;
            }
        } else if (scale <= 0.5625 && scale > 0.5) {
            return longSide / 1280 == 0 ? 1 : longSide / 1280;
        } else {
            return (int) Math.ceil(longSide / (1280.0 / scale));
        }
    }
}
