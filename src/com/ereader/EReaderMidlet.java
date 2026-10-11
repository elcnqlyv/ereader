package com.ereader;

import com.ereader.model.Book;
import com.ereader.model.Settings;
import com.ereader.epub.EpubReader;
import com.ereader.store.LibraryStore;
import com.ereader.store.SettingsStore;
import com.ereader.ui.FileBrowserCanvas;
import com.ereader.ui.LibraryCanvas;
import com.ereader.ui.ReaderCanvas;
import com.ereader.ui.SearchCanvas;
import com.ereader.ui.PageCanvas;
import com.ereader.ui.SettingsCanvas;
import java.util.Vector;
import javax.microedition.lcdui.Display;
import javax.microedition.midlet.MIDlet;

public class EReaderMidlet extends MIDlet implements LibraryCanvas.BookSelectionListener,
        FileBrowserCanvas.PathSelectionListener, SettingsCanvas.SettingsChangeListener,
        ReaderCanvas.ReaderListener, SearchCanvas.SearchListener, PageCanvas.PageListener {

    private Display display;
    private Settings settings;
    private LibraryCanvas libraryCanvas;
    private FileBrowserCanvas browserCanvas;
    private ReaderCanvas readerCanvas;
    private SettingsCanvas settingsCanvas;
    private SearchCanvas searchCanvas;
    private PageCanvas pageCanvas;
    private Book currentBook;

    public void startApp() {
        if (display == null) {
            display = Display.getDisplay(this);
            settings = SettingsStore.load();
            libraryCanvas = new LibraryCanvas(settings);
            libraryCanvas.setBookSelectionListener(this);
            browserCanvas = new FileBrowserCanvas(settings);
            browserCanvas.setPathSelectionListener(this);
            readerCanvas = new ReaderCanvas(settings);
            readerCanvas.setReaderListener(this);
            settingsCanvas = new SettingsCanvas(settings);
            settingsCanvas.setSettingsChangeListener(this);
            searchCanvas = new SearchCanvas();
            searchCanvas.setSearchListener(this);
            pageCanvas = new PageCanvas();
            pageCanvas.setPageListener(this);
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
        currentBook = book;
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
        String lower = path.toLowerCase();
        if (lower.indexOf(".txt") >= 0 || lower.indexOf(".md") >= 0) {
            Book book = new Book(displayName(path), path);
            display.setCurrent(readerCanvas);
            currentBook = book;
            readerCanvas.openBook(book);
            LibraryStore.addOrUpdate(book);
        } else if (lower.indexOf(".epub") >= 0) {
            try {
                String text = EpubReader.readBook(path);
                Book book = new Book(displayName(path), path);
                display.setCurrent(readerCanvas);
                currentBook = book;
                readerCanvas.openText(book.getTitle(), path, text);
                LibraryStore.addOrUpdate(book);
            } catch (Exception ex) {
                display.setCurrent(libraryCanvas);
            }
        } else {
            display.setCurrent(libraryCanvas);
        }
    }

    private String displayName(String path) {
        int end = path.length();
        if (end > 0 && path.charAt(end - 1) == '/') {
            end--;
        }
        int slash = path.lastIndexOf('/', end - 1);
        if (slash >= 0 && slash + 1 < end) {
            return path.substring(slash + 1, end);
        }
        return path;
    }

    public void settingsChanged(Settings settings) {
        this.settings = settings;
        SettingsStore.save(this.settings);
    }

    public void positionChanged(int page) {
        if (currentBook != null) {
            currentBook.setLastPosition(page);
            LibraryStore.addOrUpdate(currentBook);
        }
    }

    public void closeReader() {
        if (display != null) {
            libraryCanvas.refresh();
            display.setCurrent(libraryCanvas);
        }
    }

    public void openSearch() {
        if (display != null) {
            display.setCurrent(searchCanvas);
        }
    }

    public void search(String query) {
        if (display != null) {
            display.setCurrent(readerCanvas);
            readerCanvas.search(query);
        }
    }

    public void cancelSearch() {
        if (display != null) {
            display.setCurrent(readerCanvas);
        }
    }

    public void openPagePrompt() {
        if (display != null) {
            display.setCurrent(pageCanvas);
        }
    }

    public void goToPage(String value) {
        if (display != null) {
            display.setCurrent(readerCanvas);
            readerCanvas.goToPage(value);
        }
    }

    public void cancelPage() {
        if (display != null) {
            display.setCurrent(readerCanvas);
        }
    }
}
