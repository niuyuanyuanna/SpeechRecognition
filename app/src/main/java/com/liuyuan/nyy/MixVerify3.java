package com.liuyuan.nyy;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.IdentityListener;
import com.iflytek.cloud.IdentityResult;
import com.iflytek.cloud.IdentityVerifier;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechEvent;
import com.iflytek.cloud.record.PcmRecorder;
import com.iflytek.cloud.util.VerifierUtil;
import com.liuyuan.nyy.ui.HintPopupWindow;
import com.liuyuan.nyy.ui.RecordView;
import com.liuyuan.nyy.util.CameraHelper;
import com.liuyuan.nyy.util.DensityUtil;
import com.liuyuan.nyy.util.SaveFuncUtil;
import com.liuyuan.nyy.util.TtsFuncUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MixVerify3 extends AppCompatActivity implements View.OnClickListener, SurfaceHolder.Callback {
    private static final String TAG = MixVerify3.class.getSimpleName();

    //是否登陆成功
    private boolean mLoginSuccess_v = false;
    private boolean mLoginSuccess_f = false;

    private String name_f;
    private String name_v;
    private Double score_f;
    private Double score_v;

    private Camera mCamera;
    private int mCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
    private Camera.Size mPreviewSize;
    private boolean mIsPreviewing = false;
    private boolean mCanTakePic = true;
    private boolean mIsPause = false;
    private CameraHelper mCameraHelper;

    //相机预览SuifaceView
    private SurfaceView mPreviewSurface;
    private TextView mHintTextView;
    private TextView mPwdTextView;
    private TextView tvGroupId;
    private ImageButton mFlashSwitchButton;
    private ImageButton mChangeCameraButton;
    private ProgressDialog mProDialog;
    private RecordView mVolView;
    private ImageButton mRecordButton;
    private boolean mRecordButtonPressed;
    private HintPopupWindow mPopupHint;
    //提示框显示位置的纵坐标
    private int mHintOffsetY;

    // 用户输入的组ID
    private String mGroupId;


    //身份验证对象
    private IdentityVerifier mIdVerifier;
    //验证数字密码
    private String mVerifyNumPwd;
    //验证密码类型，3：数字密码，1：文字密码
    private static final String PWD_TYPE_NUM = "3";
    // 录音尾端点
    private static final String VAD_EOS = "2000";
    // 录音采样率
    private final int SAMPLE_RATE = 16000;
    // pcm录音机
    private PcmRecorder mPcmRecorder;

    // 是否开始验证
    private boolean mVerifyStarted = false;
    // 操作是否被其他应用中断
    private boolean mInterruptedByOtherApp = false;
    // 按住麦克风为true开始写音频，松开为false停止写入
    private boolean mWriteAudio = false;
    private boolean mMoveOutofBound = false;
    private final int DELAY_TIME = 1000;
    // 在松开麦克风之前是否已经出现错误
    private boolean mErrorOccurBeforeUp = false;

    private static final int MSG_FACE_START = 1;
    private static final int MSG_TAKE_PICTURE = 2;
    private static final int MSG_PCM_START = 3;
    private static final int MSG_PCM_STOP = 4;
    private static final int MSG_RESULT_DISMISS = 5;
    private static final int MSG_ACTIVITY_FINISH = 6;

    private AlertDialog resultDialog = null;
    private AlertDialog.Builder mBuider = null;
    private Toast mToast;
    //语音合成类
    private TtsFuncUtil mTtsFuncUtil = new TtsFuncUtil();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mix_verify_two);

        mCameraHelper = CameraHelper.createHelper(MixVerify3.this);

        initUi();

        mIdVerifier = IdentityVerifier.createVerifier(getApplicationContext(), new InitListener() {
            @Override
            public void onInit(int i) {
                if (ErrorCode.SUCCESS == i) {
//                    showTip("引擎初始化成功");
                } else
                    showTip("引擎初始化失败，错误码：" + i);
            }
        });
    }

    private void initUi() {

        mPreviewSurface = (SurfaceView) findViewById(R.id.sfv_preview);
        mPreviewSurface.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        //设置屏幕常亮
        mPreviewSurface.getHolder().setKeepScreenOn(true);
        //surfaceView增加回调句柄
        mPreviewSurface.getHolder().addCallback(this);

        tvGroupId = (TextView) findViewById(R.id.tv_group_id);
        mHintTextView = (TextView) findViewById(R.id.txt_hint);
        mPwdTextView = (TextView) findViewById(R.id.txt_num);
        mFlashSwitchButton = (ImageButton) findViewById(R.id.btn_flash_switch);
        mChangeCameraButton = (ImageButton) findViewById(R.id.btn_change_camera);
        mRecordButton = (ImageButton) findViewById(R.id.btn_record);

        //设置组ID
        if (getIntent().getStringExtra("group_id") != null) {
            mGroupId = getIntent().getStringExtra("group_id");
        } else {
            mGroupId = "1841946400";
        }
        tvGroupId.setText(mGroupId);

        mProDialog = new ProgressDialog(this);
        mProDialog.setCancelable(true);
        mProDialog.setCanceledOnTouchOutside(false);
        mProDialog.setTitle("请稍后");
        mProDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                mIdVerifier.cancel();
                mVerifyStarted = false;
//                mPopupHint.setHint("请按住麦克风说话");
            }
        });

        mChangeCameraButton.setOnClickListener(this);
        mFlashSwitchButton.setOnClickListener(this);
        mToast = Toast.makeText(MixVerify3.this, "", Toast.LENGTH_SHORT);


        mPopupHint = new HintPopupWindow(MixVerify3.this);
