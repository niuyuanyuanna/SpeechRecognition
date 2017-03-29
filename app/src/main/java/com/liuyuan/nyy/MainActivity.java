package com.liuyuan.nyy;

import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;
import com.iflytek.sunflower.FlowerCollector;
import com.liuyuan.nyy.regist.RegistActivity;
import com.liuyuan.nyy.util.JsonParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedHashMap;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Intent intent;
    private String group_id;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initUi();
    }

    private void initUi() {
        group_id = getIntent().getStringExtra("group_id");
        findViewById(R.id.btnLogin).setOnClickListener(this);
        findViewById(R.id.btnMixVerify).setOnClickListener(this);
        findViewById(R.id.btnInputPwd).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnLogin:
                intent = new Intent(this, RegistActivity.class);
                break;
            case R.id.btnMixVerify:
                intent = new Intent(this,MixVerify2.class);
                intent.putExtra("group_id",group_id);
                break;
            case R.id.btnInputPwd:
                intent = new Intent(this,InputPwdActivity.class);
                break;
            default:
                break;
        }
        startActivity(intent);
    }


}
