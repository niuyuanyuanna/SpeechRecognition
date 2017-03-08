package com.liuyuan.nyy;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.Toast;

import com.liuyuan.nyy.entity.GroupHisList;
import com.liuyuan.nyy.regist.GroupManage;
import com.liuyuan.nyy.ui.DropEditText;
import com.liuyuan.nyy.util.SaveFuncUtil;

public class GroupIdentify extends AppCompatActivity implements View.OnClickListener{

    private DropEditText mGroupIDDrop;
    private Toast mToast;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_identify);

        initUi();
        mToast = Toast.makeText(GroupIdentify.this,"",Toast.LENGTH_SHORT);
    }

    private void initUi() {
        //从本地读出历史纪录
        SpeechApp.setmHisList((GroupHisList) SaveFuncUtil.readObject(this,SpeechApp.HIS_FILE_NAME));
        mGroupIDDrop = (DropEditText) findViewById(R.id.drop_edit);
        findViewById(R.id.btnStart).setOnClickListener(this);
    }


    @Override
    protected void onResume() {
        mGroupIDDrop.setStringList(GroupIdentify.this,SpeechApp.getmHisList().getGroupInfo_list(),
                SpeechApp.getmHisList().getGroup_list());
        BaseAdapter adp = mGroupIDDrop.getAdapter();
        if(adp != null)
            adp.notifyDataSetChanged();
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnStart:
                String group_id_v =mGroupIDDrop.getText();
                if(TextUtils.isEmpty(group_id_v))
                {
                    mGroupIDDrop.requestFocus();
                    showTip(getString(R.string.groupid_null));
                    return;
                }
                Intent intent = new Intent(GroupIdentify.this,MixVerify.class);
                intent.putExtra("group_id",group_id_v);
                startActivity(intent);
                break;
        }

    }

    private void showTip(String string) {
    }
}
