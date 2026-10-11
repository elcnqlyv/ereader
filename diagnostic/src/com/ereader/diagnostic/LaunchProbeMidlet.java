package com.ereader.diagnostic;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Graphics;
import javax.microedition.midlet.MIDlet;

public final class LaunchProbeMidlet extends MIDlet {
    private Display display;

    public void startApp() {
        if (display == null) {
            display = Display.getDisplay(this);
            display.setCurrent(new ProbeCanvas());
        }
    }

    public void pauseApp() {
    }

    public void destroyApp(boolean unconditional) {
    }

    private static final class ProbeCanvas extends Canvas {
        protected void paint(Graphics graphics) {
            graphics.setColor(0xFFFFFF);
            graphics.fillRect(0, 0, getWidth(), getHeight());
            graphics.setColor(0x000000);
            graphics.drawString("Launch probe OK", 4, 4,
                    Graphics.TOP | Graphics.LEFT);
        }
    }
}
