package com.mzcloud.djt.module_face;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.mzcloud.djt.module_face.camera.activity.CaptureActivity;

public class FaceSampleActivity extends AppCompatActivity implements View.OnClickListener {
    private TextView resultText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_sample);
        findViewById(R.id.btn_detection).setOnClickListener(this);
        findViewById(R.id.btn_recognition).setOnClickListener(this);
        resultText = findViewById(R.id.back_message);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_detection:
                detection();
                break;
            case R.id.btn_recognition:
                recognition();
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == 11 || requestCode == 12) && data != null && data.hasExtra("Content")) {
            String result = data.getStringExtra("Content");
            if (!TextUtils.isEmpty(result))
                resultText.setText(result);
        }
    }

    private void detection() {
        CaptureActivity.actionStart(this, CaptureActivity.REGISTER, 11, "mine");
//        Intent intent = new Intent(this, CaptureActivity.class);
//        startActivity(intent);
    }

    private void recognition() {
        CaptureActivity.actionStart(this, CaptureActivity.DETECTER, 12, "mine");

    }
}
