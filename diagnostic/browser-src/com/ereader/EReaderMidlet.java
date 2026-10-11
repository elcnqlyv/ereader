package com.ereader;

import com.ereader.model.Book;
import com.ereader.model.Settings;
import com.ereader.ui.FileBrowserCanvas;
import com.ereader.ui.LibraryCanvas;
import javax.microedition.lcdui.Display;
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
    }

    public void closeBrowser() {
        display.setCurrent(library);
    }
}
