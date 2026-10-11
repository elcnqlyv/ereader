package com.ereader.ui;

import com.ereader.model.Book;
import com.ereader.model.Settings;
import com.ereader.util.Utf8;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Vector;
import javax.microedition.io.Connector;
import javax.microedition.io.InputConnection;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

public class ReaderCanvas extends Canvas {
    public interface ReaderListener {
        void positionChanged(int page);
        void closeReader();
    }

    private Settings settings;
    private String currentPath;
    private String title;
    private String sourceText;
    private String[] lines;
    private int pageIndex;
    private ReaderListener listener;

    public ReaderCanvas(Settings settings) {
        this.settings = settings;
        setFullScreenMode(true);
    }

    public void setReaderListener(ReaderListener listener) {
        this.listener = listener;
    }

    public void openBook(Book book) {
        if (book == null) {
            return;
        }
        title = book.getTitle();
        currentPath = book.getPath();
        pageIndex = book.getLastPosition();
        loadFromPath(currentPath);
    }

    public void loadFromPath(String path) {
        currentPath = path;
        String text = "Unable to open file.\n\n" + path;
        try {
            InputConnection con = (InputConnection) Connector.open(currentPath, Connector.READ);
            InputStream input = con.openInputStream();
            byte[] buffer = new byte[512];
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int read;
            while ((read = input.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            input.close();
            con.close();
            text = Utf8.decode(out.toByteArray());
            if (text == null || text.length() == 0) {
                text = "Empty file.";
            }
        } catch (Exception ex) {
            text = "Unable to open file.\n\n" + path;
        }
        sourceText = text;
        lines = paginate(sourceText);
        if (pageIndex >= getPageCount()) {
            pageIndex = 0;
        }
        repaint();
    }

    public void nextPage() {
        if (pageIndex < getPageCount() - 1) {
            pageIndex++;
            notifyPositionChanged();
            repaint();
        }
    }

    public void previousPage() {
        if (pageIndex > 0) {
            pageIndex--;
            notifyPositionChanged();
            repaint();
        }
    }

    protected void keyPressed(int keyCode) {
        int action = getGameAction(keyCode);
        if (action == LEFT || action == UP) {
            previousPage();
        } else if (action == RIGHT || action == DOWN) {
            nextPage();
        } else if (keyCode == KEY_NUM0) {
            if (listener != null) {
                listener.closeReader();
            }
        } else if (keyCode == KEY_STAR) {
            settings.setTheme(settings.getTheme() == Settings.THEME_SEPIA ? Settings.THEME_GRUVBOX : Settings.THEME_SEPIA);
            repaint();
        } else if (keyCode == KEY_POUND) {
            int next = settings.getFontLevel() + 1;
            if (next > Settings.FONT_LARGE) {
                next = Settings.FONT_SMALL;
            }
            settings.setFontLevel(next);
            if (sourceText != null) {
                lines = paginate(sourceText);
                if (pageIndex >= getPageCount()) {
                    pageIndex = Math.max(0, getPageCount() - 1);
                    notifyPositionChanged();
                }
            }
            repaint();
        }
    }

    public void paint(Graphics g) {
        g.setColor(Theme.background(settings.getTheme()));
        g.fillRect(0, 0, getWidth(), getHeight());

        Font font = getFontForLevel(settings.getFontLevel());
        g.setFont(font);
        g.setColor(Theme.foreground(settings.getTheme()));

        int margin = 8;
        int pageHeight = getHeight() - 24;
        int lineHeight = font.getHeight() + 2;
        int maxLines = pageHeight / lineHeight;
        int start = pageIndex * maxLines;
        int end = start + maxLines;
        int y = 10;

        g.drawString(title == null ? "Reader" : title, 8, 4, Graphics.TOP | Graphics.LEFT);
        if (lines != null) {
            for (int i = start; i < end && i < lines.length; i++) {
                String line = lines[i];
                if (line == null) {
                    continue;
                }
                g.drawString(line, margin, y, Graphics.TOP | Graphics.LEFT);
                y += lineHeight;
            }
        }

        g.setColor(Theme.accent(settings.getTheme()));
        int pageCount = getPageCount();
        if (pageCount > 0) {
            g.drawString("Page " + (pageIndex + 1) + "/" + pageCount, getWidth() - 70, getHeight() - 12, Graphics.TOP | Graphics.LEFT);
        }
    }

    private Font getFontForLevel(int level) {
        if (level == Settings.FONT_SMALL) {
            return Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL);
        }
        if (level == Settings.FONT_LARGE) {
            return Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_LARGE);
        }
        return Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_MEDIUM);
    }

    private String[] paginate(String text) {
        Vector result = new Vector();
        String[] raw = splitLines(text);
        Font font = getFontForLevel(settings.getFontLevel());
        int availableWidth = Math.max(1, getWidth() - 16);
        for (int i = 0; i < raw.length; i++) {
            String line = raw[i];
            if (line == null || line.length() == 0) {
                result.addElement("");
                continue;
            }
            wrapLine(line, font, availableWidth, result);
        }
        String[] array = new String[result.size()];
        for (int i = 0; i < result.size(); i++) {
            array[i] = (String) result.elementAt(i);
        }
        return array;
    }

    private String[] splitLines(String text) {
        if (text == null || text.length() == 0) {
            return new String[] { "Empty file." };
        }
        Vector lines = new Vector();
        StringBuffer current = new StringBuffer();
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (ch == '\n') {
                lines.addElement(current.toString());
                current.setLength(0);
            } else if (ch != '\r') {
                current.append(ch);
            }
        }
        if (current.length() > 0) {
            lines.addElement(current.toString());
        }
        String[] result = new String[lines.size()];
        for (int i = 0; i < lines.size(); i++) {
            result[i] = (String) lines.elementAt(i);
        }
        return result;
    }

    private void wrapLine(String line, Font font, int availableWidth, Vector result) {
        int start = 0;
        while (start < line.length()) {
            int end = start;
            int lastSpace = -1;
            while (end < line.length()) {
                char ch = line.charAt(end);
                if (ch == ' ' || ch == '\t') {
                    lastSpace = end;
                }
                if (font.stringWidth(line.substring(start, end + 1)) > availableWidth) {
                    break;
                }
                end++;
            }
            if (end == line.length()) {
                result.addElement(line.substring(start));
                return;
            }
            int splitAt = lastSpace > start ? lastSpace : Math.max(start + 1, end);
            result.addElement(line.substring(start, splitAt));
            start = splitAt;
            while (start < line.length()
                    && (line.charAt(start) == ' ' || line.charAt(start) == '\t')) {
                start++;
            }
        }
    }

    private int getPageCount() {
        if (lines == null || lines.length == 0) {
            return 1;
        }
        Font font = getFontForLevel(settings.getFontLevel());
        int lineHeight = font.getHeight() + 2;
        int pageHeight = getHeight() - 24;
        int maxLines = pageHeight / lineHeight;
        return (lines.length + maxLines - 1) / Math.max(1, maxLines);
    }

    private void notifyPositionChanged() {
        if (listener != null) {
            listener.positionChanged(pageIndex);
        }
    }
}
