package com.example.panpan.amazingdrum.activity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.AnimationUtils;

import com.example.panpan.amazingdrum.R;

public class SplashActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
    }

    private Handler handler = new Handler();
    boolean first = false;
    Runnable uiRunnable = new Runnable() {
        @Override
        public void run() {
            BluetoothAdapter adapter = ((BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();
            findViewById(R.id.imageView).startAnimation(AnimationUtils.loadAnimation(SplashActivity.this, R.anim.splash_image_anim));
            {
                adapter.enable();
                first = true;
            }
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    findViewById(R.id.imageView2).startAnimation(AnimationUtils.loadAnimation(SplashActivity.this, R.anim.splash_image_anim));
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            startActivity(new Intent(SplashActivity.this, MainBandActivity.class));
                            finish();
                        }
                    }, 1000);
                }
            }, 500);
        }
    };

    @Override
    protected void onPause() {
        handler.removeCallbacks(uiRunnable);
        super.onPause();
    }

    @Override
    protected void onResume() {
        handler.postDelayed(uiRunnable, 200);
        super.onResume();
    }
}
