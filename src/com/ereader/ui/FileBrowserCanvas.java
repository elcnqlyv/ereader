package com.ereader.ui;

import com.ereader.model.Settings;
import java.util.Enumeration;
import java.util.Vector;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

public class FileBrowserCanvas extends Canvas {
    public interface PathSelectionListener {
        void openPath(String path);
        void closeBrowser();
    }

    private Settings settings;
    private String[] paths;
    private String[] labels;
    private String currentPath;
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

    public void refresh() {
        buildPaths();
        repaint();
    }

    private void buildPaths() {
        currentPath = settings.getRootPath();
        loadDirectory();
    }

    private void loadDirectory() {
        Vector pathList = new Vector();
        Vector labelList = new Vector();
        FileConnection connection = null;
        try {
            connection = (FileConnection) Connector.open(currentPath, Connector.READ);
            Enumeration entries = connection.list();
            while (entries.hasMoreElements()) {
                String name = (String) entries.nextElement();
                pathList.addElement(currentPath + name);
                labelList.addElement(name);
            }
        } catch (Exception ex) {
            labelList.addElement("Unable to read " + currentPath);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (Exception ignored) {
                }
            }
        }
        paths = new String[pathList.size()];
        labels = new String[labelList.size()];
        for (int i = 0; i < pathList.size(); i++) {
            paths[i] = (String) pathList.elementAt(i);
        }
        for (int i = 0; i < labelList.size(); i++) {
            labels[i] = (String) labelList.elementAt(i);
        }
        selectedIndex = 0;
    }

    protected void keyPressed(int keyCode) {
        int action = getGameAction(keyCode);
        if (action == UP) {
            selectedIndex = Math.max(0, selectedIndex - 1);
        } else if (action == DOWN) {
            selectedIndex = Math.min(Math.max(0, paths.length - 1), selectedIndex + 1);
        } else if (keyCode == KEY_NUM0) {
            if (!settings.getRootPath().equals(currentPath)) {
                int end = currentPath.lastIndexOf('/', currentPath.length() - 2);
                currentPath = currentPath.substring(0, end + 1);
                loadDirectory();
            } else if (listener != null) {
                listener.closeBrowser();
            }
        } else if (action == FIRE || action == LEFT || action == RIGHT) {
            if (paths.length == 0) {
                return;
            }
            String selectedPath = paths[selectedIndex];
            if (selectedPath.charAt(selectedPath.length() - 1) == '/') {
                currentPath = selectedPath;
                loadDirectory();
            } else if (listener != null) {
                listener.openPath(selectedPath);
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

        int rowHeight = 16;
        int first = 0;
        int visibleRows = Math.max(1, (getHeight() - 40) / rowHeight);
        if (selectedIndex >= visibleRows) {
            first = selectedIndex - visibleRows + 1;
        }
        int last = Math.min(labels.length, first + visibleRows);
        int y = 22;
        for (int i = first; i < last; i++) {
            String label = labels[i];
            if (i == selectedIndex) {
                g.setColor(Theme.accent(settings.getTheme()));
                g.fillRect(0, y - 2, getWidth(), 14);
                g.setColor(Theme.background(settings.getTheme()));
            } else {
                g.setColor(Theme.foreground(settings.getTheme()));
            }
            g.drawString(label, 4, y, Graphics.TOP | Graphics.LEFT);
            y += rowHeight;
        }
        g.setColor(Theme.accent(settings.getTheme()));
        g.drawString("0 back  select file", 4, getHeight() - 12,
                     Graphics.TOP | Graphics.LEFT);
    }
}
