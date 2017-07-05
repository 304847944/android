package com.example.helloworld;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //获取名字为tv_hello的TextView控件
        TextView tv_hello = (TextView) findViewById(R.id.tv_hello);
        //给TextView控件设置文字内容
        tv_hello.setText("今天天气真热啊，火辣辣的");
        //给TextView控件设置文字颜色
        tv_hello.setTextColor(Color.RED);
        //给TextView控件设置文字大小
        tv_hello.setTextSize(30);
    }
}
