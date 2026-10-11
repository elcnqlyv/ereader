package com.ereader.ui;

import com.ereader.model.Settings;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

public class SettingsCanvas extends Canvas {
    public interface SettingsChangeListener {
        void settingsChanged(Settings settings);
        void closeSettings();
    }

    private Settings settings;
    private int selectedIndex;
    private SettingsChangeListener listener;

    public SettingsCanvas(Settings settings) {
        this.settings = settings;
        setFullScreenMode(true);
        selectedIndex = 0;
    }

    public void setSettingsChangeListener(SettingsChangeListener listener) {
        this.listener = listener;
    }

    protected void keyPressed(int keyCode) {
        int action = getGameAction(keyCode);
        if (action == UP) {
            selectedIndex = Math.max(0, selectedIndex - 1);
        } else if (action == DOWN) {
            selectedIndex = Math.min(4, selectedIndex + 1);
        } else if (keyCode == KEY_NUM0) {
            if (listener != null) {
                listener.closeSettings();
            }
        } else if (keyCode == KEY_NUM3) {
            selectedIndex = 2;
            applyAndNotify();
        } else if (keyCode == KEY_NUM4) {
            selectedIndex = 3;
            applyAndNotify();
        } else if (keyCode == KEY_NUM5) {
            selectedIndex = 4;
            applyAndNotify();
        } else if (action == FIRE || action == LEFT || action == RIGHT) {
            applyAndNotify();
        }
        repaint();
    }

    private void applySelection() {
        if (selectedIndex == 0) {
            settings.setTheme(Settings.THEME_SEPIA);
        } else if (selectedIndex == 1) {
            settings.setTheme(Settings.THEME_GRUVBOX);
        } else if (selectedIndex == 2) {
            settings.setFontLevel(Settings.FONT_SMALL);
        } else if (selectedIndex == 3) {
            settings.setFontLevel(Settings.FONT_MEDIUM);
        } else if (selectedIndex == 4) {
            settings.setFontLevel(Settings.FONT_LARGE);
        }
    }

    public void paint(Graphics g) {
        g.setColor(Theme.background(settings.getTheme()));
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(Theme.foreground(settings.getTheme()));
        Font font = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_SMALL);
        g.setFont(font);
        g.drawString("Settings", 4, 4, Graphics.TOP | Graphics.LEFT);
        Theme.drawLogo(g, getWidth());

        String[] items = {
            (settings.getTheme() == Settings.THEME_SEPIA ? "[x] " : "[ ] ") + "Theme: Sepia",
            (settings.getTheme() == Settings.THEME_GRUVBOX ? "[x] " : "[ ] ") + "Theme: Gruvbox",
            (settings.getFontLevel() == Settings.FONT_SMALL ? "[x] " : "[ ] ") + "Font: Small",
            (settings.getFontLevel() == Settings.FONT_MEDIUM ? "[x] " : "[ ] ") + "Font: Medium",
            (settings.getFontLevel() == Settings.FONT_LARGE ? "[x] " : "[ ] ") + "Font: Large"
        };

        int rowHeight = font.getHeight() + 4;
        int footerHeight = font.getHeight() + 6;
        int y = 24;
        for (int i = 0; i < items.length; i++) {
            if (i == selectedIndex) {
                g.setColor(Theme.accent(settings.getTheme()));
                g.fillRect(0, y - 2, getWidth(), rowHeight);
                g.setColor(Theme.background(settings.getTheme()));
            } else {
                g.setColor(Theme.foreground(settings.getTheme()));
            }
            g.drawString(items[i], 4, y, Graphics.TOP | Graphics.LEFT);
            y += rowHeight;
        }
        g.setColor(Theme.accent(settings.getTheme()));
        g.drawString("0 back  3/4/5 font", 4, getHeight() - footerHeight,
                     Graphics.TOP | Graphics.LEFT);
    }

    private void applyAndNotify() {
        applySelection();
        if (listener != null) {
            listener.settingsChanged(settings);
        }
    }
}
