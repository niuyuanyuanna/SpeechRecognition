package com.liuyuan.nyy.regist;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.liuyuan.nyy.IsvDemo;
import com.liuyuan.nyy.R;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 用户登陆界面
 */

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private Toast mToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
//        requestWindowFeature(Window.FEATURE_NO_TITLE);

        findViewById(R.id.btnConfirm).setOnClickListener(LoginActivity.this);
        mToast = Toast.makeText(LoginActivity.this, "", Toast.LENGTH_SHORT);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnConfirm:
                //过滤不合法的用户名
                String isname = "liuyuan";
                String name = ((EditText) findViewById(R.id.etName)).getText().toString();
                if (TextUtils.isEmpty(name)) {
                    showTip("用户名不能为空");
                    return;
                } else {
                    Pattern pattern = Pattern.compile("[\u4e00-\u9fa5]");
                    Matcher macher = pattern.matcher(name);
                    if (macher.find()) {
                        showTip("不支持中文字符");
                        return;
                    } else if (name.contains(" ")) {
                        showTip("不能包含空格");
                        return;
                    } else if (!name.matches("^[a-zA-Z][a-zA-Z0-9_]{5,17}")) {
                        showTip("6-18个字母、数字或下划线线的组合，以字母开头");
                        return;
                    }
                }

                //过滤不合法密码
                String isPassword = "niuyuanyuan";
                String password = ((EditText) findViewById(R.id.etPassword)).getText().toString();
                if (TextUtils.isEmpty(password)) {
                    showTip("密码不能为空");
                    return;
                } else {
                    Pattern pattern = Pattern.compile("[\u4e00-\u9fa5]");
                    Matcher macher = pattern.matcher(password);
                    if (macher.find()) {
                        showTip("不支持中文字符");
                        return;
                    } else if (name.contains(" ")) {
                        showTip("不能包含空格");
                        return;
                    } else if (!name.matches("^[a-zA-Z][a-zA-Z0-9_]{5,17}")) {
                        showTip("6-18个字母、数字或下划线线的组合");
                        return;
                    }
                }

                if (name.equals(isname) && password.equals(isPassword)) {
                    Intent intent = new Intent(LoginActivity.this, RegistActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("name", name);
                    bundle.putString("password", password);
                    intent.putExtras(bundle);
                    startActivity(intent);
                } else {
                    showTip("用户名和密码输入错误");
                    return;
                }
                break;
            default:
                break;
        }

    }

    private void showTip(final String str) {
        mToast.setText(str);
        mToast.show();
    }
}
