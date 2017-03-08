package com.liuyuan.nyy.regist;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.liuyuan.nyy.R;
import com.liuyuan.nyy.SpeechApp;
import com.liuyuan.nyy.entity.User;
import com.liuyuan.nyy.util.SaveFuncUtil;

public class RegistActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText  edt_username;
    private Toast mToast;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_regist);

        intUi();
        mToast = Toast.makeText(RegistActivity.this,"",Toast.LENGTH_SHORT);
    }

    private void intUi() {
        findViewById(R.id.btnVoiceConfirm).setOnClickListener(this);
        findViewById(R.id.btnFaceConfirm).setOnClickListener(this);
        findViewById(R.id.btnGroupManage).setOnClickListener(this);
        edt_username = (EditText) findViewById(R.id.edt_username);
    }

    @Override
    public void onClick(View view) {
        String uname = null;
        Intent intent = null;
        uname = edt_username.getText().toString();
        switch (view.getId()){
            case R.id.btnVoiceConfirm:
                if(TextUtils.isEmpty(uname)){
                    showTip("用户名不能为空");
                    return;
                }
                //设置全局mAuth_id
                SpeechApp.mAuth_id = uname;
                SpeechApp.setHostUser((User) SaveFuncUtil.readObject(this,uname));
                SpeechApp.getHostUser().setUsername(uname);
                SaveFuncUtil.saveObject(this,SpeechApp.getHostUser(),uname);

                //跳转至声纹注册界面
                intent = new Intent(RegistActivity.this,VocalRegist.class);
                intent.putExtra("scenes", "ivp");
                startActivity(intent);
                break;
            case R.id.btnFaceConfirm:
                if(TextUtils.isEmpty(uname)){
                    showTip("用户名不能为空");
                    return;
                }
                //设置全局mAuth_id
                SpeechApp.mAuth_id = uname;
                SpeechApp.setHostUser((User) SaveFuncUtil.readObject(this,uname));
                SpeechApp.getHostUser().setUsername(uname);
                SaveFuncUtil.saveObject(this,SpeechApp.getHostUser(),uname);

                //跳转至人脸注册界面
                intent = new Intent(RegistActivity.this,FaceRegist.class);
                intent.putExtra("scenes", "ifr");
                startActivity(intent);
                break;
            case R.id.btnGroupManage:
                if(TextUtils.isEmpty(uname)){
                    showTip("用户名不能为空");
                    return;
                }
                //设置全局mAuth_id
                SpeechApp.mAuth_id = uname;
                SpeechApp.setHostUser((User) SaveFuncUtil.readObject(this,uname));
                SpeechApp.getHostUser().setUsername(uname);
                SaveFuncUtil.saveObject(this,SpeechApp.getHostUser(),uname);

                //跳转至组别管理界面
                intent = new Intent(RegistActivity.this,GroupManage.class);
                startActivity(intent);
                break;
            default:
                break;
        }

    }

    private void showTip(String str) {
        mToast.setText(str);
        mToast.show();
    }
}
