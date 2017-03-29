package com.liuyuan.nyy.regist;

import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.Toast;

import com.liuyuan.nyy.MainActivity;
import com.liuyuan.nyy.R;
import com.liuyuan.nyy.SpeechApp;
import com.liuyuan.nyy.entity.GroupHisList;
import com.liuyuan.nyy.entity.User;
import com.liuyuan.nyy.ui.DropEditText;
import com.liuyuan.nyy.util.SaveFuncUtil;
import com.nightonke.blurlockview.BlurLockView;
import com.nightonke.blurlockview.Directions.HideType;
import com.nightonke.blurlockview.Directions.ShowType;
import com.nightonke.blurlockview.Eases.EaseType;
import com.nightonke.blurlockview.Password;

public class RegistActivity extends AppCompatActivity
        implements
        View.OnClickListener,
        BlurLockView.OnPasswordInputListener,
        BlurLockView.OnLeftButtonClickListener {
    private EditText edt_username;
    private EditText edt_pwd;
    private DropEditText mGroupIDDrop;
    private BlurLockView mBlurLockView;
    private Toast mToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_regist);

        intUi();
        mToast = Toast.makeText(RegistActivity.this, "", Toast.LENGTH_SHORT);
    }

    private void intUi() {
        //从本地读出历史纪录
        SpeechApp.setmHisList((GroupHisList) SaveFuncUtil.readObject(this, SpeechApp.HIS_FILE_NAME));
        mGroupIDDrop = (DropEditText) findViewById(R.id.drop_edit);


        findViewById(R.id.btnVoiceConfirm).setOnClickListener(this);
        findViewById(R.id.btnFaceConfirm).setOnClickListener(this);
        findViewById(R.id.btnGroupManage).setOnClickListener(this);
        findViewById(R.id.btn_confirm).setOnClickListener(this);
        findViewById(R.id.btn_pwd_comfirm).setOnClickListener(this);
        edt_username = (EditText) findViewById(R.id.edt_username);
        edt_pwd = (EditText) findViewById(R.id.edt_pwd);

        mBlurLockView = (BlurLockView) findViewById(R.id.blurlockview);
        mBlurLockView.setBlurredView(findViewById(R.id.view_regist));
        mBlurLockView.setCorrectPassword("666666");
        mBlurLockView.setTitle(getString(R.string.set_blur_radius_title));
        mBlurLockView.setLeftButton(getString(R.string.set_left_button));
        mBlurLockView.setRightButton(getString(R.string.set_right_button));
        mBlurLockView.setType(Password.NUMBER, false);
        mBlurLockView.setTypeface(Typeface.DEFAULT);
        mBlurLockView.setBlurRadius(20);
        mBlurLockView.setDownsampleFactor(20);

        mBlurLockView.show(0, ShowType.FADE_IN, EaseType.Linear);
        mBlurLockView.setOnPasswordInputListener(this);
        mBlurLockView.setOnLeftButtonClickListener(this);


    }

    @Override
    protected void onResume() {
        mGroupIDDrop.setStringList(RegistActivity.this,
                SpeechApp.getmHisList().getGroupInfo_list(),
                SpeechApp.getmHisList().getGroup_list());
        BaseAdapter adp = mGroupIDDrop.getAdapter();
        if (adp != null)
            adp.notifyDataSetChanged();
        super.onResume();
    }

    @Override
    public void onClick() {
        finish();
    }

    @Override
    public void onClick(View view) {
        String uname = null;
        Intent intent = null;
        uname = edt_username.getText().toString();
        switch (view.getId()) {
            case R.id.btnVoiceConfirm:
                if (TextUtils.isEmpty(uname)) {
                    showTip("用户名不能为空");
                    return;
                }
                //设置全局mAuth_id
                SpeechApp.mAuth_id = uname;
                SpeechApp.setHostUser((User) SaveFuncUtil.readObject(this, uname));
                SpeechApp.getHostUser().setUsername(uname);
                SaveFuncUtil.saveObject(this, SpeechApp.getHostUser(), uname);

                //跳转至声纹注册界面
                intent = new Intent(RegistActivity.this, VocalRegist.class);
                intent.putExtra("scenes", "ivp");
                startActivity(intent);
                break;
            case R.id.btnFaceConfirm:
                if (TextUtils.isEmpty(uname)) {
                    showTip("用户名不能为空");
                    return;
                }
                //设置全局mAuth_id
                SpeechApp.mAuth_id = uname;
                SpeechApp.setHostUser((User) SaveFuncUtil.readObject(this, uname));
                SpeechApp.getHostUser().setUsername(uname);
                SaveFuncUtil.saveObject(this, SpeechApp.getHostUser(), uname);

                //跳转至人脸注册界面
                intent = new Intent(RegistActivity.this, FaceRegist.class);
                intent.putExtra("scenes", "ifr");
                startActivity(intent);
                break;
            case R.id.btnGroupManage:
                if (TextUtils.isEmpty(uname)) {
                    showTip("用户名不能为空");
                    return;
                }
                //设置全局mAuth_id
                SpeechApp.mAuth_id = uname;
                SpeechApp.setHostUser((User) SaveFuncUtil.readObject(this, uname));
                SpeechApp.getHostUser().setUsername(uname);
                SaveFuncUtil.saveObject(this, SpeechApp.getHostUser(), uname);

                //跳转至组别管理界面
                intent = new Intent(RegistActivity.this, GroupManage.class);
                startActivity(intent);
                break;
            case R.id.btn_confirm:
                String group_id = mGroupIDDrop.getText();
                if (TextUtils.isEmpty(group_id)) {
                    mGroupIDDrop.requestFocus();
                    showTip(getString(R.string.groupid_null));
                    return;
                }
                SpeechApp.setGroup_id(group_id);
                break;
            case R.id.btn_pwd_comfirm:
                String passwod = edt_pwd.getText().toString();
                if (TextUtils.isEmpty(passwod)) {
                    showTip("密码不能为空");
                    return;
                } else {
                    SpeechApp.setmPassword(passwod);
                    showTip("密码已保存");
                }
                break;
            default:
                break;
        }

    }


    @Override
    public void correct(String inputPassword) {
        showTip(getString(R.string.password_correct));
        mBlurLockView.hide(500, HideType.FADE_OUT, EaseType.Linear);


    }

    @Override
    public void incorrect(String inputPassword) {
        showTip(getString(R.string.password_incorrect));
    }

    @Override
    public void input(String inputPassword) {

    }

    private void showTip(String str) {
        mToast.setText(str);
        mToast.show();
    }

}
