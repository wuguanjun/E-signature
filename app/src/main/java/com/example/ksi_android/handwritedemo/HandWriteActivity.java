package com.example.ksi_android.handwritedemo;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import butterknife.Bind;
import butterknife.ButterKnife;

public class HandWriteActivity extends AppCompatActivity {

    @Bind(R.id.view)
    LinePathView view;
    @Bind(R.id.clear1)
    Button mclear;
    @Bind(R.id.ll)
    LinearLayout ll;
    @Bind(R.id.save1)
    Button msave;
    @Bind(R.id.change)
    Button changecolor;
    @Bind(R.id.changewidth)
    Button changewidth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hand_write);
        ButterKnife.bind(this);
        setResult(50);

        msave.setOnClickListener(v -> {
            if (view.getTouched()) {
                view.save(MainActivity.path, true, 10, true, 0.9);
                setResult(100);
                finish();
            } else {

                Toast.makeText(HandWriteActivity.this, "您没有签名~", Toast.LENGTH_SHORT).show();
            }

        });
        mclear.setOnClickListener(v -> view.clear());
        changecolor.setOnClickListener(v->{view.setmBackColor(Color.RED);view.setmPenColor(Color.WHITE);view.clear();});
        changewidth.setOnClickListener(v->{view.setmPaintWidth(20);view.clear();});
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
    }
}
