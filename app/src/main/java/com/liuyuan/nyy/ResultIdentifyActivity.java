package com.liuyuan.nyy;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

public class ResultIdentifyActivity extends AppCompatActivity  {
    private static final String TAG = ResultIdentifyActivity.class.getSimpleName();

    private String group_id;
    private String name_f;
    private String name_v;
    private double score_f;
    private double score_v;
    private TextView result;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result_identify);

        initUi();

        final Intent localIntent = new Intent(this,MainActivity.class);
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                localIntent.putExtra("group_id",group_id);
                startActivity(localIntent);
            }
        };
        timer.schedule(task,5000);
    }

    private void initUi() {
        result = (TextView) findViewById(R.id.result);
        Bundle bundle = getIntent().getExtras();
        group_id = bundle.getString("group_id");
        name_f = bundle.getString("name_f");
        name_v = bundle.getString("name_v");
        score_v = bundle.getDouble("score_v");
        score_f = bundle.getDouble("score_f");
        result.setText(String.format("声纹测试结果："+name_v+"  得分："+score_v+"\n人脸测试结果："+name_f+"  得分："+score_f));

    }


}
