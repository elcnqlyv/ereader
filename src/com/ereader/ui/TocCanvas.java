package com.ereader.ui;

import com.ereader.epub.TocEntry;
import com.ereader.model.Settings;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

public final class TocCanvas extends Canvas {
    public interface Listener {
        void selectTocEntry(int index);
        void closeToc();
    }

    private final Settings settings;
    private TocEntry[] entries = new TocEntry[0];
    private int selected;
    private Listener listener;

    public TocCanvas(Settings settings) {
        this.settings = settings;
        setFullScreenMode(true);
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public void setEntries(TocEntry[] entries) {
        this.entries = entries == null ? new TocEntry[0] : entries;
        selected = 0;
        repaint();
    }

    protected void keyPressed(int keyCode) {
        int action = getGameAction(keyCode);
        if (action == UP) {
            selected = Math.max(0, selected - 1);
        } else if (action == DOWN) {
            selected = Math.min(entries.length - 1, selected + 1);
        } else if (action == FIRE || action == RIGHT) {
            if (listener != null && entries.length > 0) {
                listener.selectTocEntry(selected);
            }
        } else if (keyCode == KEY_NUM0 || action == LEFT) {
            if (listener != null) {
                listener.closeToc();
            }
        }
        repaint();
    }

    public void paint(Graphics g) {
        Font font = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_SMALL);
        g.setFont(font);
        g.setColor(Theme.background(settings.getTheme()));
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(Theme.foreground(settings.getTheme()));
        g.drawString("Contents", 4, 4, Graphics.TOP | Graphics.LEFT);
        int rowHeight = font.getHeight() + 4;
        int visible = Math.max(1, (getHeight() - 30) / rowHeight);
        int first = selected >= visible ? selected - visible + 1 : 0;
        int last = Math.min(entries.length, first + visible);
        int y = 22;
        for (int i = first; i < last; i++) {
            if (i == selected) {
                g.setColor(Theme.accent(settings.getTheme()));
                g.fillRect(0, y - 2, getWidth(), rowHeight);
                g.setColor(Theme.background(settings.getTheme()));
            } else {
                g.setColor(Theme.foreground(settings.getTheme()));
            }
            g.drawString(fit(entries[i].getTitle(), getWidth() - 8, font),
                    4, y, Graphics.TOP | Graphics.LEFT);
            y += rowHeight;
        }
        g.setColor(Theme.accent(settings.getTheme()));
        g.drawString("Select  0 back", 4, getHeight() - font.getHeight() - 2,
                Graphics.TOP | Graphics.LEFT);
    }

    private String fit(String value, int width, Font font) {
        if (font.stringWidth(value) <= width) {
            return value;
        }
        int end = value.length();
        while (end > 0 && font.stringWidth(value.substring(0, end) + "...") > width) {
            end--;
        }
        return end == 0 ? "..." : value.substring(0, end) + "...";
    }
}
