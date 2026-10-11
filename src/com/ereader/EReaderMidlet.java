package com.ereader;

import com.ereader.model.Book;
import com.ereader.model.Settings;
import com.ereader.epub.EpubReader;
import com.ereader.epub.TocEntry;
import com.ereader.store.LibraryStore;
import com.ereader.store.SettingsStore;
import com.ereader.ui.FileBrowserCanvas;
import com.ereader.ui.LibraryCanvas;
import com.ereader.ui.ReaderCanvas;
import com.ereader.ui.SearchCanvas;
import com.ereader.ui.PageCanvas;
import com.ereader.ui.SettingsCanvas;
import com.ereader.ui.TocCanvas;
import java.util.Vector;
import javax.microedition.lcdui.Display;
import javax.microedition.midlet.MIDlet;

public class EReaderMidlet extends MIDlet implements LibraryCanvas.BookSelectionListener,
        FileBrowserCanvas.PathSelectionListener, SettingsCanvas.SettingsChangeListener,
        ReaderCanvas.ReaderListener, SearchCanvas.SearchListener, PageCanvas.PageListener,
        TocCanvas.Listener {

    private Display display;
    private Settings settings;
    private LibraryCanvas libraryCanvas;
    private FileBrowserCanvas browserCanvas;
    private ReaderCanvas readerCanvas;
    private SettingsCanvas settingsCanvas;
    private SearchCanvas searchCanvas;
    private PageCanvas pageCanvas;
    private TocCanvas tocCanvas;
    private Book currentBook;

    public void startApp() {
        if (display == null) {
            display = Display.getDisplay(this);
            settings = SettingsStore.load();
            libraryCanvas = new LibraryCanvas(settings);
            libraryCanvas.setBookSelectionListener(this);
            readerCanvas = new ReaderCanvas(settings);
            readerCanvas.setReaderListener(this);
            settingsCanvas = new SettingsCanvas(settings);
            settingsCanvas.setSettingsChangeListener(this);
            searchCanvas = new SearchCanvas();
            searchCanvas.setSearchListener(this);
            pageCanvas = new PageCanvas();
            pageCanvas.setPageListener(this);
            tocCanvas = new TocCanvas(settings);
            tocCanvas.setListener(this);
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
            if (browserCanvas == null) {
                browserCanvas = new FileBrowserCanvas(settings);
                browserCanvas.setPathSelectionListener(this);
            }
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
        if (lower.endsWith(".txt") || lower.endsWith(".md")) {
            Book book = new Book(displayName(path), path);
            display.setCurrent(readerCanvas);
            currentBook = book;
            readerCanvas.openBook(book);
            LibraryStore.addOrUpdate(book);
        } else if (lower.endsWith(".epub")) {
            try {
                String text = EpubReader.readBook(path);
                Book book = new Book(displayName(path), path);
                display.setCurrent(readerCanvas);
                currentBook = book;
                readerCanvas.openText(book.getTitle(), path, text);
                try {
                    readerCanvas.setTableOfContents(EpubReader.readTableOfContents(path));
                } catch (Exception ignored) {
                    readerCanvas.setTableOfContents(new TocEntry[0]);
                }
                LibraryStore.addOrUpdate(book);
            } catch (Exception ex) {
                Book errorBook = new Book(displayName(path), path);
                currentBook = errorBook;
                readerCanvas.openText(errorBook.getTitle(), path,
                        "Unable to open EPUB.\n\n" + ex.toString());
                display.setCurrent(readerCanvas);
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

    public void openToc() {
        if (display != null) {
            display.setCurrent(tocCanvas);
        }
    }

    public void selectTocEntry(int index) {
        if (display != null) {
            display.setCurrent(readerCanvas);
            readerCanvas.selectTocEntry(index);
        }
    }

    public void closeToc() {
        if (display != null) {
            display.setCurrent(readerCanvas);
        }
    }
}
