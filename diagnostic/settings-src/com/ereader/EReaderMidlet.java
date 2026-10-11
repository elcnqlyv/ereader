package com.ereader;

import com.ereader.model.Settings;
import com.ereader.store.SettingsStore;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Graphics;
import javax.microedition.midlet.MIDlet;

public final class EReaderMidlet extends MIDlet {
    private Display display;

    public void startApp() {
        if (display == null) {
            display = Display.getDisplay(this);
            Settings settings = SettingsStore.load();
            display.setCurrent(new SettingsProbeCanvas(settings));
        }
    }

    public void pauseApp() {
    }

    public void destroyApp(boolean unconditional) {
    }

    private static final class SettingsProbeCanvas extends Canvas {
        private final Settings settings;

        SettingsProbeCanvas(Settings settings) {
            this.settings = settings;
        }

        protected void paint(Graphics graphics) {
            graphics.setColor(0xFFFFFF);
            graphics.fillRect(0, 0, getWidth(), getHeight());
            graphics.setColor(0x000000);
            graphics.drawString("Settings OK " + settings.getTheme(), 4, 4,
                    Graphics.TOP | Graphics.LEFT);
        }
    }
}
