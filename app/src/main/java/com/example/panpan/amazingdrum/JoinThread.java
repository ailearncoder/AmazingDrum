package com.example.panpan.amazingdrum;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by panpan on 2017/10/11.
 */

public class JoinThread extends Thread {
    private Socket socket;
    private InputStream inputStream;
    private OutputStream outputStream;
    private String host;
    private OnJoinListener listener;
    private State state = State.None;

    public JoinThread(String name, String host) {
        this.host = host;
        setName(name);
        this.setPriority(Thread.MAX_PRIORITY);
    }

    @Override
    public void run() {
        try {
            socket = new Socket(host, 8888);
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();
            OnStateChanged(State.Linked);
            byte name[] = getName().getBytes("UTF-8");
            byte name2[] = new byte[name.length + 1];
            System.arraycopy(name, 0, name2, 1, name.length);
            name2[0] = 0;
            outputStream.write(name2);
            OnStateChanged(State.Verified);
            byte data[] = new byte[128];
            int length = 0;
            while (true) {
                length = inputStream.read(data);
                if (length > 0) {
                    OnDataReceived(data, length);
                } else {
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        OnStateChanged(State.Dislink);
        close();
    }

    public boolean write(byte... data) {
        try {
            if (outputStream != null) {
                outputStream.write(data);
                return true;
            }
        } catch (Exception e) {
            close();
        }
        return false;
    }

    public void close() {
        if (inputStream != null)
            try {
                inputStream.close();
            } catch (IOException e) {
                //e.printStackTrace();
            }
        inputStream = null;
        if (outputStream != null)
            try {
                outputStream.close();
            } catch (IOException e) {
                //e.printStackTrace();
            }
        outputStream = null;
        if (socket != null)
            try {
                socket.close();
            } catch (IOException e) {
                //e.printStackTrace();
            }
        socket = null;
    }

    void OnStateChanged(State state) {
        if (this.state != state) {
            this.state = state;
            if (listener != null)
                listener.OnStateChanged(this, state);
        }
    }

    void OnDataReceived(byte[] data, int length) {
        if (listener != null)
            listener.OnDataReceived(this, data, length);
    }

    public OnJoinListener getListener() {
        return listener;
    }

    public void setListener(OnJoinListener listener) {
        this.listener = listener;
    }

    public interface OnJoinListener {
        void OnStateChanged(JoinThread thread, State state);

        void OnDataReceived(JoinThread thread, byte[] data, int length);
    }

    public enum State {
        None,
        Linked,
        Verified,
        Dislink
    }
}
