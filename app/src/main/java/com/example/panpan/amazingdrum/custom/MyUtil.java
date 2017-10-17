package com.example.panpan.amazingdrum.custom;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by panpan on 2017/10/11.
 */

public class MyUtil {
    public static Toast toast;
    public static void showToast(Context context,String msg)
    {
        if(toast!=null)
            toast.cancel();
        toast=Toast.makeText(context,msg,Toast.LENGTH_LONG);
        toast.show();
    }
    public static void sleep(long millis)
    {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    public static int byte2Int(byte data)
    {
        if(data<0)
            return 256+data;
        return data;
    }
    public static String  byte2HexStr(byte data)
    {
        return String.format("%02X",byte2Int(data));
    }
    public static String bytes2Addr(byte data[],int offset)
    {
        String result="";
        for (int i = offset; i < data.length; i++) {
            if(i!=offset)
                result+=":";
            result+=byte2HexStr(data[i]);
        }
        return result;
    }
    public static byte[] addr2Bytes(String addr)
    {
        String result[]=addr.split(":");
        byte data[]=new byte[result.length];
        for (int i = 0; i < data.length; i++) {
           data[i]= (byte) Integer.parseInt(result[i],16);
        }
        return data;
    }
}
