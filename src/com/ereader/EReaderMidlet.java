package com.ereader;

import com.ereader.model.Book;
import com.ereader.model.Settings;
import com.ereader.store.LibraryStore;
import com.ereader.store.SettingsStore;
import com.ereader.ui.FileBrowserCanvas;
import com.ereader.ui.LibraryCanvas;
import com.ereader.ui.ReaderCanvas;
import com.ereader.ui.SettingsCanvas;
import java.util.Vector;
import javax.microedition.lcdui.Display;
import javax.microedition.midlet.MIDlet;

public class EReaderMidlet extends MIDlet implements LibraryCanvas.BookSelectionListener,
        FileBrowserCanvas.PathSelectionListener, SettingsCanvas.SettingsChangeListener {

    private Display display;
    private Settings settings;
    private LibraryCanvas libraryCanvas;
    private FileBrowserCanvas browserCanvas;
    private ReaderCanvas readerCanvas;
    private SettingsCanvas settingsCanvas;

    public void startApp() {
        if (display == null) {
            display = Display.getDisplay(this);
            settings = SettingsStore.load();
            libraryCanvas = new LibraryCanvas(settings);
            libraryCanvas.setBookSelectionListener(this);
            browserCanvas = new FileBrowserCanvas(settings);
            browserCanvas.setPathSelectionListener(this);
            readerCanvas = new ReaderCanvas(settings);
            settingsCanvas = new SettingsCanvas(settings);
            settingsCanvas.setSettingsChangeListener(this);
        }
        display.setCurrent(libraryCanvas);
    }

    public void pauseApp() {
    }

    public void destroyApp(boolean unconditional) {
        SettingsStore.save(settings);
    }

    public void openBook(Book book) {
        if (book == null) {
            return;
        }
        if (book.getPath() == null || book.getPath().length() == 0) {
            return;
        }
        if (display == null) {
            return;
        }
        display.setCurrent(readerCanvas);
        readerCanvas.openBook(book);
        LibraryStore.addOrUpdate(book);
    }

    public void openBrowser() {
        if (display != null) {
            browserCanvas.refresh();
            display.setCurrent(browserCanvas);
        }
    }

    public void openSettings() {
        if (display != null) {
            display.setCurrent(settingsCanvas);
        }
    }

    public void closeBrowser() {
        if (display != null) {
            libraryCanvas.refresh();
            display.setCurrent(libraryCanvas);
        }
    }

    public void closeSettings() {
        if (display != null) {
            display.setCurrent(libraryCanvas);
        }
    }

    public void openPath(String path) {
        if (path == null || path.length() == 0) {
            return;
        }
        if (display == null) {
            return;
        }
        if (path.toLowerCase().indexOf(".txt") >= 0 || path.toLowerCase().indexOf(".md") >= 0) {
            Book book = new Book(path, path);
            display.setCurrent(readerCanvas);
            readerCanvas.openBook(book);
            LibraryStore.addOrUpdate(book);
        } else {
            display.setCurrent(libraryCanvas);
        }
    }

    public void settingsChanged(Settings settings) {
        this.settings = settings;
        SettingsStore.save(this.settings);
    }
}
