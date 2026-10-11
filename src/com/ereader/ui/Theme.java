package com.ereader.ui;

import javax.microedition.lcdui.Graphics;

public final class Theme {
    public static final int SEPIA = 0;
    public static final int GRUVBOX = 1;
    private Theme() {
    }

    public static int background(int theme) {
        if (theme == GRUVBOX) {
            return 0x1D1D1D;
        }
        return 0xF4ECD8;
    }

    public static int foreground(int theme) {
        if (theme == GRUVBOX) {
            return 0xF5E6C8;
        }
        return 0x3D2B1F;
    }

    public static int accent(int theme) {
        if (theme == GRUVBOX) {
            return 0xEABF6B;
        }
        return 0x8C6D4F;
    }
}
