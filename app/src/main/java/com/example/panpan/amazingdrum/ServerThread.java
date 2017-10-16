package com.example.panpan.amazingdrum;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by panpan on 2017/10/10.
 */

public class ServerThread extends Thread {
    public int id = 0;
    private String tag="";
    private boolean isRun = false;
    private Socket socket;
    private OutputStream outputStream;
    private State state = State.None;
    private OnServerListener listener;

    public ServerThread(Socket socket) {
        this.socket = socket;
        this.setPriority(Thread.MAX_PRIORITY);
    }

    @Override
    public void run() {
        isRun = true;
        //获取输入流,并读取客户端的信息
        InputStream is = null;
        OutputStream os = null;
        try {
            is = socket.getInputStream();
            os = socket.getOutputStream();
            outputStream = os;
            byte data[] = new byte[128];
            int length = 0;
            OnStateChanged(State.Linked);
            length = is.read(data);
            if(length>0)
            {
                if(data[0]==0x00) {
                    String name=new String(data,1,data.length-1,"UTF-8");
                    this.tag=name;
                    OnStateChanged(State.Verified);
                }
            }
            while (isRun) {
                length = is.read(data);
                if (length > 0) {
                    OnDataReceived(data, length);
                } else {
                    break;
                }
            }
        } catch (Exception e) {
        }
        try {
            if (is != null) {
                is.close();
                is = null;
            }
        } catch (Exception e) {

        }
        close();
        isRun = false;
        OnStateChanged(State.Dislink);
    }

    private synchronized void close() {
        try {
            if (outputStream != null) {
                outputStream.close();
                outputStream = null;
            }
        } catch (Exception e) {

        }
        try {
            if (socket != null) {
                socket.close();
                socket = null;
            }
        } catch (Exception e) {

        }
        isRun = false;
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

    public boolean write(byte[] data, int off, int len) {
        try {
            if (outputStream != null)
                outputStream.write(data, off, len);
            return true;
        } catch (Exception e) {
            close();
        }
        return false;
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

    public OnServerListener getListener() {
        return listener;
    }

    public void setListener(OnServerListener listener) {
        this.listener = listener;
    }

    public String getTag() {
        return tag;
    }

    public interface OnServerListener {
        void OnStateChanged(ServerThread server, State state);

        void OnDataReceived(ServerThread server, byte[] data, int length);
    }

    public enum State {
        None,
        Linked,
        Verified,
        Dislink
    }
}
