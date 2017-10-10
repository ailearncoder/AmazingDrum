package com.example.panpan.amazingdrum.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.example.panpan.amazingdrum.R;
import com.example.panpan.amazingdrum.ServerThread;
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
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);
        ButterKnife.inject(this);
        initView();
        startListen();
    }

    private void initView() {
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        listMembers.setAdapter(adapter);
        textInfo.setText("How to enter room?\nPlease input:" + IpAdressUtils.getIp(this));
    }

    @OnClick(R.id.button_begin)
    public void onViewClicked() {
    }

    private boolean isUiRun = false;
    private Runnable listRunnable = new Runnable() {
        @Override
        public void run() {
            adapter.clear();
            for (int i = 0; i < servers.size(); i++) {
                adapter.add(servers.get(i).getTag());
            }
            adapter.notifyDataSetChanged();
            isUiRun = false;
        }
    };

    private ServerThread.OnServerListener serverListener = new ServerThread.OnServerListener() {
        @Override
        public void OnStateChanged(ServerThread server, ServerThread.State state) {
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
        public void OnDataReceived(ServerThread server, byte[] data, int length) {

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
        super.onDestroy();
    }
}
