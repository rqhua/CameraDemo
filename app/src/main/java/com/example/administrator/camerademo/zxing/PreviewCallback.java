/*
 * Copyright (C) 2010 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.administrator.camerademo.zxing;

import android.graphics.Point;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.example.administrator.camerademo.Logger;
import com.google.zxing.Result;

public class PreviewCallback implements Camera.PreviewCallback {
    private Camera.Size previewSize;
    private Decode.DecodeCallback callback;
    private static byte[] data;

    public PreviewCallback(Decode.DecodeCallback callback) {
        this.callback = callback;
    }

    public Camera.Size getPreviewSize() {
        return previewSize;
    }

    public void setPreviewSize(Camera.Size previewSize) {
        this.previewSize = previewSize;
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        Logger.debug("onPreviewFrame");
        if (previewSize != null) {
            Result rawResult = Decode.getInstance().decode(getPreviewSize(), data);
            if (rawResult != null) {
                Logger.debug("Decode Success");
                if (callback != null)
                    callback.onSuccess(rawResult);
            } else {
                Logger.debug("Decode Fail");
                if (callback != null)
                    callback.onFail();
            }
//            this.data = data;
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            new DecodeTask().execute();
        } else {
            Logger.debug("onPreviewFrame previewSize == null");
        }
    }

    private class DecodeTask extends AsyncTask<Void, Void, Result> {

        @Override
        protected Result doInBackground(Void... voids) {
            return Decode.getInstance().decode(getPreviewSize(), data);
        }

        @Override
        protected void onPostExecute(Result rawResult) {
            if (rawResult != null) {
                Logger.debug("Decode Success");
                if (callback != null)
                    callback.onSuccess(rawResult);
            } else {
                Logger.debug("Decode Fail");
                if (callback != null)
                    callback.onFail();
            }
        }
    }

}
