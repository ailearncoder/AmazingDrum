package com.example.panpan.amazingdrum.custom;

/**
 * Created by PanXuesen on 2017/10/17.
 */

public class Test {
    public static void main(String[] args) {
        System.out.println("Hello!");
        System.out.println(MyUtil.bytes2Addr(new byte[]{0x11,0x22,0x33,0x44,0x55,0x66},0));
        System.out.println(String.format("0x%02X",MyUtil.byte2Int(MyUtil.int2Byte(0x11223344,4)[0])));
        System.out.println(String.format("0x%02X",MyUtil.byte2Int(MyUtil.int2Byte(0x11223344,4)[1])));
        System.out.println(String.format("0x%02X",MyUtil.byte2Int(MyUtil.int2Byte(0x11223344,4)[2])));
        System.out.println(String.format("0x%02X",MyUtil.byte2Int(MyUtil.int2Byte(0x11223344,4)[3])));
        System.out.println(String.format("0x%08X",MyUtil.byte2Int(0,MyUtil.int2Byte(0x89ABCDEF,4))));
    }
}
