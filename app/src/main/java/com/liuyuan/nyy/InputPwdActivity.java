package com.liuyuan.nyy;


import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.liuyuan.nyy.util.TtsFuncUtil;
import com.nightonke.blurlockview.BlurLockView;
import com.nightonke.blurlockview.Directions.HideType;
import com.nightonke.blurlockview.Directions.ShowType;
import com.nightonke.blurlockview.Eases.EaseType;
import com.nightonke.blurlockview.Password;


public class InputPwdActivity extends AppCompatActivity implements BlurLockView.OnPasswordInputListener {
    private BlurLockView mBlurLockView;
    private ImageView backgroung;
    private Toast mToast;

    //记录输入密码错误次数
    private int wrongTime = 0;
    //语音合成类
    private TtsFuncUtil mTtsFuncUtil = new TtsFuncUtil();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_pwd);

        initUi();

    }

    private void initUi() {
        mBlurLockView = (BlurLockView) findViewById(R.id.blurlockview);
        backgroung = (ImageView) findViewById(R.id.image_1);
        mBlurLockView.setBlurredView(backgroung);
        mBlurLockView.setCorrectPassword(SpeechApp.getmPassword());
        mBlurLockView.setTitle(getString(R.string.set_blur_radius_title));
        mBlurLockView.setLeftButton(getString(R.string.set_left_button));
        mBlurLockView.setRightButton(getString(R.string.set_right_button));
        mBlurLockView.setType(Password.NUMBER,false);
        mBlurLockView.setTypeface(Typeface.DEFAULT);
        mBlurLockView.setBlurRadius(10);

        mBlurLockView.show(500,getShowType(4),getEaseType(30));
        mBlurLockView.setOnPasswordInputListener(this);

        mToast = Toast.makeText(InputPwdActivity.this, "", Toast.LENGTH_SHORT);

    }


    @Override
    public void correct(String inputPassword) {
        showTip(getString(R.string.password_correct));
        mBlurLockView.hide(500,getHideType(4),getEaseType(30));

    }


    @Override
    public void incorrect(String inputPassword) {
        wrongTime++;
        if(wrongTime>=3){
            showTip(getString(R.string.wrong_time_exceed_three));
            //打开及时器设置30s后重新打开BlurLockView

            wrongTime = 0;
        }else {
            showTip(getString(R.string.password_incorrect));
        }
    }

    @Override
    public void input(String inputPassword) {

    }

    private ShowType getShowType(int p) {
        ShowType showType = ShowType.FROM_TOP_TO_BOTTOM;
        switch (p) {
            case 0:
                showType = ShowType.FROM_TOP_TO_BOTTOM;
                break;
            case 1:
                showType = ShowType.FROM_RIGHT_TO_LEFT;
                break;
            case 2:
                showType = ShowType.FROM_BOTTOM_TO_TOP;
                break;
            case 3:
                showType = ShowType.FROM_LEFT_TO_RIGHT;
                break;
            case 4:
                showType = ShowType.FADE_IN;
                break;
        }
        return showType;
    }

    private HideType getHideType(int p) {
        HideType hideType = HideType.FROM_TOP_TO_BOTTOM;
        switch (p) {
            case 0:
                hideType = HideType.FROM_TOP_TO_BOTTOM;
                break;
            case 1:
                hideType = HideType.FROM_RIGHT_TO_LEFT;
                break;
            case 2:
                hideType = HideType.FROM_BOTTOM_TO_TOP;
                break;
            case 3:
                hideType = HideType.FROM_LEFT_TO_RIGHT;
                break;
            case 4:
                hideType = HideType.FADE_OUT;
                break;
        }
        return hideType;
    }

    private EaseType getEaseType(int p) {
        EaseType easeType = EaseType.Linear;
        switch (p) {
            case 0:
                easeType = EaseType.EaseInSine;
                break;
            case 1:
                easeType = EaseType.EaseOutSine;
                break;
            case 2:
                easeType = EaseType.EaseInOutSine;
                break;
            case 3:
                easeType = EaseType.EaseInQuad;
                break;
            case 4:
                easeType = EaseType.EaseOutQuad;
                break;
            case 5:
                easeType = EaseType.EaseInOutQuad;
                break;
            case 6:
                easeType = EaseType.EaseInCubic;
                break;
            case 7:
                easeType = EaseType.EaseOutCubic;
                break;
            case 8:
                easeType = EaseType.EaseInOutCubic;
                break;
            case 9:
                easeType = EaseType.EaseInQuart;
                break;
            case 10:
                easeType = EaseType.EaseOutQuart;
                break;
            case 11:
                easeType = EaseType.EaseInOutQuart;
                break;
            case 12:
                easeType = EaseType.EaseInQuint;
                break;
            case 13:
                easeType = EaseType.EaseOutQuint;
                break;
            case 14:
                easeType = EaseType.EaseInOutQuint;
                break;
            case 15:
                easeType = EaseType.EaseInExpo;
                break;
            case 16:
                easeType = EaseType.EaseOutExpo;
                break;
            case 17:
                easeType = EaseType.EaseInOutExpo;
                break;
            case 18:
                easeType = EaseType.EaseInCirc;
                break;
            case 19:
                easeType = EaseType.EaseOutCirc;
                break;
            case 20:
                easeType = EaseType.EaseInOutCirc;
                break;
            case 21:
                easeType = EaseType.EaseInBack;
                break;
            case 22:
                easeType = EaseType.EaseOutBack;
                break;
            case 23:
                easeType = EaseType.EaseInOutBack;
                break;
            case 24:
                easeType = EaseType.EaseInElastic;
                break;
            case 25:
                easeType = EaseType.EaseOutElastic;
                break;
            case 26:
                easeType = EaseType.EaseInOutElastic;
                break;
            case 27:
                easeType = EaseType.EaseInBounce;
                break;
            case 28:
                easeType = EaseType.EaseOutBounce;
                break;
            case 29:
                easeType = EaseType.EaseInOutBounce;
                break;
            case 30:
                easeType = EaseType.Linear;
                break;
        }
        return easeType;
    }

    private void showTip(final String str) {
        mToast.setText(str);
        mToast.show();
        mTtsFuncUtil.ttsFunction(this, str);
    }

}
