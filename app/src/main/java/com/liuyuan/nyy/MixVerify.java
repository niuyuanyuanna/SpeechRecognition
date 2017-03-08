package com.liuyuan.nyy;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
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
import com.liuyuan.nyy.entity.User;
import com.liuyuan.nyy.ui.HintPopupWindow;
import com.liuyuan.nyy.ui.RecordView;
import com.liuyuan.nyy.util.CameraHelper;
import com.liuyuan.nyy.util.DensityUtil;
import com.liuyuan.nyy.util.SaveFuncUtil;

import org.json.JSONException;
import org.json.JSONObject;

public class MixVerify extends AppCompatActivity implements View.OnClickListener, SurfaceHolder.Callback {
    private static final String TAG = MixVerify.class.getSimpleName();

    //用户信息
//    private User mUser;

    //是否登陆成功
    private boolean mLoginSuccess_v = false;
    private boolean mLoginSuccess_f = false;

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
    private TextView txt_title;
    private ImageButton mFlashSwitchButton;
    private ImageButton mChangeCameraButton;
    private ProgressDialog mProDialog;
    private RecordView mVolView;
    private ImageButton mRecordButton;
    private boolean mRecordButtonPressed;
    private com.liuyuan.nyy.ui.HintPopupWindow mPopupHint;
    //提示框显示位置的纵坐标
    private int mHintOffsetY;

    // 用户输入的组ID
    private String mGroupId;

    //用户id，唯一标识
    private String authid;
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
    private final int DELAY_TIME = 300;
    // 在松开麦克风之前是否已经出现错误
    private boolean mErrorOccurBeforeUp = false;
    // 上次有效点击快门的时间，用于防止用户频繁点击快门
    private long mLastValidShutterClickTime = 0;

    private static final int MSG_FACE_START = 1;
    private static final int MSG_TAKE_PICTURE = 2;

    private Toast mToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mix_verify);

        mGroupId = getIntent().getStringExtra("group_id");
        txt_title.setText("组ID：" + mGroupId);
