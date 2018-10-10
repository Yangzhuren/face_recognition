package com.mz_cloud.djt.djtadvanced;

import android.hardware.Camera;
import android.media.AudioManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

public class CameraActivity extends AppCompatActivity implements SurfaceHolder.Callback {
    private SurfaceView mPreview;
    private DetectionView mWindowView;
    private InactivityTimer mInactivityTimer;
    private boolean mHasSurface;
    private SurfaceHolder mSurfaceHolder;
    private static final float BEEP_VOLUME = 0.10f;
    private boolean playBeep;
    private boolean vibrate;
    private Camera camera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CameraManager.init(getApplication());
        mInactivityTimer = new InactivityTimer(this);
        setContentView(R.layout.activity_camera);

        mPreview = findViewById(R.id.sv_preview);
        mWindowView = findViewById(R.id.dv_detection_window);

    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mHasSurface){
            initCamera(mSurfaceHolder);
        }else{
            mSurfaceHolder.addCallback(this);
            mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }
        playBeep = true;
        AudioManager audioService = (AudioManager)getSystemService(AUDIO_SERVICE);
        if (audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
            playBeep = false;
        }
        initBeepSound();
        vibrate = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        CameraManager.get().stopPreview();
        CameraManager.get().closeDriver();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if(!mHasSurface){
            mHasSurface = true;
            initCamera(holder);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mInactivityTimer.shutdown();
    }

    private void initCamera(SurfaceHolder holder){
        try{
            CameraManager.get().openDriver(holder);
            camera = CameraManager.get().getCamera();
        }catch (IOException e){
            return;
        }catch (RuntimeException e){
            return;
        }
        CameraManager.get().startPreview();
        mWindowView.drawViewfinder();
    }

    private void initBeepSound(){}
}
