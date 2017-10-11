package com.example.panpan.amazingdrum.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import com.example.panpan.amazingdrum.JoinThread;
import com.example.panpan.amazingdrum.R;
import com.example.panpan.amazingdrum.Sound;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class PlayActivity extends Activity {
    public static JoinThread joinThread;
    @InjectView(R.id.play_button_1)
    Button playButton1;
    @InjectView(R.id.play_button_2)
    Button playButton2;
    @InjectView(R.id.play_button_3)
    Button playButton3;
    @InjectView(R.id.play_button_4)
    Button playButton4;
    @InjectView(R.id.play_button_5)
    Button playButton5;
    @InjectView(R.id.play_button_6)
    Button playButton6;
    @InjectView(R.id.play_button_7)
    Button playButton7;
    @InjectView(R.id.play_button_8)
    Button playButton8;
    @InjectView(R.id.play_button_9)
    Button playButton9;
    private byte[] sendData = new byte[8];
    private Sound sound;

    @SuppressWarnings("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        ButterKnife.inject(this);
        if (joinThread != null)
            joinThread.setListener(joinListener);
        playButton1.setOnTouchListener(onPlayTouchListener);
        playButton2.setOnTouchListener(onPlayTouchListener);
        playButton3.setOnTouchListener(onPlayTouchListener);
        playButton4.setOnTouchListener(onPlayTouchListener);
        playButton5.setOnTouchListener(onPlayTouchListener);
        playButton6.setOnTouchListener(onPlayTouchListener);
        playButton7.setOnTouchListener(onPlayTouchListener);
        playButton8.setOnTouchListener(onPlayTouchListener);
        playButton9.setOnTouchListener(onPlayTouchListener);
        playButton1.setId(0);
        playButton2.setId(1);
        playButton3.setId(2);
        playButton4.setId(3);
        playButton5.setId(4);
        playButton6.setId(5);
        playButton7.setId(6);
        playButton8.setId(7);
        playButton9.setId(8);
        sound=new Sound();
        sound.init(this);
    }

    private JoinThread.OnJoinListener joinListener = new JoinThread.OnJoinListener() {
        @Override
        public void OnStateChanged(JoinThread thread, JoinThread.State state) {

        }

        @Override
        public void OnDataReceived(JoinThread thread, byte[] data, int length) {

        }
    };

    @Override
    protected void onDestroy() {
        if (joinThread != null)
            joinThread.close();
        joinThread = null;
        sound.release();
        super.onDestroy();
    }

    private View.OnTouchListener onPlayTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                int id = v.getId();
                sendData[0] = (byte) 0x02;
                sendData[1] = (byte) id;
                if (joinThread != null)
                    joinThread.write(sendData);
                sound.play(id%4);
            }
            return false;
        }
    };
}
