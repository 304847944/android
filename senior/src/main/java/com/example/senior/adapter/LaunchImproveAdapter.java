package com.example.senior.adapter;

import java.util.ArrayList;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.example.senior.fragment.LaunchFragment;

public class LaunchImproveAdapter extends FragmentStatePagerAdapter {

	private ArrayList<Integer> mImageList = new ArrayList<Integer>();
	
	public LaunchImproveAdapter(FragmentManager fm, int[] imageArray) {
		super(fm);
		for (int i=0; i<imageArray.length; i++) {
			mImageList.add(imageArray[i]);
		}
	}

	public int getCount() {
		return mImageList.size();
	}

	public Fragment getItem(int position) {
		return LaunchFragment.newInstance(position, mImageList.get(position));
	}

}
