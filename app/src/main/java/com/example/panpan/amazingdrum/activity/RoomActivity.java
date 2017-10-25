package com.example.panpan.amazingdrum.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.example.panpan.amazingdrum.R;
import com.example.panpan.amazingdrum.ServerThread;
import com.example.panpan.amazingdrum.sound.Sound;
import com.example.panpan.amazingdrum.custom.MyUtil;
import com.example.panpan.amazingdrum.util.IpAdressUtils;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class RoomActivity extends Activity {

    @InjectView(R.id.text_info)
    TextView textInfo;
    @InjectView(R.id.list_members)
    ListView listMembers;
    @InjectView(R.id.button_begin)
    Button buttonBegin;
    private ArrayList<ServerThread> servers = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private Sound sound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);
        ButterKnife.inject(this);
        initView();
        startListen();
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        sound = new Sound();
        sound.init(this);
    }

    private void initView() {
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        listMembers.setAdapter(adapter);
        textInfo.setText("How to enter room?\nPlease input:" + IpAdressUtils.getIp(this));
    }

    @OnClick(R.id.button_begin)
    public void onViewClicked() {
        if (servers.size() > 0) {
            ServerThread thread;
            for (int i = 0; i < servers.size(); i++) {
                thread = servers.get(i);
                thread.write((byte) 0x01, (byte) 0x01);
            }
        } else {
            MyUtil.showToast(this, "等待玩家加入房间");
        }
    }

    private boolean isUiRun = false;
    private Runnable listRunnable = new Runnable() {
        @Override
        public void run() {
            adapter.clear();
            if (MainBandActivity.instrumentType == 0)
                adapter.add("房主——吉他");
            if (MainBandActivity.instrumentType == 1)
                adapter.add("房主——架子鼓");
            if (MainBandActivity.instrumentType == 2)
                adapter.add("房主——电吉他");
            if (MainBandActivity.instrumentType == 3)
                adapter.add("房主——贝斯");
            for (int i = 0; i < servers.size(); i++) {
                adapter.add(servers.get(i).getTag());
            }
            adapter.notifyDataSetChanged();
            isUiRun = false;
        }
    };

    private ServerThread.OnServerListener serverListener = new ServerThread.OnServerListener() {
        @Override
        public void OnStateChanged(ServerThread server, ServerThread.LinkState state) {
            synchronized (this) {
                switch (state) {
                    case Linked:
                        break;
                    case Verified:
                        servers.add(server);
                        break;
                    case Dislink:
                        servers.remove(server);
                        break;
                }
                isUiRun = true;
                runOnUiThread(listRunnable);
                while (isUiRun) ;
            }
        }

        @Override
        public void OnDataReceived(ServerThread server, final byte[] data, int length) {
            //sound.play(data[1]%4);
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    MyUtil.showToast(RoomActivity.this, "received:" + data[1]);
//                }
//            });
            if (data[0]==2&&data[1] == 1) {
                switch (data[2]) {
                    case 0x01://底鼓
                        sound.play(2);
                        break;
                    case 0x02://军鼓
                        sound.play(3);
                        break;
                    case 0x04://左擦
                        sound.play(0);
                        break;
                    case 0x08://右擦
                        sound.play(1);
                        break;
                    default:
                        break;
                }
            }
        }
    };
    ServerSocket serverSocket;

    public void startListen() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    serverSocket = new ServerSocket(8888);
                    Log.i("RoomActivity", "startListen");
                    Socket socket;
                    while (true) {
                        socket = serverSocket.accept();
                        ServerThread serverThread = new ServerThread(socket);
                        serverThread.setListener(serverListener);
                        serverThread.start();
                    }
                } catch (Exception e) {
                    Log.i("RoomActivity", "endListen");
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    @Override
    protected void onDestroy() {
        try {
            if (serverSocket != null)
                serverSocket.close();
        } catch (Exception e) {

        }
        sound.release();
        super.onDestroy();
    }
}
