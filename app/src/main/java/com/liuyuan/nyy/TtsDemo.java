package com.liuyuan.nyy;

import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.SynthesizerListener;
import com.iflytek.sunflower.FlowerCollector;
import com.liuyuan.nyy.settings.TtsSettings;

public class TtsDemo extends AppCompatActivity implements View.OnClickListener {
    private EditText tts_text;
    private static String TAG = TtsDemo.class.getSimpleName();
    private SpeechSynthesizer mTts;


    // 默认发音人
    private String voicer = "xiaoyan";

    private String[] mCloudVoicersEntries;
    private String[] mCloudVoicersValue;

    // 缓冲进度
    private int mPercentForBuffering = 0;
    // 播放进度
    private int mPercentForPlaying = 0;

    private Toast mToast;
//    private SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tts_demo);
//        SpeechUtility.createUtility(TtsDemo.this, "appid=" + getString(R.string.app_id));

        initLayout();
        // 初始化合成对象
        mTts = SpeechSynthesizer.createSynthesizer(TtsDemo.this, mTtsInitListener);

        mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);



    }

    private void initLayout() {
        findViewById(R.id.image_tts_set).setOnClickListener(TtsDemo.this);
        findViewById(R.id.tts_play).setOnClickListener(TtsDemo.this);

        tts_text = (EditText) findViewById(R.id.tts_text);
        ((EditText) findViewById(R.id.tts_text)).setText("您好，请说出口令");
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.image_tts_set:
                Intent intent = new Intent(TtsDemo.this, TtsSettings.class);
                startActivity(intent);
                break;
            case R.id.tts_play:
                FlowerCollector.onEvent(TtsDemo.this, "tts_play");
                String text = ((EditText) findViewById(R.id.tts_text)).getText().toString();
                setParam();
                mTts.startSpeaking(text,mTtsListener);
                int code = mTts.startSpeaking(text, mTtsListener);
                if (code != ErrorCode.SUCCESS)
                    showTip("语音合成失败，错误码：" + code);
                break;
            default:
                break;
        }

    }

//    private int selectedNum = 0;
    /**
     * 初始化监听。
     */
    private InitListener mTtsInitListener = new InitListener() {
        @Override
        public void onInit(int code) {
            Log.d(TAG, "InitListener init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
                showTip("初始化失败,错误码：" + code);
            } else {
                // 初始化成功，之后可以调用startSpeaking方法
                // 注：有的开发者在onCreate方法中创建完合成对象之后马上就调用startSpeaking进行合成，
                // 正确的做法是将onCreate中的startSpeaking调用移至这里
            }
        }
    };

    /**
     * 合成回调监听。
     */
    private SynthesizerListener mTtsListener = new SynthesizerListener() {

        @Override
        public void onSpeakBegin() {
            showTip("开始播放");
        }

        @Override
        public void onSpeakPaused() {
            showTip("暂停播放");
        }

        @Override
        public void onSpeakResumed() {
            showTip("继续播放");
        }

        @Override
        public void onBufferProgress(int percent, int beginPos, int endPos,
                                     String info) {
            // 合成进度
            mPercentForBuffering = percent;
            showTip(String.format(getString(R.string.tts_toast_format),
                    mPercentForBuffering, mPercentForPlaying));
        }

        @Override
        public void onSpeakProgress(int percent, int beginPos, int endPos) {
            // 播放进度
            mPercentForPlaying = percent;
            showTip(String.format(getString(R.string.tts_toast_format),
                    mPercentForBuffering, mPercentForPlaying));
        }

        @Override
        public void onCompleted(SpeechError error) {
            if (error == null) {
                showTip("播放完成");
            } else if (error != null) {
                showTip(error.getPlainDescription(true));
            }
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
            // 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
            // 若使用本地能力，会话id为null
            //	if (SpeechEvent.EVENT_SESSION_ID == eventType) {
            //		String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
            //		Log.d(TAG, "session id =" + sid);
            //	}
        }
    };

    private void showTip(final String str) {
        mToast.setText(str);
        mToast.show();
    }


    private void setParam() {
        // 清空参数
        mTts.setParameter(SpeechConstant.PARAMS, null);


        mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
        // 设置在线合成发音人
        mTts.setParameter(SpeechConstant.VOICE_NAME, voicer);
        //设置合成语速
        mTts.setParameter(SpeechConstant.SPEED,  "50");
        //设置合成音调
        mTts.setParameter(SpeechConstant.PITCH, "50");
        //设置合成音量
        mTts.setParameter(SpeechConstant.VOLUME, "50");

        //设置播放器音频流类型
        mTts.setParameter(SpeechConstant.STREAM_TYPE, "3");
        // 设置播放合成音频打断音乐播放，默认为true
        mTts.setParameter(SpeechConstant.KEY_REQUEST_FOCUS, "true");

        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        // 注：AUDIO_FORMAT参数语记需要更新版本才能生效
        mTts.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
        mTts.setParameter(SpeechConstant.TTS_AUDIO_PATH, Environment.getExternalStorageDirectory() + "/msc/tts.wav");
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mTts.stopSpeaking();
        // 退出时释放连接
        mTts.destroy();
    }

    @Override
    protected void onResume() {
        //移动数据统计分析
        FlowerCollector.onResume(TtsDemo.this);
        FlowerCollector.onPageStart(TAG);
        super.onResume();
    }
    @Override
    protected void onPause() {
        //移动数据统计分析
        FlowerCollector.onPageEnd(TAG);
        FlowerCollector.onPause(TtsDemo.this);
        super.onPause();
    }
}
