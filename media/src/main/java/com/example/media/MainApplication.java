package com.example.media;

import android.app.Application;
import android.media.MediaPlayer;
import android.util.Log;

public class MainApplication extends Application {
	private static final String TAG = "MainApplication";
	private static MainApplication mApp;
	public MediaPlayer mMediaPlayer;
	public String mSong;
	public String mFilePath;

	public static MainApplication getInstance() {
		return mApp;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mApp = this;
		mMediaPlayer = new MediaPlayer();
	}
	
}
