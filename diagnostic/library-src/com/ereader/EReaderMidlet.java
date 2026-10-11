package com.ereader;

import com.ereader.model.Settings;
import com.ereader.ui.LibraryCanvas;
import javax.microedition.lcdui.Display;
import javax.microedition.midlet.MIDlet;

public final class EReaderMidlet extends MIDlet
        implements LibraryCanvas.BookSelectionListener {
    private Display display;

    public void startApp() {
        if (display == null) {
            display = Display.getDisplay(this);
            Settings settings = new Settings();
            LibraryCanvas library = new LibraryCanvas(settings);
            library.setBookSelectionListener(this);
            display.setCurrent(library);
        }
    }

    public void pauseApp() {
    }

    public void destroyApp(boolean unconditional) {
    }

    public void openBook(com.ereader.model.Book book) {
    }

    public void openBrowser() {
    }

    public void openSettings() {
    }
}