//        mPopupHint.setHint(getString(R.string.vocal_register_press_hint));

        mVolView = new RecordView(MixVerify3.this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        // 获取和设置验证密码
        mVerifyNumPwd = VerifierUtil.generateNumberPassword(8);
        mPwdTextView.setText(getStyledPwdHint(mVerifyNumPwd));

        mIsPause = false;

        // 显示操作提示
        mHintTextView.setText(R.string.login_operation_hint);
//        mPopupHint.setHint(getString(R.string.vocal_register_press_hint));
        mTtsFuncUtil.ttsFunction(this, mHintTextView.getText().toString() +
                mPwdTextView.getText().toString());

        mCanTakePic = true;
        if (mCamera != null) {
            mCamera.startPreview();
            mIsPreviewing = true;
        }

        if (mInterruptedByOtherApp) {
            showTip(getString(R.string.login_hint_interrupted));
            mInterruptedByOtherApp = false;
        }
        //打开Activity后延迟7S打开麦克风
        mHandler.sendEmptyMessageDelayed(MSG_PCM_START, 7000);
        //打开Activity后延迟14S关闭麦克风
        mHandler.sendEmptyMessageDelayed(MSG_PCM_STOP, 14000);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_change_camera:
                int cameracount = Camera.getNumberOfCameras();
                if (cameracount <= 1) {
                    showTip(getString(R.string.hint_change_not_support));
                    return;
                }
                // 先关闭摄相头
                closeCamera();

                if (Camera.CameraInfo.CAMERA_FACING_BACK == mCameraId) {
                    if (CameraHelper.hasCamera(Camera.CameraInfo.CAMERA_FACING_FRONT))
                        mCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
                } else if (Camera.CameraInfo.CAMERA_FACING_FRONT == mCameraId) {
                    if (CameraHelper.hasCamera(Camera.CameraInfo.CAMERA_FACING_BACK))
                        mCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
                } else {
                    showTip(getString(R.string.hint_change_not_support));
                    return;
                }

                openCamera();
                break;
            case R.id.btn_flash_switch:
                // 检查当前硬件设施是否支持闪光灯
                if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)
                        || mCameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    showTip(getString(R.string.hint_flash_not_support));
                    return;
                }
                Camera.Parameters param = mCamera.getParameters();
                String flasemode = param.getFlashMode();
                if (TextUtils.isEmpty(flasemode))
                    return;
                if (flasemode.equals(Camera.Parameters.FLASH_MODE_TORCH)) {
                    param.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                    mFlashSwitchButton.setImageResource(R.drawable.flash_close);
                    showTip(getString(R.string.hint_flash_closed));
                } else {
                    param.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                    mFlashSwitchButton.setImageResource(R.drawable.flash_open);
                    showTip(getString(R.string.hint_flash_opened));
                }
                // 防止参数设置部分手机failed
                try {
                    mCamera.setParameters(param);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

            default:
                break;
        }

    }

    private void actionDown() {
        mRecordButtonPressed = true;
        // 按下事件
        if (!mVerifyStarted) {
            if (null != mCamera && !mIsPreviewing) {
                mCamera.startPreview();
                mIsPreviewing = true;
            }
            if (!mVerifyStarted) {
                mWriteAudio = true;
                // 开启录音机
                mPcmRecorder = new PcmRecorder(SAMPLE_RATE, 40);
                try {
                    mPcmRecorder.startRecording(mPcmRecordListener);
                } catch (SpeechError e) {
                    e.printStackTrace();
                }
                // 开始验证
                startMFVVerify();
            }
            mPopupHint.setHint(getString(R.string.vocal_register_listening_hint));
        } else {
            showTip(getString(R.string.login_hint_verifying));
        }
    }

    private void actionUp() {
        mRecordButtonPressed = false;
        // 停止录音，开始拍照，随后开始人脸验证
        stopRecording();

        if (!mVerifyStarted) {
//            mPopupHint.setHint(getString(R.string.vocal_register_press_hint));
        }

        // 松开事件
        if (!mMoveOutofBound) {

            if (!mErrorOccurBeforeUp) {
                if (mPopupHint.isShowing()) {
                    mPopupHint.dismiss();
                }
                mHandler.sendEmptyMessageDelayed(MSG_TAKE_PICTURE, DELAY_TIME);
                showProDialog();
            }
        }

    }

    /**
     * 录音机监听器,向子业务传递参数
     */
    private PcmRecorder.PcmRecordListener mPcmRecordListener = new PcmRecorder.PcmRecordListener() {

        @Override
        public void onRecordStarted(boolean success) {
        }

        @Override
        public void onRecordReleased() {
        }

        @Override
        public void onRecordBuffer(byte[] data, int offset, int length) {
            if (mWriteAudio) {
                // 子业务执行参数，若无可以传空字符传
                StringBuffer params = new StringBuffer();
                params.append("ptxt=" + mVerifyNumPwd + ",");
                params.append("pwdt=" + PWD_TYPE_NUM + ",");
                params.append("group_id=" + mGroupId + ",");
                params.append("vad_eos=" + VAD_EOS + ",");
                params.append("topc = 1");

                Log.d(TAG, params.toString());
                // 向子业务写入声纹数据
                mIdVerifier.writeData("ivp", params.toString(), data, 0, length);
            }
        }

        @Override
        public void onError(final SpeechError e) {
            // 由于onError不在主线程中回调，所以要runOnUiThread
            runOnUiThread(new Runnable() {
                public void run() {
                    showTip(e.getPlainDescription(true));
                }
            });
        }
    };

    /**
     * 开始声纹验证，Touch事件监听器中case MotionEvent.ACTION_DOWN处理
     * 设置声纹鉴别对象参数
     * 声纹鉴别监听器开始工作
     */
    private void startMFVVerify() {
        Log.d(TAG, "startMFVVerify");

        mVerifyStarted = true;
        mLoginSuccess_v = false;

        // 设置融合验证参数
        // 清空参数
        mIdVerifier.setParameter(SpeechConstant.PARAMS, null);
        // 设置会话场景
        mIdVerifier.setParameter(SpeechConstant.MFV_SCENES, "ivp");
        // 设置会话类型
        mIdVerifier.setParameter(SpeechConstant.MFV_SST, "identify");
        // 设置组ID
        mIdVerifier.setParameter("group_id", mGroupId);
        // 设置监听器，开始会话
        mIdVerifier.startWorking(mVerifyListener);

    }

    /**
     * 语音判别监听器，识别通过与否，(返回评分)
     */
    private IdentityListener mVerifyListener = new IdentityListener() {

        @Override
        public void onResult(IdentityResult result, boolean islast) {
            Log.d(TAG, result.getResultString());

            mVolView.stopRecord();
            mVerifyStarted = false;

            if (null != mProDialog) {
                mProDialog.dismiss();
            }
            if (null == result) {
                return;
            }

            try {
                String resultStr_v = result.getResultString();
                JSONObject object = new JSONObject(resultStr_v);
                JSONObject ifv_result = object.getJSONObject("ifv_result");
                JSONArray candidates = ifv_result.getJSONArray("candidates");
                JSONObject obj = candidates.getJSONObject(0);

                int ret_v = object.getInt("ret");

                if (ErrorCode.SUCCESS != ret_v) {
//                    showTip(getString(R.string.login_vocal_failure_hint));
                    return;
                } else {
                    if ((obj.optString("decision")).equals("accepted")) {
                        mLoginSuccess_v = true;
                        name_v = obj.optString("user");
                        score_v = obj.optDouble("score");
                        // 保存到历史记录中
                        SpeechApp.getmHisList().addHisItem(object.getString("group_id"),
                                object.getString("group_name") + "(" + object.getString("group_id") + ")");
                        SaveFuncUtil.saveObject(MixVerify3.this, SpeechApp.getmHisList(), SpeechApp.HIS_FILE_NAME);

//                        showTip(getString(R.string.login_vocal_success_hint));
                        // 隐藏提示框
                        mPopupHint.dismiss();
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
            if (SpeechEvent.EVENT_VOLUME == eventType) {
                mVolView.setVolume(arg1);
            }

            // 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
            //	if (SpeechEvent.EVENT_SESSION_ID == eventType) {
            //		String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
            //		Log.d(TAG, "session id =" + sid);
            //	}
        }

        @Override
        public void onError(SpeechError error) {
            mVerifyStarted = false;
            mLoginSuccess_v = false;
            mVolView.stopRecord();

            if (null != mProDialog) {
                mProDialog.dismiss();
            }

            showTip(error.getPlainDescription(true));
        }

    };

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (null == mVolView.getParent()) {
            // 设置VolView圆圈中心位置为麦克风中心
            Rect rect = new Rect();
            mRecordButton.getHitRect(rect);
            mVolView.setCenterXY(rect.centerX(), rect.centerY());

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            FrameLayout layout = (FrameLayout) findViewById(R.id.fllyt_vol);
            layout.addView(mVolView, 0, params);
        }

        if (hasFocus && !mLoginSuccess_v) {
            // 在合适的位置弹出提示框
            int[] loc = new int[2];
            mRecordButton.getLocationInWindow(loc);
            mHintOffsetY = loc[1] - DensityUtil.dip2px(MixVerify3.this, 60);

            if (mRecordButtonPressed) {
                mPopupHint.setHint(getString(R.string.vocal_register_listening_hint));
            } else {
//                mPopupHint.setHint(getString(R.string.vocal_register_press_hint));
            }

            // 弹出按住说话提示消息
            mPopupHint.showAtLocation(mRecordButton, Gravity.TOP | Gravity.CENTER_HORIZONTAL,
                    0, mHintOffsetY);

            mToast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL,
                    0, mHintOffsetY + DensityUtil.dip2px(MixVerify3.this, 30));
        }

    }

    /**
     * 停止录音
     */
    private void stopRecording() {
        mWriteAudio = false;
        if (null != mPcmRecorder) {
            mPcmRecorder.stopRecord(true);
        }
        // 停止写入声纹数据
        mIdVerifier.stopWrite("ivp");

        setStopViewStatus();
    }

    private void setStopViewStatus() {
        if (null != mVolView) {
            mVolView.stopRecord();
            mVolView.clearAnimation();
        }
    }

    /**
     * 在8位数字密码中间加空格
     */
    private String getStyledPwdHint(String pwdHint) {
        String mTtsPwd = " ";
        for (int i = 0; i < pwdHint.length(); i++) {
            mTtsPwd = mTtsPwd + pwdHint.substring(i, i + 1) + " ";
        }
        pwdHint = mTtsPwd;
        return pwdHint;
    }

    /**
     * 人脸鉴别监听器，处理结果
     */
    private IdentityListener mSearchListener = new IdentityListener() {

        @Override
        public void onResult(IdentityResult result, boolean islast) {
            Log.d(TAG, result.getResultString());
            mVerifyStarted = false;

            if (null != mProDialog) {
                mProDialog.dismiss();
            }
            if (null == result) {
                return;
            }
            try {
                String resultStr_f = result.getResultString();
                JSONObject object = new JSONObject(resultStr_f);
                JSONObject ifv_result = object.getJSONObject("ifv_result");
                JSONArray candidates = ifv_result.getJSONArray("candidates");
                JSONObject obj = candidates.getJSONObject(0);

                int ret_f = object.getInt("ret");
                if (ErrorCode.SUCCESS != ret_f) {
                    showTip(getString(R.string.login_face_failure_hint));
                    return;
                } else {

                    if ((obj.optString("decision")).equals("accepted")) {
                        mLoginSuccess_f = true;
                        name_f = obj.optString("user");
                        score_f = obj.optDouble("score");
                        // 保存到历史记录中
                        SpeechApp.getmHisList().addHisItem(object.getString("group_id"),
                                object.getString("group_name") + "(" + object.getString("group_id") + ")");
                        SaveFuncUtil.saveObject(MixVerify3.this, SpeechApp.getmHisList(), SpeechApp.HIS_FILE_NAME);

//                        showTip(getString(R.string.login_face_success_hint));

                        if (mLoginSuccess_f == true && name_f.equals(name_v)) {
                            // 创建AlertDialog成功
                            setResultDialog(true, name_f, score_v, score_f);

//
                        } else {
                            // 创建AlertDialog失败
                            setResultDialog(false, name_f, score_v, score_f);
                        }
                    } else {
                        setResultDialog(false, name_f, score_v, score_f);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
        }

        @Override
        public void onError(SpeechError error) {
            mVerifyStarted = false;
            mLoginSuccess_v = false;

            if (null != mProDialog) {
                mProDialog.dismiss();
            }

            showTip(error.getPlainDescription(true));
        }

    };

    private void setResultDialog(Boolean decision, String name, Double score_v, Double score_f) {
        mBuider = new AlertDialog.Builder(MixVerify3.this);
        if (decision) {
            resultDialog = mBuider.setIcon(R.drawable.icon_succeed)
                    .setTitle("验证通过")
                    .setMessage(name + "请进" +
                            "\n声纹相似度：" + score_v +
                            "\n人脸相似度：" + score_f)
                    .create();
            resultDialog.show();
            mTtsFuncUtil.ttsFunction(MixVerify3.this, name_f + "请进");
        } else {
            resultDialog = mBuider.setIcon(R.drawable.icon_failed)
                    .setTitle("验证失败")
                    .setMessage("\n声纹相似度：" + score_v +
                            "\n人脸相似度：" + score_f)
                    .create();
            resultDialog.show();
            mTtsFuncUtil.ttsFunction(MixVerify3.this, "验证失败");
            mHandler.sendEmptyMessageDelayed(MSG_ACTIVITY_FINISH, 5000);
        }
        mHandler.sendEmptyMessageDelayed(MSG_RESULT_DISMISS, 5000);

    }


    /**
     * 处理消息，拍照或开始人脸识别
     */
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_FACE_START:
                    startFaceVerify();
                    break;
                case MSG_TAKE_PICTURE:
                    takePicture();
                    break;
                case MSG_PCM_START:
                    actionDown();
                    break;
                case MSG_PCM_STOP:
                    actionUp();
                    break;
                case MSG_RESULT_DISMISS:
                    resultDialog.dismiss();
                    break;
                case MSG_ACTIVITY_FINISH:
                    finish();
                default:
                    break;
            }
        }
    };

    /**
     * 开始拍照
     * 发起人脸验证
     */
    private void takePicture() {
        // 拍照，发起人脸注册
        try {
            if (mCamera != null && mCanTakePic) {
                Log.d(TAG, "takePicture");
                mCamera.takePicture(mShutterCallback, null, mPictureCallback);
                mCanTakePic = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //发起人脸验证
    private Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            Log.d(TAG, "onPictureTaken");
            if (!mIsPause) {
                mCameraHelper.setCacheData(data, mCameraId, MixVerify3.this);
                //发送消息 开始人脸识别
                mHandler.sendEmptyMessage(MSG_FACE_START);
            }
            mIsPreviewing = false;
            mCanTakePic = true;
        }
    };
    private Camera.ShutterCallback mShutterCallback = new Camera.ShutterCallback() {

        @Override
        public void onShutter() {

        }
    };

    /**
     * 开始人脸验证
     * 设置参数
     * 向子业务写入人脸数据
     */
    private void startFaceVerify() {
        Log.d(TAG, "startFaceVerify");

        byte[] imageData = mCameraHelper.getImageData();

        // 清空参数
        mIdVerifier.setParameter(SpeechConstant.PARAMS, null);
        // 设置业务场景
        mIdVerifier.setParameter(SpeechConstant.MFV_SCENES, "ifr");
        // 设置业务类型
        mIdVerifier.setParameter(SpeechConstant.MFV_SST, "identify");
        // 设置监听器，开始会话
        mIdVerifier.startWorking(mSearchListener);

        // 子业务执行参数，若无可以传空字符传
        StringBuffer params_f = new StringBuffer();

        params_f.append(",group_id=" + mGroupId + ",topc=1");

        // 向子业务写入数据，人脸数据可以一次写入
        mIdVerifier.writeData("ifr", params_f.toString(), imageData, 0, imageData.length);
        // 停止写入数据
        mIdVerifier.stopWrite("ifr");
    }


    private void showProDialog() {
        mProDialog.setMessage("验证中...");
        mProDialog.show();
    }

    private void openCamera() {
        if (null != mCamera) {
            return;
        }

        // 只有一个摄相头，打开后置
        if (Camera.getNumberOfCameras() == 1) {
            mCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
        }

        try {
            // 打开摄像头
            mCamera = Camera.open(mCameraId);
            mCamera.setDisplayOrientation(CameraHelper.getPreviewDegree(MixVerify3.this, mCameraId));
            mCamera.setParameters(mCameraHelper.getCameraParam(MixVerify3.this, mCamera, mCameraId));
            mPreviewSize = mCamera.getParameters().getPreviewSize();

            setSurfaceViewSize();

            // 设置用于显示拍照影像的SurfaceHolder对象
            mCamera.setPreviewDisplay(mPreviewSurface.getHolder());
            mCamera.startPreview(); // 开始预览
            mIsPreviewing = true;

            Log.d(TAG, "camera create");
        } catch (Exception e) {
            closeCamera();
            e.printStackTrace();
        }
    }

    private void closeCamera() {
        if (null != mCamera) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    private void setSurfaceViewSize() {
        Point fitSurfaceSize = mCameraHelper.getFitSurfaceSize(mPreviewSize);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(fitSurfaceSize.y, fitSurfaceSize.x);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        mPreviewSurface.setLayoutParams(params);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        openCamera();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        try {
            // 刷新屏幕横转参数变化，得放在catch里防止出现异常事件
            mCamera.setParameters(mCameraHelper.getCameraParam(MixVerify3.this,
                    mCamera, mCameraId));// 设置相机的参数
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        closeCamera();
    }

    @Override
    protected void onPause() {
        mIsPause = true;

//		closeCamera();

        // 关闭录音机
        if (null != mPcmRecorder) {
            mPcmRecorder.stopRecord(true);
        }

        mHandler.removeMessages(MSG_FACE_START);
        mHandler.removeMessages(MSG_TAKE_PICTURE);

        // 若已经开始验证，然后执行了onPause就表明Activity被其他应用中断
        if (mVerifyStarted) {
            mInterruptedByOtherApp = true;
        }

        mVerifyStarted = false;

        if (null != mIdVerifier) {
            mIdVerifier.cancel();
        }

        // 防止跳转到其他Activity后还显示前面的toast
        if (null != mToast) {
            mToast.cancel();
        }
        // 取消提示框
        if (null != mPopupHint) {
            mPopupHint.dismiss();
        }
        // 取消进度框
        if (null != mProDialog) {
            mProDialog.cancel();
        }

        setStopViewStatus();

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != mIdVerifier) {
            mIdVerifier.destroy();
            mIdVerifier = null;
        }
    }

    @Override
    public void finish() {
        if (null != mProDialog) {
            mProDialog.dismiss();
        }
        setResult(RESULT_OK);
        super.finish();
    }

    private void showTip(final String str) {
        mToast.setText(str);
        mToast.show();
        mTtsFuncUtil.ttsFunction(MixVerify3.this, str);
    }
}
