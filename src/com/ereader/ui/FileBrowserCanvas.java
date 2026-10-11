package com.ereader.ui;

import com.ereader.model.Settings;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

public class FileBrowserCanvas extends Canvas {
    public interface PathSelectionListener {
        void openPath(String path);
    }

    private Settings settings;
    private String[] paths;
    private int selectedIndex;
    private PathSelectionListener listener;

    public FileBrowserCanvas(Settings settings) {
        this.settings = settings;
        setFullScreenMode(true);
        buildPaths();
    }

    public void setPathSelectionListener(PathSelectionListener listener) {
        this.listener = listener;
    }

    private void buildPaths() {
        paths = new String[] {
            "file:///E:/",
            "file:///E:/Books/",
            "file:///E:/Books/sample.txt",
            "file:///E:/sample.txt",
            "file:///E:/notes.txt"
        };
        selectedIndex = 0;
    }

    protected void keyPressed(int keyCode) {
        int action = getGameAction(keyCode);
        if (action == UP) {
            selectedIndex = Math.max(0, selectedIndex - 1);
        } else if (action == DOWN) {
            selectedIndex = Math.min(paths.length - 1, selectedIndex + 1);
        } else if (action == FIRE || action == LEFT || action == RIGHT) {
            if (listener != null) {
                listener.openPath(paths[selectedIndex]);
            }
        }
        repaint();
    }

    public void paint(Graphics g) {
        g.setColor(Theme.background(settings.getTheme()));
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(Theme.foreground(settings.getTheme()));
        Font font = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_SMALL);
        g.setFont(font);
        g.drawString("File Browser", 4, 4, Graphics.TOP | Graphics.LEFT);

        int y = 22;
        for (int i = 0; i < paths.length; i++) {
            String label = paths[i];
            if (i == selectedIndex) {
                g.setColor(Theme.accent(settings.getTheme()));
                g.fillRect(0, y - 2, getWidth(), 14);
                g.setColor(Theme.background(settings.getTheme()));
            } else {
                g.setColor(Theme.foreground(settings.getTheme()));
            }
            g.drawString(label, 4, y, Graphics.TOP | Graphics.LEFT);
            y += 16;
        }
    }
}
