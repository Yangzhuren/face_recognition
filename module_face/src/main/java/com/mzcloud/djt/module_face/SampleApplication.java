package com.mzcloud.djt.module_face;

import com.mzcloud.djt.module_face.camera.FaceDB;

public class SampleApplication extends android.app.Application {
    public static FaceDB mFaceDB;

    @Override
    public void onCreate() {
        super.onCreate();
        mFaceDB = new FaceDB(getExternalCacheDir().getPath());
    }
}
