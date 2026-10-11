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

public class EReaderMidlet extends MIDlet {

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
            libraryCanvas.setBookSelectionListener(new LibraryListener(this));
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
        ensureReader();
        display.setCurrent(readerCanvas);
        currentBook = book;
        readerCanvas.openBook(book);
        LibraryStore.addOrUpdate(book);
    }

    public void openBrowser() {
        if (display != null) {
            if (browserCanvas == null) {
                browserCanvas = new FileBrowserCanvas(settings);
                browserCanvas.setPathSelectionListener(new BrowserListener(this));
            }
            browserCanvas.refresh();
            display.setCurrent(browserCanvas);
        }
    }

    public void openSettings() {
        if (display != null) {
            ensureSettings();
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
            ensureReader();
            display.setCurrent(readerCanvas);
            currentBook = book;
            readerCanvas.openBook(book);
            LibraryStore.addOrUpdate(book);
        } else if (lower.endsWith(".epub")) {
            try {
                String text = EpubReader.readBook(path);
                Book book = new Book(displayName(path), path);
                ensureReader();
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
                ensureReader();
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
            ensureSearch();
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
            ensurePagePrompt();
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
            ensureToc();
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

    private void ensureReader() {
        if (readerCanvas == null) {
            readerCanvas = new ReaderCanvas(settings);
            readerCanvas.setReaderListener(new ReaderListener(this));
        }
    }

    private void ensureSettings() {
        if (settingsCanvas == null) {
            settingsCanvas = new SettingsCanvas(settings);
            settingsCanvas.setSettingsChangeListener(new SettingsListener(this));
        }
    }

    private void ensureSearch() {
        if (searchCanvas == null) {
            searchCanvas = new SearchCanvas();
            searchCanvas.setSearchListener(new SearchListener(this));
        }
    }

    private void ensurePagePrompt() {
        if (pageCanvas == null) {
            pageCanvas = new PageCanvas();
            pageCanvas.setPageListener(new PageListener(this));
        }
    }

    private void ensureToc() {
        if (tocCanvas == null) {
            tocCanvas = new TocCanvas(settings);
            tocCanvas.setListener(new TocListener(this));
        }
    }

    private static final class LibraryListener
            implements LibraryCanvas.BookSelectionListener {
        private final EReaderMidlet midlet;

        LibraryListener(EReaderMidlet midlet) {
            this.midlet = midlet;
        }

        public void openBook(Book book) {
            midlet.openBook(book);
        }

        public void openBrowser() {
            midlet.openBrowser();
        }

        public void openSettings() {
            midlet.openSettings();
        }
    }

    private static final class BrowserListener
            implements FileBrowserCanvas.PathSelectionListener {
        private final EReaderMidlet midlet;

        BrowserListener(EReaderMidlet midlet) {
            this.midlet = midlet;
        }

        public void openPath(String path) {
            midlet.openPath(path);
        }

        public void closeBrowser() {
            midlet.closeBrowser();
        }
    }

    private static final class SettingsListener
            implements SettingsCanvas.SettingsChangeListener {
        private final EReaderMidlet midlet;

        SettingsListener(EReaderMidlet midlet) {
            this.midlet = midlet;
        }

        public void settingsChanged(Settings settings) {
            midlet.settingsChanged(settings);
        }

        public void closeSettings() {
            midlet.closeSettings();
        }
    }

    private static final class ReaderListener
            implements ReaderCanvas.ReaderListener {
        private final EReaderMidlet midlet;

        ReaderListener(EReaderMidlet midlet) {
            this.midlet = midlet;
        }

        public void positionChanged(int page) {
            midlet.positionChanged(page);
        }

        public void closeReader() {
            midlet.closeReader();
        }

        public void openSearch() {
            midlet.openSearch();
        }

        public void openPagePrompt() {
            midlet.openPagePrompt();
        }

        public void openToc() {
            midlet.openToc();
        }
    }

    private static final class SearchListener
            implements SearchCanvas.SearchListener {
        private final EReaderMidlet midlet;

        SearchListener(EReaderMidlet midlet) {
            this.midlet = midlet;
        }

        public void search(String query) {
            midlet.search(query);
        }

        public void cancelSearch() {
            midlet.cancelSearch();
        }
    }

    private static final class PageListener
            implements PageCanvas.PageListener {
        private final EReaderMidlet midlet;

        PageListener(EReaderMidlet midlet) {
            this.midlet = midlet;
        }

        public void goToPage(String value) {
            midlet.goToPage(value);
        }

        public void cancelPage() {
            midlet.cancelPage();
        }
    }

    private static final class TocListener
            implements TocCanvas.Listener {
        private final EReaderMidlet midlet;

        TocListener(EReaderMidlet midlet) {
            this.midlet = midlet;
        }

        public void selectTocEntry(int index) {
            midlet.selectTocEntry(index);
        }

        public void closeToc() {
            midlet.closeToc();
        }
    }
}
