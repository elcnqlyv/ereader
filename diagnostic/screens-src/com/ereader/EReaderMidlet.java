package com.ereader;

import com.ereader.model.Settings;
import com.ereader.ui.LibraryCanvas;
import com.ereader.ui.SettingsCanvas;
import javax.microedition.lcdui.Display;
import javax.microedition.midlet.MIDlet;

public final class EReaderMidlet extends MIDlet
        implements LibraryCanvas.BookSelectionListener,
                   SettingsCanvas.SettingsChangeListener {
    private Display display;
    private LibraryCanvas library;
    private SettingsCanvas settingsScreen;

    public void startApp() {
        if (display == null) {
            display = Display.getDisplay(this);
            Settings settings = new Settings();
            library = new LibraryCanvas(settings);
            library.setBookSelectionListener(this);
            settingsScreen = new SettingsCanvas(settings);
            settingsScreen.setSettingsChangeListener(this);
        }
        display.setCurrent(library);
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
        display.setCurrent(settingsScreen);
    }

    public void settingsChanged(Settings settings) {
    }

    public void closeSettings() {
        display.setCurrent(library);
    }
}
