package com.ereader.util;

public final class Utf8 {
    private Utf8() {
    }

    public static String decode(byte[] data) {
        try {
            return new String(data, "UTF-8");
        } catch (Exception ex) {
            return new String(data);
        }
    }

    public static byte[] encode(String text) {
        try {
            return text.getBytes("UTF-8");
        } catch (Exception ex) {
            return text.getBytes();
        }
    }
}