//        mUser = new User();
//        authid = SpeechApp.mAuth_id;
        mCameraHelper = CameraHelper.createHelper(MixVerify.this);

        initUi();

        mIdVerifier = IdentityVerifier.createVerifier(getApplicationContext(), new InitListener() {
            @Override
            public void onInit(int i) {
                if (ErrorCode.SUCCESS == i)
                    showTip("引擎初始化成功");
                else
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

        mHintTextView = (TextView) findViewById(R.id.txt_hint);
        mPwdTextView = (TextView) findViewById(R.id.txt_num);
        mFlashSwitchButton = (ImageButton) findViewById(R.id.btn_flash_switch);
        mChangeCameraButton = (ImageButton) findViewById(R.id.btn_change_camera);
        mRecordButton = (ImageButton) findViewById(R.id.btn_record);

        mProDialog = new ProgressDialog(this);
        mProDialog.setCancelable(true);
        mProDialog.setCanceledOnTouchOutside(false);
        mProDialog.setTitle("请稍后");
        mProDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                mIdVerifier.cancel();
                mVerifyStarted = false;
                mPopupHint.setHint("请按住麦克风说话");
            }
        });

        mChangeCameraButton.setOnClickListener(this);
        mFlashSwitchButton.setOnClickListener(this);
        mToast = Toast.makeText(MixVerify.this, "", Toast.LENGTH_SHORT);

        //设置麦克风touch事件监听器
        mRecordButton.setOnClickListener((View.OnClickListener) mRecordButtonOnTouchListener);

        mPopupHint = new HintPopupWindow(MixVerify.this);
        mPopupHint.setHint(getString(R.string.vocal_register_press_hint));

        mVolView = new RecordView(MixVerify.this);

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

    /**
     * 人脸鉴别监听器
     */
    private IdentityListener mSearchListener = new IdentityListener() {

        @Override
        public void onResult(IdentityResult result, boolean islast) {
            Log.d(TAG, result.getResultString());
            if (null != mProDialog)
                mProDialog.dismiss();

            try {
                String resultStr_f = result.getResultString();
                JSONObject object = new JSONObject(result.getResultString());
                int ret_f = object.getInt("ret");

                if (ErrorCode.SUCCESS != ret_f) {
                    showTip("鉴别失败");
                    return;
                } else {
                    mLoginSuccess_f = true;

                        showTip(getString(R.string.login_success_hint));
                        // 隐藏提示框
                        mPopupHint.dismiss();

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
            mProDialog.dismiss();

            showTip(error.getPlainDescription(true));
        }

    };

    /**
     * 语音判别监听器，识别通过与否，返回评分
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

            try {
                String resultStr_v = result.getResultString();
                JSONObject object = new JSONObject(result.getResultString());
                int ret_v = object.getInt("ret");

                if (ErrorCode.SUCCESS != ret_v) {
                    showTip(getString(R.string.login_failure_hint));
                    return;
                } else {
                    mLoginSuccess_v = true;
                    if (mLoginSuccess_v == true && mLoginSuccess_f == true) {
                        // 保存到历史记录中
                        SpeechApp.getmHisList().addHisItem(object.getString("group_id"),
                                object.getString("group_name") + "(" + object.getString("group_id") + ")");
                        SaveFuncUtil.saveObject(MixVerify.this, SpeechApp.getmHisList(), SpeechApp.HIS_FILE_NAME);

                        // 跳转到结果展示页面
                        Intent intent = new Intent(MixVerify.this, ResultIdentifyActivity.class);
                        intent.putExtra("result_v", resultStr_v);
                        startActivity(intent);
                        finish();

                        showTip(getString(R.string.login_success_hint));
                        // 隐藏提示框
                        mPopupHint.dismiss();
                    } else
                        showTip("人脸识别失败，声纹识别成功");
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

        if (hasFocus && !mLoginSuccess_f && !mLoginSuccess_v) {
            // 在合适的位置弹出提示框
            int[] loc = new int[2];
            mRecordButton.getLocationInWindow(loc);
            mHintOffsetY = loc[1] - DensityUtil.dip2px(MixVerify.this, 60);

            if (mRecordButtonPressed) {
                mPopupHint.setHint(getString(R.string.vocal_register_listening_hint));
            } else {
                mPopupHint.setHint(getString(R.string.vocal_register_press_hint));
            }

            // 弹出按住说话提示消息
            mPopupHint.showAtLocation(mRecordButton, Gravity.TOP | Gravity.CENTER_HORIZONTAL,
                    0, mHintOffsetY);

            mToast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL,
                    0, mHintOffsetY + DensityUtil.dip2px(MixVerify.this, 30));
        }

    }


    // 录音按钮Touch事件监听器
    private OnTouchListener mRecordButtonOnTouchListener = new OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mRecordButtonPressed = true;
                    // 按下事件
                    if (!mVerifyStarted) {
                        if (isFrequestlyClick()) {
                            // 频繁无效点击，则不处理
                            return false;
                        }
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
                        return false;
                    }
                    mMoveOutofBound = false;
                    mErrorOccurBeforeUp = false;
                    mVolView.startRecording();
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (!mVerifyStarted) {
                        return false;
                    }

                    int x = (int) event.getX();
                    int y = (int) event.getY();
                    Rect rect = new Rect();
                    v.getLocalVisibleRect(rect);
                    if (!rect.contains(x, y)) {
                        // 按住后手指move出控件范围
                        if (!mMoveOutofBound) {
                            mMoveOutofBound = true;
                            // 停止录音，开始拍照，随后开始人脸验证
                            stopRecording();
                            if (!mErrorOccurBeforeUp) {
                                if (mPopupHint.isShowing()) {
                                    mPopupHint.dismiss();
                                }
                                mHandler.sendEmptyMessageDelayed(MSG_TAKE_PICTURE, DELAY_TIME);
                                showProDialog();
                            }
                        }
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    mRecordButtonPressed = false;
                    v.performClick();

                    // 停止录音，开始拍照，随后开始人脸验证
                    stopRecording();

                    if (!mVerifyStarted) {
                        mPopupHint.setHint(getString(R.string.vocal_register_press_hint));
                        return false;
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
                    break;

                default:
                    break;
            }

            return false;
        }
    };

    /**
     * 判断是否为频繁点击，Touch事件监听器中case MotionEvent.ACTION_UP判断
     */
    private boolean isFrequestlyClick() {
        long clickTime = System.currentTimeMillis();
        if (clickTime - mLastValidShutterClickTime < 200) {
            return true;
        }
        mLastValidShutterClickTime = clickTime;

        return false;
    }

    /**
     * 停止录音，Touch事件监听器中case MotionEvent.ACTION_UP处理
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
     * 开始声纹验证，Touch事件监听器中case MotionEvent.ACTION_DOWN处理
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
//        // 用户id
//        mIdVerifier.setParameter(SpeechConstant.AUTH_ID, authid);
        // 设置监听器，开始会话
        mIdVerifier.startWorking(mVerifyListener);
    }

    /**
     * 在8位数字密码中间加空格
     */
    private String getStyledPwdHint(String pwdHint) {
        return pwdHint.substring(0, 4) + " " + pwdHint.substring(4, pwdHint.length());
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
                default:
                    break;
            }
        }
    };

    /**
     * 开始人脸验证，mHandler中case MSG_FACE_START处理
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
        params_f.append(",group_id=" + mGroupId +",topc=3");
        // 向子业务写入数据，人脸数据可以一次写入
        mIdVerifier.writeData("ifr", params_f.toString(), imageData, 0, imageData.length);
        // 停止写入数据
        mIdVerifier.stopWrite("ifr");
    }

    /**
     * 开始拍照，mHandler中case MSG_TAKE_PICTURE处理
     * 发起人脸注册
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

    private Camera.ShutterCallback mShutterCallback = new Camera.ShutterCallback() {

        @Override
        public void onShutter() {

        }
    };
    private Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            Log.d(TAG, "onPictureTaken");
            if (!mIsPause) {
                mCameraHelper.setCacheData(data, mCameraId, MixVerify.this);
                //发送消息 开始人脸识别
                mHandler.sendEmptyMessage(MSG_FACE_START);
            }
            mIsPreviewing = false;
            mCanTakePic = true;
        }
    };

    private void showProDialog() {
        mProDialog.setMessage("登录中...");
        mProDialog.show();
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
        mPopupHint.setHint(getString(R.string.vocal_register_press_hint));

        mCanTakePic = true;
        if (mCamera != null) {
            mCamera.startPreview();
            mIsPreviewing = true;
        }

        if (mInterruptedByOtherApp) {
            showTip(getString(R.string.login_hint_interrupted));
            mInterruptedByOtherApp = false;
        }
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
            mCamera.setDisplayOrientation(CameraHelper.getPreviewDegree(MixVerify.this, mCameraId));
            mCamera.setParameters(mCameraHelper.getCameraParam(MixVerify.this, mCamera, mCameraId));
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
                params.append("ptxt=" + mVerifyNumPwd + ",")
                        .append("pwdt=" + PWD_TYPE_NUM + ",")
                        .append("vad_eos=" + VAD_EOS);

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

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        openCamera();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
        try {
            // 刷新屏幕横转参数变化，得放在catch里防止出现异常事件
            mCamera.setParameters(mCameraHelper.getCameraParam(MixVerify.this,
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
    }

    @Override
    public void finish() {
        if (null != mProDialog) {
            mProDialog.dismiss();
        }
        setResult(RESULT_OK);
        super.finish();
    }

    private void showTip(String str) {
        mToast.setText(str);
        mToast.show();
    }
}
