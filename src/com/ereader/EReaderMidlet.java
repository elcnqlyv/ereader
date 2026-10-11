// SPDX-License-Identifier: CC-BY-NC-SA-4.0
package com.ereader;

import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;

public class EReaderMidlet extends MIDlet {
    private Display display;
    private HelloCanvas canvas;

    public void startApp() {
        display = Display.getDisplay(this);
        if (canvas == null) canvas = new HelloCanvas();
        display.setCurrent(canvas);
    }

    public void pauseApp() {}
    public void destroyApp(boolean unconditional) {}

    static class HelloCanvas extends Canvas {
        HelloCanvas() { setFullScreenMode(true); }

        protected void paint(Graphics g) {
            // Sepia theme colors
            g.setColor(0xF4ECD8);
            g.fillRect(0, 0, getWidth(), getHeight());

            g.setColor(0x5B4636);
            Font title = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_LARGE);
            g.setFont(title);
            g.drawString("EReader", getWidth() / 2, getHeight() / 2 - 20,
                         Graphics.HCENTER | Graphics.BASELINE);

            Font body = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_MEDIUM);
            g.setFont(body);
            g.drawString("Smoke test OK", getWidth() / 2, getHeight() / 2 + 10,
                         Graphics.HCENTER | Graphics.BASELINE);
            g.drawString(getWidth() + "x" + getHeight(),
                         getWidth() / 2, getHeight() / 2 + 30,
                         Graphics.HCENTER | Graphics.BASELINE);
        }
    }
}
