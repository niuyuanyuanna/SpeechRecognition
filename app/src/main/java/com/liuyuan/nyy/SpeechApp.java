package com.liuyuan.nyy;

import android.app.Application;

import com.iflytek.cloud.SpeechUtility;
import com.liuyuan.nyy.entity.GroupHisList;
import com.liuyuan.nyy.entity.User;


public class SpeechApp extends Application {
	public static String mAuth_id;
	private static String mPassword = "1234";
	public static int failTime = 0;
	private static String group_id = "1955034676";
	private static String group_name = "实验室";

	private static User mUser;
	public static final String HIS_FILE_NAME ="HistoryFile";
	private static GroupHisList mHisList ;

	@Override
	public void onCreate() {
		// 应用程序入口处调用，避免手机内存过小，杀死后台进程后通过历史intent进入Activity造成SpeechUtility对象为null
		// 如在Application中调用初始化，需要在Mainifest中注册该Applicaiton
		// 注意：此接口在非主进程调用会返回null对象，如需在非主进程使用语音功能，请增加参数：SpeechConstant.FORCE_LOGIN+"=true"
		// 参数间使用半角“,”分隔。
		// 设置你申请的应用appid,请勿在'='与appid之间添加空格及空转义符
		
		// 注意： appid 必须和下载的SDK保持一致，否则会出现10407错误
		
		SpeechUtility.createUtility(SpeechApp.this, "appid=" + getString(R.string.app_id));
			
		// 以下语句用于设置日志开关（默认开启），设置成false时关闭语音云SDK日志打印
		// Setting.setShowLog(false);
		super.onCreate();
	}

	public static User getHostUser() {
		if (null == mUser) {
			mUser = new User();
		}
		return mUser;
	}
	public static void setHostUser(User user) {
		mUser = user;
	}

	public static GroupHisList getmHisList() {
		if (null == mHisList) {
			mHisList = new GroupHisList();
		}
		return mHisList;
	}

	public static void setmHisList(GroupHisList mHisList) {
		SpeechApp.mHisList = mHisList;
	}

	public static void setmPassword(String password){
		SpeechApp.mPassword = password;
	}
	public static String getmPassword(){
		return mPassword;
	}

	public static void setGroup_id(String group_id) {
		SpeechApp.group_id = group_id;
	}

	public static String getGroup_id() {
		return group_id;
	}

	public static void setGroup_name(String group_name) {
		SpeechApp.group_name = group_name;
	}

	public static String getGroup_name() {
		return group_name;
	}
}
