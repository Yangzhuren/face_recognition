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

package com.mzcloud.djt.module_face.camera.decoding;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.arcsoft.ageestimation.ASAE_FSDKFace;
import com.arcsoft.facedetection.AFD_FSDKEngine;
import com.arcsoft.facedetection.AFD_FSDKError;
import com.arcsoft.facedetection.AFD_FSDKFace;
import com.arcsoft.facedetection.AFD_FSDKVersion;
import com.arcsoft.facerecognition.AFR_FSDKEngine;
import com.arcsoft.facerecognition.AFR_FSDKError;
import com.arcsoft.facerecognition.AFR_FSDKFace;
import com.arcsoft.facerecognition.AFR_FSDKMatching;
import com.arcsoft.facerecognition.AFR_FSDKVersion;
import com.arcsoft.facetracking.AFT_FSDKEngine;
import com.arcsoft.facetracking.AFT_FSDKError;
import com.arcsoft.facetracking.AFT_FSDKFace;
import com.arcsoft.genderestimation.ASGE_FSDKFace;
import com.mzcloud.djt.module_face.R;
import com.mzcloud.djt.module_face.SampleApplication;
import com.mzcloud.djt.module_face.camera.CameraManager;
import com.mzcloud.djt.module_face.camera.FaceDB;
import com.mzcloud.djt.module_face.camera.PlanarYUVLuminanceSource;
import com.mzcloud.djt.module_face.camera.activity.CaptureActivity;
import com.mzcloud.djt.module_face.camera.activity.CaptureFragment;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

final class DecodeHandler extends Handler {

    private static final String TAG = DecodeHandler.class.getSimpleName();

    private final CaptureFragment fragment;

    private int order;
    private String name;

    DecodeHandler(CaptureFragment fragment) {
        this.fragment = fragment;
        CaptureActivity activity = (CaptureActivity) fragment.getActivity();
        order = activity.getOrder();
        name = activity.getName();
    }

    @Override
    public void handleMessage(Message message) {
        if (message.what == R.id.decode) {
            decode((byte[]) message.obj, message.arg1, message.arg2);
            Log.w(TAG, "............decoding............");
        } else if (message.what == R.id.skip) {
            Message mes = Message.obtain(fragment.getHandler(), R.id.decode_failed);
            mes.sendToTarget();
        } else if (message.what == R.id.quit) {
            Looper.myLooper().quit();
        }
    }

    /**
     * Decode the data within the viewfinder rectangle, and time how long it took. For efficiency,
     * reuse the same reader objects from one decode to the next.
     *
     * @param data   The YUV preview frame.
     * @param width  The width of the preview frame.
     * @param height The height of the preview frame.
     */
    private void decode(byte[] data, int width, int height) {
        if (order == CaptureActivity.REGISTER) {
            register(data, width, height);
        } else if (order == CaptureActivity.DETECTER) {
            detector(data, width, height);
        } else {
            throw new RuntimeException("order is not defined!");
        }

    }

