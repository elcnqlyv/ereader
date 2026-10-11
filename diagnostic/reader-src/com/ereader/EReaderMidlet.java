package com.ereader;

import com.ereader.model.Book;
import com.ereader.model.Settings;
import com.ereader.ui.FileBrowserCanvas;
import com.ereader.ui.LibraryCanvas;
import com.ereader.ui.ReaderCanvas;
import javax.microedition.lcdui.Display;
import javax.microedition.midlet.MIDlet;

public final class EReaderMidlet extends MIDlet
        implements LibraryCanvas.BookSelectionListener,
                   FileBrowserCanvas.PathSelectionListener,
                   ReaderCanvas.ReaderListener {
    private Display display;
    private LibraryCanvas library;
    private FileBrowserCanvas browser;
    private ReaderCanvas reader;
    private Settings settings;

    public void startApp() {
        if (display == null) {
            display = Display.getDisplay(this);
            settings = new Settings();
            library = new LibraryCanvas(settings);
            library.setBookSelectionListener(this);
            reader = new ReaderCanvas(settings);
            reader.setReaderListener(this);
            display.setCurrent(library);
        }
    }

    public void pauseApp() {
    }

    public void destroyApp(boolean unconditional) {
    }

    public void openBook(Book book) {
        openPath(book.getPath());
    }

    public void openBrowser() {
        if (browser == null) {
            browser = new FileBrowserCanvas(settings);
            browser.setPathSelectionListener(this);
        }
        browser.refresh();
        display.setCurrent(browser);
    }

    public void openSettings() {
    }

    public void openPath(String path) {
        Book book = new Book(path, path);
        display.setCurrent(reader);
        reader.openBook(book);
    }

    public void closeBrowser() {
        display.setCurrent(library);
    }

    public void positionChanged(int page) {
    }

    public void closeReader() {
        display.setCurrent(library);
    }

    public void openSearch() {
    }

    public void openPagePrompt() {
    }

    public void openToc() {
    }
}
