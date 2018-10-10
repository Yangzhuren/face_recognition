package com.mz_cloud.djt.djtadvanced;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn_detection).setOnClickListener(this);
        findViewById(R.id.btn_recognition).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_detection:
                // 面部识别
                openCameraToDetection();
                break;
            case R.id.btn_recognition:
                // 面部比对
                openCameraToRecognition();
                break;
        }
    }

    private void openCameraToDetection() {
    }

    private void openCameraToRecognition() {
    }
}