    private AFR_FSDKFace register(byte[] data, int width, int height) {
        AFD_FSDKEngine engine = new AFD_FSDKEngine();
        AFD_FSDKVersion version = new AFD_FSDKVersion();
        List<AFD_FSDKFace> result = new ArrayList<>();
        AFD_FSDKError error = engine.AFD_FSDK_InitialFaceEngine(FaceDB.APP_ID, FaceDB.FACE_DETECTION, AFD_FSDKEngine.AFD_OPF_0_HIGHER_EXT, 16, 5);
        Log.d(TAG, "AFD_FSDK_InitialFaceEngine = " + error.getCode());
        if (error.getCode() != AFD_FSDKError.MOK) {
            Message message = Message.obtain(fragment.getHandler(), R.id.decode_failed);
            message.sendToTarget();
        }
        error = engine.AFD_FSDK_GetVersion(version);
        Log.d(TAG, "AFD_FSDK_GetVersion =" + version.toString() + ", " + error.getCode());
        error = engine.AFD_FSDK_StillImageFaceDetection(data, width, height, AFD_FSDKEngine.CP_PAF_NV21, result);
        Log.d(TAG, "AFD_FSDK_StillImageFaceDetection =" + error.getCode() + "<" + result.size());
        if (!result.isEmpty()) {
            AFR_FSDKVersion version1 = new AFR_FSDKVersion();
            AFR_FSDKEngine engine1 = new AFR_FSDKEngine();
            AFR_FSDKFace result1 = new AFR_FSDKFace();
            AFR_FSDKError error1 = engine1.AFR_FSDK_InitialEngine(FaceDB.APP_ID, FaceDB.FACE_RECOGNITION);
            Log.d(TAG, "AFR_FSDK_InitialEngine = " + error1.getCode());
            if (error1.getCode() != AFD_FSDKError.MOK) {
                Message message = Message.obtain(fragment.getHandler(), R.id.decode_failed);
                message.sendToTarget();
            }
            error1 = engine1.AFR_FSDK_GetVersion(version1);
            Log.d(TAG, "FR=" + version.toString() + "," + error1.getCode()); //(210, 178 - 478, 446), degree = 1　780, 2208 - 1942, 3370
            error1 = engine1.AFR_FSDK_ExtractFRFeature(data, width, height, AFR_FSDKEngine.CP_PAF_NV21, new Rect(result.get(0).getRect()), result.get(0).getDegree(), result1);
            Log.d(TAG, "Face=" + result1.getFeatureData()[0] + "," + result1.getFeatureData()[1] + "," + result1.getFeatureData()[2] + "," + error1.getCode());
            if (error1.getCode() == error1.MOK) {
                if (order == CaptureActivity.REGISTER) {
                    YuvImage yuvImage = new YuvImage(data, ImageFormat.NV21, width, height, null);
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    yuvImage.compressToJpeg(new Rect(0, 0, width, height), 80, byteArrayOutputStream);
                    byte[] faceData = byteArrayOutputStream.toByteArray();
                    Bitmap bitmap = BitmapFactory.decodeByteArray(faceData, 0, faceData.length);
                    Message message = Message.obtain(fragment.getHandler(), R.id.decode_succeeded, result1);
                    Bundle bundle = new Bundle();
                    bundle.putParcelable(DecodeThread.BARCODE_BITMAP, bitmap);
                    message.setData(bundle);
                    //Log.d(TAG, "Sending decode succeeded message...");
                    message.sendToTarget();
                }
                return result1;
            }
        } else {
            Message message = Message.obtain(fragment.getHandler(), R.id.decode_failed);
            message.sendToTarget();
        }
        error = engine.AFD_FSDK_UninitialFaceEngine();
        Log.d(TAG, "AFD_FSDK_UninitialFaceEngine =" + error.getCode());
        return null;
    }

    private void detector(byte[] data, int width, int height) {
        AFR_FSDKFace resultT = register(data, width, height);
        if (resultT == null) return;

        AFR_FSDKEngine engine = new AFR_FSDKEngine();
        AFR_FSDKVersion version = new AFR_FSDKVersion();
        AFR_FSDKError error = engine.AFR_FSDK_InitialEngine(FaceDB.APP_ID, FaceDB.FACE_RECOGNITION);
        error = engine.AFR_FSDK_GetVersion(version);
        List<FaceDB.FaceRegist> mResgist = SampleApplication.mFaceDB.mRegister;
        AFR_FSDKMatching score = new AFR_FSDKMatching();

        float max = 0f;
        String name = null;
        for (FaceDB.FaceRegist fr : mResgist) {
            for (AFR_FSDKFace face : fr.mFaceList.values()) {
                error = engine.AFR_FSDK_FacePairMatching(resultT, face, score);
                Log.d(TAG, "detector: " + error.getCode());
                if (max < score.getScore()) {
                    max = score.getScore();
                    name = fr.mName;
                }
            }
        }
        final StringBuilder builder = new StringBuilder();
        if (max > 0.6f) {
            builder.append("{");
            builder.append("success");
            builder.append(":");
            builder.append(true);
            builder.append(",");
            builder.append("recognizedName");
            builder.append(":");
            builder.append(name);
            builder.append(",");
            builder.append("confidence");
            builder.append(":");
            builder.append((float) ((int) (max * 1000)) / 1000.0);
            builder.append("}");

            Message message = Message.obtain(fragment.getHandler(), R.id.decode_succeeded, builder.toString());
            Bundle bundle = new Bundle();
            bundle.putParcelable(DecodeThread.BARCODE_BITMAP, null);
            message.setData(bundle);
            //Log.d(TAG, "Sending decode succeeded message...");
            message.sendToTarget();
        } else {
            builder.append("{");
            builder.append("success");
            builder.append(":");
            builder.append(false);
            builder.append(",");
            builder.append("recognizedName");
            builder.append(":");
            builder.append(name);
            builder.append(",");
            builder.append("confidence");
            builder.append(":");
            builder.append((float) ((int) (max * 1000)) / 1000.0);
            builder.append("}");
            Log.d(TAG, "未识别 :" + builder.toString());
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Message message = Message.obtain(fragment.getHandler(), R.id.decode_failed, builder.toString());
                    message.sendToTarget();
                }
            }, 500);
        }

    }

}
