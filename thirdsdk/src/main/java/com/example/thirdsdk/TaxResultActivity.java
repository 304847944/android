package com.example.thirdsdk;

import com.example.thirdsdk.adapter.ShareGridAdapter;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.RatingBar;
import android.widget.Toast;
import android.widget.RatingBar.OnRatingBarChangeListener;

/**
 * Created by ouyangshen on 2016/12/18.
 */
public class TaxResultActivity extends AppCompatActivity implements 
		OnClickListener, OnRatingBarChangeListener {
	private final static String TAG = "TaxResultActivity";
	private RatingBar rb_tax_score;
	private Button btn_tax_score;
	private GridView gv_share_channel;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tax_result);
		rb_tax_score = (RatingBar) findViewById(R.id.rb_tax_score);
		btn_tax_score = (Button) findViewById(R.id.btn_tax_score);
		gv_share_channel = (GridView) findViewById(R.id.gv_share_channel);
		rb_tax_score.setOnRatingBarChangeListener(this);
		btn_tax_score.setOnClickListener(this);
		initShareChannel();
	}

	@Override
	public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
	}
	
	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.btn_tax_score) {
			String desc = String.format("您的评分为%d颗星，感谢您的评价", (int)rb_tax_score.getRating());
			Toast.makeText(this, desc, Toast.LENGTH_SHORT).show();
			btn_tax_score.setText("已评价");
			btn_tax_score.setTextColor(getResources().getColor(R.color.dark_grey));
			btn_tax_score.setEnabled(false);
		}
	}
	
	private Handler mHandler = new Handler();
	private void initShareChannel() {
		String url = "http://blog.csdn.net/aqi00";
		String title = "我用咚咚打车啦";
		String content = "你也来打打车，方便快捷真省心。";
		String imgage_url = "http://avatar.csdn.net/C/1/5/1_aqi00.jpg";
		ShareGridAdapter adapter = new ShareGridAdapter(this, mHandler, url,
				title, content, imgage_url, null);
		gv_share_channel.setAdapter(adapter);
		gv_share_channel.setOnItemClickListener(adapter);
	}

}
