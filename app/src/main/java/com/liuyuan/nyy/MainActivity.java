package com.liuyuan.nyy;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btnIat).setOnClickListener(this);
        findViewById(R.id.btnIsv).setOnClickListener(this);
        findViewById(R.id.btnFace).setOnClickListener(this);
        findViewById(R.id.btnTts).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnIat:
                intent = new Intent(this,IatDemo.class);
                break;
            case R.id.btnIsv:
                intent = new Intent(this,LoginActivity.class);
                break;
            case R.id.btnFace:
                intent = new Intent (this,FaceDemo.class);
                break;
            case R.id.btnTts:
                intent = new Intent(this,TtsDemo.class);
                break;
            default:
                break;
        }
        startActivity(intent);

    }
}
