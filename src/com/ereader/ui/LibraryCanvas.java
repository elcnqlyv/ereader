package com.ereader.ui;

import com.ereader.model.Book;
import com.ereader.model.Settings;
import com.ereader.store.LibraryStore;
import java.util.Vector;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

public class LibraryCanvas extends Canvas {
    public interface BookSelectionListener {
        void openBook(Book book);
        void openBrowser();
        void openSettings();
    }

    private Settings settings;
    private Vector books;
    private int selectedIndex;
    private BookSelectionListener listener;

    public LibraryCanvas(Settings settings) {
        this.settings = settings;
        setFullScreenMode(true);
        refresh();
    }

    public void setBookSelectionListener(BookSelectionListener listener) {
        this.listener = listener;
    }

    public void refresh() {
        books = LibraryStore.load();
        if (books.size() == 0) {
            books = new Vector();
            books.addElement(new Book("No books yet", "file:///E:/"));
        }
        selectedIndex = 0;
    }

    protected void keyPressed(int keyCode) {
        int action = getGameAction(keyCode);
        if (action == UP) {
            selectedIndex = Math.max(0, selectedIndex - 1);
        } else if (action == DOWN) {
            selectedIndex = Math.min(books.size() - 1, selectedIndex + 1);
        } else if (action == FIRE || action == LEFT || action == RIGHT) {
            Book selected = (Book) books.elementAt(selectedIndex);
            if (listener != null && selected.getPath() != null
                    && isSupportedBook(selected.getPath())) {
                listener.openBook(selected);
            }
        } else if (keyCode == KEY_NUM1) {
            if (listener != null) {
                listener.openBrowser();
            }
        } else if (keyCode == KEY_NUM2) {
            if (listener != null) {
                listener.openSettings();
            }
        } else if (keyCode == KEY_STAR) {
            settings.setTheme(settings.getTheme() == Settings.THEME_SEPIA ? Settings.THEME_GRUVBOX : Settings.THEME_SEPIA);
        } else if (keyCode == KEY_POUND) {
            int next = settings.getFontLevel() + 1;
            if (next > Settings.FONT_LARGE) {
                next = Settings.FONT_SMALL;
            }
            settings.setFontLevel(next);
        }
        repaint();
    }

    public void paint(Graphics g) {
        g.setColor(Theme.background(settings.getTheme()));
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(Theme.foreground(settings.getTheme()));
        Font titleFont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_SMALL);
        g.setFont(titleFont);
        g.drawString("Library", 4, 4, Graphics.TOP | Graphics.LEFT);

        int rowHeight = titleFont.getHeight() + 4;
        int footerHeight = titleFont.getHeight() + 6;
        int visibleRows = Math.max(1, (getHeight() - 28 - footerHeight) / rowHeight);
        int first = selectedIndex >= visibleRows ? selectedIndex - visibleRows + 1 : 0;
        int last = Math.min(books.size(), first + visibleRows);
        int y = 22;
        for (int i = first; i < last; i++) {
            Book book = (Book) books.elementAt(i);
            String label = book.getTitle();
            if (i == selectedIndex) {
                g.setColor(Theme.accent(settings.getTheme()));
                g.fillRect(0, y - 2, getWidth(), rowHeight);
                g.setColor(Theme.background(settings.getTheme()));
            } else {
                g.setColor(Theme.foreground(settings.getTheme()));
            }
            g.drawString(label, 4, y, Graphics.TOP | Graphics.LEFT);
            y += rowHeight;
        }

        g.setColor(Theme.accent(settings.getTheme()));
        g.drawString("1 files 2 settings", 4, getHeight() - footerHeight,
                     Graphics.TOP | Graphics.LEFT);
        g.drawString("* theme # font", getWidth() / 2, getHeight() - footerHeight,
                     Graphics.TOP | Graphics.LEFT);
    }

    private boolean isSupportedBook(String path) {
        String lower = path.toLowerCase();
        return lower.endsWith(".txt") || lower.endsWith(".md") || lower.endsWith(".epub");
    }
}
