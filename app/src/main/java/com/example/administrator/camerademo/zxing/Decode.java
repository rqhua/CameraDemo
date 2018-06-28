package com.example.administrator.camerademo.zxing;

import android.graphics.Rect;
import android.hardware.Camera;

import com.example.administrator.camerademo.Logger;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.LuminanceSource;
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

    public static Result decode(Camera.Size previewSize, byte[] yuvData, Rect rect) {
        if (yuvData == null || previewSize == null)
            return null;
        // 这里需要将获取的data翻转一下，因为相机默认拿的的横屏的数据
        byte[] rotatedData = new byte[yuvData.length];
        for (int y = 0; y < previewSize.height; y++) {
            for (int x = 0; x < previewSize.width; x++)
                rotatedData[x * previewSize.height + previewSize.height - y - 1] = yuvData[x + y * previewSize.width];
        }
//
//         宽高也要调整
//        int tmp = previewSize.width;
//        previewSize.width = previewSize.height;
//        previewSize.height = tmp;



        int dataWidth = previewSize.height;
        int dataHeight = previewSize.width;
        int left = rect.left;
        int top = rect.top;
        int width = rect.width();
        int height = rect.height();
        if (left + width > dataWidth || top + height > dataHeight) {
            Logger.debug("22222 back 22222");
            return null;
        }
        Result rawResult = null;
        PlanarYUVLuminanceSource source = buildLuminanceSource(rotatedData, dataWidth, dataHeight, rect);
//        YUVLuminanceSource source = new YUVLuminanceSource(rotatedData, dataWidth, dataHeight, left, top, width, height, false);
        if (source != null) {
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            try {
                rawResult = multiFormatReader.decodeWithState(bitmap);
            } catch (ReaderException re) {
                Logger.debug("", re);
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
    public static PlanarYUVLuminanceSource buildLuminanceSource(byte[] data, int width, int height, Rect rect) {
//        Rect rect = null; //条码区域
//        if (rect == null) {
//            return null;
//        }
        // Go ahead and assume it's YUV rather than die.
//        return new PlanarYUVLuminanceSource(data, width, height, 0, 0, width, height, false);
        //byte[] yuvData, int dataWidth, int dataHeight, int left, int top, int width, int height, boolean reverseHorizontal
        return new PlanarYUVLuminanceSource(data, width, height, rect.left, rect.top, rect.width(), rect.height(), false);
    }

    public interface DecodeCallback {
        void onSuccess(Result result);

        void onFail();
    }


    public static class YUVLuminanceSource extends LuminanceSource {
        private static final int THUMBNAIL_SCALE_FACTOR = 2;
        private final byte[] yuvData;
        private final int dataWidth;
        private final int dataHeight;
        private final int left;
        private final int top;

        public YUVLuminanceSource(byte[] yuvData, int dataWidth, int dataHeight, int left, int top, int width, int height, boolean reverseHorizontal) {
            super(width, height);
            if (left + width <= dataWidth && top + height <= dataHeight) {
                this.yuvData = yuvData;
                this.dataWidth = dataWidth;
                this.dataHeight = dataHeight;
                this.left = left;
                this.top = top;
                if (reverseHorizontal) {
                    this.reverseHorizontal(width, height);
                }

            } else {
                throw new IllegalArgumentException("Crop rectangle does not fit within image data.");
            }
        }

        public byte[] getRow(int y, byte[] row) {
            if (y >= 0 && y < this.getHeight()) {
                int width = this.getWidth();
                if (row == null || row.length < width) {
                    row = new byte[width];
                }

                int offset = (y + this.top) * this.dataWidth + this.left;
                System.arraycopy(this.yuvData, offset, row, 0, width);
                return row;
            } else {
                throw new IllegalArgumentException("Requested row is outside the image: " + y);
            }
        }

        public byte[] getMatrix() {
            int width = this.getWidth();
            int height = this.getHeight();
            if (width == this.dataWidth && height == this.dataHeight) {
                return this.yuvData;
            } else {
                int area = width * height;
                byte[] matrix = new byte[area];
                int inputOffset = this.top * this.dataWidth + this.left;
                if (width == this.dataWidth) {
                    System.arraycopy(this.yuvData, inputOffset, matrix, 0, area);
                    return matrix;
                } else {
                    byte[] yuv = this.yuvData;

                    for (int y = 0; y < height; ++y) {
                        int outputOffset = y * width;
                        System.arraycopy(yuv, inputOffset, matrix, outputOffset, width);
                        inputOffset += this.dataWidth;
                    }

                    return matrix;
                }
            }
        }

        public boolean isCropSupported() {
            return true;
        }

        public LuminanceSource crop(int left, int top, int width, int height) {
            return new PlanarYUVLuminanceSource(this.yuvData, this.dataWidth, this.dataHeight, this.left + left, this.top + top, width, height, false);
        }

        public int[] renderThumbnail() {
            int width = this.getWidth() / 2;
            int height = this.getHeight() / 2;
            int[] pixels = new int[width * height];
            byte[] yuv = this.yuvData;
            int inputOffset = this.top * this.dataWidth + this.left;

            for (int y = 0; y < height; ++y) {
                int outputOffset = y * width;

                for (int x = 0; x < width; ++x) {
                    int grey = yuv[inputOffset + x * 2] & 255;
                    pixels[outputOffset + x] = -16777216 | grey * 65793;
                }

                inputOffset += this.dataWidth * 2;
            }

            return pixels;
        }

        public int getThumbnailWidth() {
            return this.getWidth() / 2;
        }

        public int getThumbnailHeight() {
            return this.getHeight() / 2;
        }

        private void reverseHorizontal(int width, int height) {
            byte[] yuvData = this.yuvData;
            int y = 0;

            for (int rowStart = this.top * this.dataWidth + this.left; y < height; rowStart += this.dataWidth) {
                int middle = rowStart + width / 2;
                int x1 = rowStart;

                for (int x2 = rowStart + width - 1; x1 < middle; --x2) {
                    byte temp = yuvData[x1];
                    yuvData[x1] = yuvData[x2];
                    yuvData[x2] = temp;
                    ++x1;
                }

                ++y;
            }

        }
    }
}
