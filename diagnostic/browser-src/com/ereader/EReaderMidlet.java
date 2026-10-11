package com.ereader;

import com.ereader.model.Book;
import com.ereader.model.Settings;
import com.ereader.ui.FileBrowserCanvas;
import com.ereader.ui.LibraryCanvas;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Graphics;
import javax.microedition.midlet.MIDlet;

public final class EReaderMidlet extends MIDlet
        implements LibraryCanvas.BookSelectionListener,
                   FileBrowserCanvas.PathSelectionListener {
    private Display display;
    private LibraryCanvas library;
    private FileBrowserCanvas browser;

    public void startApp() {
        if (display == null) {
            display = Display.getDisplay(this);
            Settings settings = new Settings();
            library = new LibraryCanvas(settings);
            library.setBookSelectionListener(this);
            display.setCurrent(library);
        }
    }

    public void pauseApp() {
    }

    public void destroyApp(boolean unconditional) {
    }

    public void openBook(Book book) {
    }

    public void openBrowser() {
        if (browser == null) {
            browser = new FileBrowserCanvas(new Settings());
            browser.setPathSelectionListener(this);
        }
        browser.refresh();
        display.setCurrent(browser);
    }

    public void openSettings() {
    }

    public void openPath(String path) {
        display.setCurrent(new SelectedPathCanvas(path, display, library));
    }

    public void closeBrowser() {
        display.setCurrent(library);
    }

    private static final class SelectedPathCanvas extends Canvas {
        private final String path;
        private final Display display;
        private final LibraryCanvas library;

        SelectedPathCanvas(String path, Display display, LibraryCanvas library) {
            this.path = path;
            this.display = display;
            this.library = library;
        }

        protected void paint(Graphics graphics) {
            graphics.setColor(0xFFFFFF);
            graphics.fillRect(0, 0, getWidth(), getHeight());
            graphics.setColor(0x000000);
            graphics.drawString("Selected file:", 4, 4,
                    Graphics.TOP | Graphics.LEFT);
            String value = path == null ? "" : path;
            int start = 0;
            int y = 24;
            while (start < value.length() && y < getHeight() - 20) {
                int end = Math.min(value.length(), start + 38);
                graphics.drawString(value.substring(start, end), 4, y,
                        Graphics.TOP | Graphics.LEFT);
                start = end;
                y += 16;
            }
            graphics.drawString("0 back", 4, getHeight() - 18,
                    Graphics.TOP | Graphics.LEFT);
        }

        protected void keyPressed(int keyCode) {
            if (keyCode == KEY_NUM0) {
                display.setCurrent(library);
            }
        }
    }
}
