package com.example.panpan.amazingdrum.custom;

/**
 * Created by PanXuesen on 2017/10/17.
 */

public class Test {
    public static void main(String[] args) {
        System.out.println("Hello!");
        System.out.println(MyUtil.bytes2Addr(new byte[]{0x11,0x22,0x33,0x44,0x55,0x66},0));
    }
}
