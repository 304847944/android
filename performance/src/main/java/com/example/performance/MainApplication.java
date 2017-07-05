package com.example.performance;

import com.example.performance.util.Utils;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

/**
 * Created by ouyangshen on 2016/12/27.
 */
public class MainApplication extends Application {
	private static final String TAG = "MainApplication";
	private static MainApplication mApp;
	private LockScreenReceiver mReceiver;
	private String mChange = "";

	public static MainApplication getInstance() {
		return mApp;
	}

	public String getChangeDesc() {
		return mApp.mChange;
	}

	public void setChangeDesc(String change) {
		mApp.mChange = mApp.mChange + change;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mApp = this;
		mReceiver = new LockScreenReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_SCREEN_ON);
		filter.addAction(Intent.ACTION_SCREEN_OFF);
		filter.addAction(Intent.ACTION_USER_PRESENT);
		registerReceiver(mReceiver, filter);
	}

	private class LockScreenReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent != null) {
				String change = "";
				change = String.format("%s\n%s : 收到广播：%s", change,
						Utils.getNowTime(), intent.getAction());
				if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
					change = String.format("%s\n这是屏幕点亮事件，可在此开启日常操作", change);
				} else if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
					change = String.format("%s\n这是屏幕关闭事件，可在此暂停耗电操作", change);
				} else if (intent.getAction()
						.equals(Intent.ACTION_USER_PRESENT)) {
					change = String.format("%s\n这是用户解锁事件", change);
				}
				Log.d(TAG, change);
				MainApplication.getInstance().setChangeDesc(change);
			}
		}
	}

}
