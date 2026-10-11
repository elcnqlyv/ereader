package com.ereader.ui;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

public final class Theme {
    public static final int SEPIA = 0;
    public static final int GRUVBOX = 1;
    private static Image logo;

    private Theme() {
    }

    public static void drawLogo(Graphics g, int width) {
        try {
            if (logo == null) {
                logo = Image.createImage("/logo.png");
            }
            g.drawImage(logo, Math.max(0, width - logo.getWidth() - 2), 0,
                    Graphics.TOP | Graphics.LEFT);
        } catch (Exception ignored) {
        }
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
