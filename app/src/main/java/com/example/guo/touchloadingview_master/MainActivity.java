package com.example.guo.touchloadingview_master;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TouchLoadingView touchView = (TouchLoadingView) findViewById(R.id.touchLoadingView);
        touchView.setOnPressCompletedListener(new TouchLoadingView.OnPressCompletedListener() {
            @Override
            public void onPressCompleted() {
                Toast.makeText(MainActivity.this, "结束", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
