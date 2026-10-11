package com.ereader;

import com.ereader.model.Book;
import com.ereader.model.Settings;
import com.ereader.epub.EpubReader;
import com.ereader.ui.FileBrowserCanvas;
import com.ereader.ui.LibraryCanvas;
import com.ereader.ui.ReaderCanvas;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Graphics;
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
        if (path.toLowerCase().endsWith(".epub")) {
            display.setCurrent(new LoadingCanvas(display, reader, book));
        } else {
            display.setCurrent(reader);
            reader.openBook(book);
        }
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

    private static final class LoadingCanvas extends Canvas implements Runnable {
        private final Display display;
        private final ReaderCanvas reader;
        private final Book book;

        LoadingCanvas(Display display, ReaderCanvas reader, Book book) {
            this.display = display;
            this.reader = reader;
            this.book = book;
            new Thread(this).start();
        }

        protected void paint(Graphics graphics) {
            graphics.setColor(0xFFFFFF);
            graphics.fillRect(0, 0, getWidth(), getHeight());
            graphics.setColor(0x000000);
            graphics.drawString("Opening EPUB...", 4, 4,
                    Graphics.TOP | Graphics.LEFT);
            graphics.drawString("Please wait", 4, 24,
                    Graphics.TOP | Graphics.LEFT);
        }

        public void run() {
            try {
                String text = EpubReader.readFirstChapter(book.getPath());
                reader.openText(book.getTitle(), book.getPath(), text);
            } catch (Exception ex) {
                reader.openText(book.getTitle(), book.getPath(),
                        "Unable to open EPUB.\n\n" + ex.toString());
            }
            display.setCurrent(reader);
        }
    }
}
