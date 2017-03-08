package com.liuyuan.nyy;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.liuyuan.nyy.regist.LoginActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btnIat).setOnClickListener(this);
        findViewById(R.id.btnLogin).setOnClickListener(this);
        findViewById(R.id.btnTts).setOnClickListener(this);
        findViewById(R.id.btnMixVerify).setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnIat:
                intent = new Intent(this,IatDemo.class);
                break;
            case R.id.btnTts:
                intent = new Intent(this,TtsDemo.class);
                break;
            case R.id.btnMixVerify:
                intent = new Intent (this,GroupIdentify.class);
                break;
            case R.id.btnLogin:
                intent = new Intent(this,LoginActivity.class);
                break;
            default:
                break;
        }
        startActivity(intent);

    }
}
