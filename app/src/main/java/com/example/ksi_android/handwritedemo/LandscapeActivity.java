package com.example.ksi_android.handwritedemo;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import butterknife.Bind;
import butterknife.ButterKnife;

public class LandscapeActivity extends Activity {
    @Bind(R.id.view)
    LinePathView view;
    @Bind(R.id.clear1)
    Button mclear;
    @Bind(R.id.save1)
    Button msave;
    @Bind(R.id.ll)
    LinearLayout ll;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_hand_write);
        ButterKnife.bind(this);
        setResult(50);
        msave.setOnClickListener(v -> {
            if (view.getTouched())
            {
                view.save(MainActivity.path1,false,10,false,0.9);
                setResult(101);
                finish();
            }else
            {
                Toast.makeText(LandscapeActivity.this,"您没有签名~",Toast.LENGTH_SHORT).show();
            }
        });
        mclear.setOnClickListener(v -> view.clear());
    }



}
