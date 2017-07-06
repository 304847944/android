package com.example.thirdsdk;

import com.example.thirdsdk.util.CacheUtil;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RatingBar;
import android.widget.RatingBar.OnRatingBarChangeListener;
import android.widget.TextView;

/**
 * Created by ouyangshen on 2016/12/18.
 */
public class RatingBarActivity extends AppCompatActivity implements
		OnCheckedChangeListener, OnRatingBarChangeListener {
	private CheckBox ck_whole;
	private RatingBar rb_score;
	private TextView tv_rating;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_rating_bar);
		ck_whole = (CheckBox) findViewById(R.id.ck_whole);
		rb_score = (RatingBar) findViewById(R.id.rb_score);
		tv_rating = (TextView) findViewById(R.id.tv_rating);
		ck_whole.setOnCheckedChangeListener(this);
		rb_score.setIsIndicator(false);
		rb_score.setNumStars(5);
		rb_score.setRating(3);
		rb_score.setStepSize(1);
		rb_score.setOnRatingBarChangeListener(this);
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (buttonView.getId() == R.id.ck_whole) {
			rb_score.setStepSize(ck_whole.isChecked()?1:rb_score.getNumStars()/10.0f);
		}
	}

	@Override
	public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
		String desc = String.format("当前选中的是%s颗星", CacheUtil.formatDecimal(rating,1));
		tv_rating.setText(desc);
	}

}
