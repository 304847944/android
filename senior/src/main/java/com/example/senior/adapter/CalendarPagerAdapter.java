package com.example.senior.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.example.senior.calendar.Constant;
import com.example.senior.fragment.CalendarFragment;

public class CalendarPagerAdapter extends FragmentStatePagerAdapter {

	public CalendarPagerAdapter(FragmentManager fm) {
		super(fm);
	}

	public int getCount() {
		return 12;
	}

	public Fragment getItem(int position) {
		return CalendarFragment.newInstance(position + 1);
	}

	@Override
	public CharSequence getPageTitle(int position) {
		return new String(Constant.xuhaoArray[position + 1] + "月");
	}

}
