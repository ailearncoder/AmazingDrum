package com.example.panpan.amazingdrum.activity;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import com.example.panpan.amazingdrum.R;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class JoinActivity extends Activity {

    @InjectView(R.id.edit_room)
    EditText editRoom;
    @InjectView(R.id.button_join)
    Button buttonJoin;
    @InjectView(R.id.edit_name)
    EditText editName;
    private Socket socket;
    private InputStream inputStream;
    private OutputStream outputStream;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);
        ButterKnife.inject(this);
    }

    @OnClick(R.id.button_join)
    public void onViewClicked() {
        if (buttonJoin.getText().toString().equals("加入"))
            join();
        if (buttonJoin.getText().toString().equals("退出")) {
            close();
        }
    }

    private void join() {
        buttonJoin.setEnabled(false);
        buttonJoin.setText("正在加入...");
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                String host = editRoom.getText().toString();
                try {
                    socket = new Socket(host, 8888);
                    inputStream = socket.getInputStream();
                    outputStream = socket.getOutputStream();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            buttonJoin.setEnabled(true);
                            buttonJoin.setText("退出");
                        }
                    });
                    byte name[]=editName.getText().toString().getBytes("UTF-8");
                    byte name2[]=new byte[name.length+1];
                    System.arraycopy(name,0,name2,1,name.length);
                    name2[0]=0;
                    outputStream.write(name2);
                    while (true) {
                        inputStream.read();
                    }
                } catch (Exception e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            buttonJoin.setEnabled(true);
                            buttonJoin.setText("加入");
                        }
                    });
                }
            }
        });
        thread.start();
    }

    private void close() {
        buttonJoin.setEnabled(false);
        buttonJoin.setText("正在退出...");
        if (inputStream != null)
            try {
                inputStream.close();
            } catch (IOException e) {
                //e.printStackTrace();
            }
        if (outputStream != null)
            try {
                outputStream.close();
            } catch (IOException e) {
                //e.printStackTrace();
            }
        if (socket != null)
            try {
                socket.close();
            } catch (IOException e) {
                //e.printStackTrace();
            }
    }

    @Override
    protected void onDestroy() {
        close();
        super.onDestroy();
    }
}
