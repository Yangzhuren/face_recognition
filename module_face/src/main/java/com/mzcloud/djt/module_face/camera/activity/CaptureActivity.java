package com.mzcloud.djt.module_face.camera.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.widget.Toast;

import com.mzcloud.djt.module_face.R;
import com.mzcloud.djt.module_face.SampleApplication;
import com.mzcloud.djt.module_face.camera.utilclass.DisplayUtil;

import java.lang.annotation.Inherited;

/**
 * Initial the camera
 * <p>
 * 默认的二维码扫描Activity
 */
public class CaptureActivity extends AppCompatActivity {
    public static final int REGISTER = 0;
    public static final int DETECTER = 1;
    private static final String ORDER = "order";
    private static final String NAME = "name";
    private int order;
    private String name;

    @IntDef({REGISTER, DETECTER})
    @interface CaptureOrder {
    }


    public static void actionStart(Activity activity, @CaptureOrder int order, int requestCode, String name) {
        if (order == DETECTER && SampleApplication.mFaceDB.mRegister.isEmpty()) {
            Toast.makeText(activity, "没有注册人脸，请先注册！", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(activity, CaptureActivity.class);
        intent.putExtra(ORDER, order);
        intent.putExtra(NAME, name);
        activity.startActivityForResult(intent, requestCode);
    }

    public int getOrder() {
        return order;
    }

    public String getName() {
        return name;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera);
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(ORDER)) {
            order = intent.getIntExtra(ORDER, -1);
            name = intent.getStringExtra(NAME);
        }
        CaptureFragment captureFragment = new CaptureFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.fl_zxing_container, captureFragment).commit();
        initDisplayOpinion();
    }


    protected void decode_success(String str) {
//        Intent intent = new Intent();
//        intent.setClass(CaptureActivity.this,ResultActivity.class);
//        Bundle bundle=new Bundle();
//        bundle.putString("Tips","扫描解析成功！");
//        bundle.putString("Content",str);
//        intent.putExtras(bundle);
//        startActivity(intent);
        Intent intent = new Intent();
        intent.putExtra("Content", str);
        setResult(RESULT_OK, intent);
        CaptureActivity.this.finish();
    }

    private void initDisplayOpinion() {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        DisplayUtil.density = dm.density;
        DisplayUtil.densityDPI = dm.densityDpi;
        DisplayUtil.screenWidthPx = dm.widthPixels;
        DisplayUtil.screenhightPx = dm.heightPixels;
        DisplayUtil.screenWidthDip = DisplayUtil.px2dip(getApplicationContext(), dm.widthPixels);
        DisplayUtil.screenHightDip = DisplayUtil.px2dip(getApplicationContext(), dm.heightPixels);
    }

}