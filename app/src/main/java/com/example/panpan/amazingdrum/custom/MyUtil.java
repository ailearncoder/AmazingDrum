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
}
