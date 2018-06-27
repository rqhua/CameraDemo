package com.example.administrator.camerademo.zxing;

import android.graphics.Rect;
import android.hardware.Camera;

import com.example.administrator.camerademo.Logger;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;

/**
 * Created by Administrator on 2018/6/27.
 */
//解码工具类
public class Decode {
    private static MultiFormatReader multiFormatReader;
    private final Map<DecodeHintType, Object> hints;
    //条码
    public static final int BARCODE_MODE = 0X100;
    //二维码
    public static final int QRCODE_MODE = 0X200;
    //二维码 + 条码
    public static final int ALL_MODE = 0X300;

    public static class InstanceHelper {
        public static Decode decode = new Decode();
    }

    public static Decode getInstance() {
        return InstanceHelper.decode;
    }

    private Decode(/*int decodeMode*/) {
        multiFormatReader = new MultiFormatReader();
        hints = new EnumMap<DecodeHintType, Object>(DecodeHintType.class);

        Collection<BarcodeFormat> decodeFormats = new ArrayList<BarcodeFormat>();
        decodeFormats.addAll(EnumSet.of(BarcodeFormat.AZTEC));
        decodeFormats.addAll(EnumSet.of(BarcodeFormat.PDF_417));

        /*switch (decodeMode) {
            case BARCODE_MODE:
                decodeFormats.addAll(DecodeFormatManager.getBarCodeFormats());
                break;

            case QRCODE_MODE:
                decodeFormats.addAll(DecodeFormatManager.getQrCodeFormats());
                break;

            case ALL_MODE:
                decodeFormats.addAll(DecodeFormatManager.getBarCodeFormats());
                decodeFormats.addAll(DecodeFormatManager.getQrCodeFormats());
                break;

            default:
                break;
        }*/
        decodeFormats.addAll(DecodeFormatManager.getBarCodeFormats());
        decodeFormats.addAll(DecodeFormatManager.getQrCodeFormats());

        hints.put(DecodeHintType.POSSIBLE_FORMATS, decodeFormats);
        multiFormatReader.setHints(hints);
    }

    public static Result decode(Camera.Size previewSize, byte[] data) {
        if (data == null || previewSize == null)
            return null;
        // 这里需要将获取的data翻转一下，因为相机默认拿的的横屏的数据
        byte[] rotatedData = new byte[data.length];
        for (int y = 0; y < previewSize.height; y++) {
            for (int x = 0; x < previewSize.width; x++)
                rotatedData[x * previewSize.height + previewSize.height - y - 1] = data[x + y * previewSize.width];
        }

        // 宽高也要调整
        int tmp = previewSize.width;
        previewSize.width = previewSize.height;
        previewSize.height = tmp;

        Result rawResult = null;
        PlanarYUVLuminanceSource source = buildLuminanceSource(rotatedData, previewSize.width, previewSize.height);
        if (source != null) {
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            try {
                rawResult = multiFormatReader.decodeWithState(bitmap);
            } catch (ReaderException re) {
                // continue
            } finally {
                multiFormatReader.reset();
            }
        }
        return rawResult;
    }

    /**
     * A factory method to build the appropriate LuminanceSource object based on
     * the format of the preview buffers, as described by Camera.Parameters.
     *
     * @param data   A preview frame.
     * @param width  The width of the image.
     * @param height The height of the image.
     * @return A PlanarYUVLuminanceSource instance.
     */
    public static PlanarYUVLuminanceSource buildLuminanceSource(byte[] data, int width, int height) {
//        Rect rect = null; //条码区域
//        if (rect == null) {
//            return null;
//        }
        // Go ahead and assume it's YUV rather than die.
        return new PlanarYUVLuminanceSource(data, width, height, 0, 0, width, height, false);
//        return new PlanarYUVLuminanceSource(data, width, height, rect.left, rect.top, rect.width(), rect.height(), false);
    }

    public interface DecodeCallback {
        void onSuccess(Result result);

        void onFail();
    }
}
