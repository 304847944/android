package com.example.senior;

import java.util.HashMap;

import android.app.Application;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.util.Log;

/**
 * Created by ouyangshen on 2016/10/7.
 */
public class MainApplication extends Application {

	private final static String TAG = "MainApplication";
	private static MainApplication mApp;
	public HashMap<String, String> mInfoMap = new HashMap<String, String>();
	
	public static MainApplication getInstance() {
		return mApp;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mApp = this;
		Log.d(TAG, "onCreate");
	}
	
	@Override
	public void onTerminate() {
		Log.d(TAG, "onTerminate");
		super.onTerminate();
	}
	
	public HashMap<Long, Bitmap> mIconMap = new HashMap<Long, Bitmap>();
	
}
