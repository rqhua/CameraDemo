package com.example.administrator.camerademo;

import android.hardware.Camera.Size;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CamParaUtil {

    private static CamParaUtil myCamPara = null;
    private static final int FLAG_MIN_WIDTH = 640;

    private CamParaUtil() {
    }

    public static CamParaUtil getInstance() {
        if (myCamPara == null) {
            myCamPara = new CamParaUtil();
            return myCamPara;
        } else {
            return myCamPara;
        }
    }

    /**
     * @param list                 相机支持预览的值
     * @param screenWidthAndHeight 屏幕的 宽values[0] 高values[1]
     * @return
     */
    public Size getPropPreviewSize(List<Size> list, int[] screenWidthAndHeight) {

        if (list == null || list.size() < 0) {
            return null;
        }

        Collections.sort(list, new CameraSizeComparator());

        // 屏幕是竖直的调换屏幕的宽高 和相机（list）匹配
        int width = screenWidthAndHeight[1];
        int height = screenWidthAndHeight[0];

        // 先找出宽高相同的尺寸
        for (int i = 0; i < list.size(); i++) {
            Size size = list.get(i);
            if (size == null) {
                continue;
            }
            if (size.width < FLAG_MIN_WIDTH) {
                break;
            }
            if (width == size.width && height == size.height) {
                return size;
            }
        }


        // 找比例最接近的
        int index = 0;
        float screenRate = ((float) width) / height;
        Size size0 = list.get(0);
        float rate = equalRate(size0, screenRate);
        for (int i = 1; i < list.size(); i++) {
            Size size = list.get(i);
            if (size == null) {
                continue;
            }
            if (size.width < FLAG_MIN_WIDTH) {
                break;
            }
            float equalRate = equalRate(size, screenRate);
            if (equalRate < rate) {
                rate = equalRate;
                index = i;
            }
        }

        return list.get(index);
    }

    public Size getPropPictureSize(List<Size> list, int[] screenWidthAndHeight) {

        if (list == null) {
            return null;
        }

        Collections.sort(list, new CameraSizeComparator());

        // 屏幕是竖直的调换屏幕的宽高 和相机（list）匹配
        int width = screenWidthAndHeight[1];
        int height = screenWidthAndHeight[0];

        // 先找出宽高相同的尺寸
        for (int i = 0; i < list.size(); i++) {
            Size size = list.get(i);
            if (size == null) {
                continue;
            }
            if (size.width < FLAG_MIN_WIDTH) {
                break;
            }
            if (width == size.width && height == size.height) {
                return size;
            }
        }


        // 找比例最接近的
        int index = 0;
        float screenRate = ((float) width) / height;
        Size size0 = list.get(0);
        float rate = equalRate(size0, screenRate);
        for (int i = 1; i < list.size(); i++) {
            Size size = list.get(i);
            if (size == null) {
                continue;
            }
            if (size.width < FLAG_MIN_WIDTH) {
                break;
            }
            float equalRate = equalRate(size, screenRate);
            if (equalRate < rate) {
                rate = equalRate;
                index = i;
            }
        }

        return list.get(index);
    }

    public float equalRate(Size s, float rate) {
        if (s == null) {
            return Integer.MAX_VALUE;
        }
        float r = (float) (s.width) / (float) (s.height);
        return Math.abs(r - rate);
    }

    // 按照width降序排列
    public class CameraSizeComparator implements Comparator<Size> {
        public int compare(Size lhs, Size rhs) {
            if (lhs.width == rhs.width) {
                return 0;
            } else if (lhs.width < rhs.width) {
                return 1;
            } else {
                return -1;
            }
        }
    }

    public Size getOptimalSize(List<Size> sizes, int width, int height) {
        // Use a very small tolerance because we want an exact match.
        final double ASPECT_TOLERANCE = 0.1;
        if (sizes == null) {
            return null;
        }
        Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;
        int targetHeight = width;
        double targetRatio = 1.0f * height / width;
        for (Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) {
                continue;
            }
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        // Cannot find the one match the aspect ratio. This should not happen.
        // Ignore the requirement.
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Size size : sizes) {
                if (Math.abs(size.height - targetHeight) <= minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    public Size getOptimalPicSize(List<Size> sizes, int width, int height) {
        final double ASPECT_TOLERANCE = 0.3;
        if (sizes == null) {
            return null;
        }
        Size optimalSize = null;
        int targetHeight = width;
        int targetWidth = height;
        double minDiff = Double.MAX_VALUE;
        double targetRatio = 1.0f * height / width;
        for (Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) {
                continue;
            }
            if (size.height > targetHeight && size.width > targetWidth
                    && Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        // Cannot find the one match the aspect ratio. This should not happen.
        // Ignore the requirement.
        if (optimalSize == null) {
            optimalSize = getOptimalSize(sizes, width, height);
        }
        return optimalSize;
    }
}