package com.ereader.store;

import com.ereader.model.Bookmark;
import com.ereader.util.Utf8;
import java.util.Vector;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;
import javax.microedition.rms.RecordStoreNotFoundException;

public final class BookmarkStore {
    private static final String STORE_NAME = "EReaderBookmarks";

    private BookmarkStore() {
    }

    public static Vector load() {
        Vector bookmarks = new Vector();
        try {
            RecordStore rs = RecordStore.openRecordStore(STORE_NAME, false);
            if (rs.getNumRecords() == 0) {
                rs.closeRecordStore();
                return bookmarks;
            }

            byte[] raw = rs.getRecord(1);
            rs.closeRecordStore();
            String text = Utf8.decode(raw);
            String[] lines = split(text);
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i];
                if (line == null || line.length() == 0) {
                    continue;
                }
                int idx = line.indexOf('|');
                if (idx <= 0) {
                    continue;
                }
                String path = line.substring(0, idx);
                String rest = line.substring(idx + 1);
                int idx2 = rest.indexOf('|');
                if (idx2 <= 0) {
                    continue;
                }
                String title = rest.substring(0, idx2);
                String pageValue = rest.substring(idx2 + 1);
                Bookmark bookmark = new Bookmark(path, title, parseInt(pageValue, 0));
                bookmarks.addElement(bookmark);
            }
            return bookmarks;
        } catch (RecordStoreNotFoundException e) {
            return bookmarks;
        } catch (Exception e) {
            return bookmarks;
        }
    }

    public static void save(Vector bookmarks) {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < bookmarks.size(); i++) {
            Bookmark bookmark = (Bookmark) bookmarks.elementAt(i);
            if (bookmark == null) {
                continue;
            }
            buffer.append(bookmark.getBookPath()).append('|')
                   .append(bookmark.getTitle()).append('|')
                   .append(bookmark.getPage()).append('\n');
        }
        byte[] data = Utf8.encode(buffer.toString());
        try {
            RecordStore rs = RecordStore.openRecordStore(STORE_NAME, true);
            if (rs.getNumRecords() == 0) {
                rs.addRecord(data, 0, data.length);
            } else {
                rs.setRecord(1, data, 0, data.length);
            }
            rs.closeRecordStore();
        } catch (RecordStoreException e) {
        }
    }

    public static void add(Bookmark bookmark) {
        Vector bookmarks = load();
        bookmarks.addElement(bookmark);
        save(bookmarks);
    }

    public static boolean toggle(String bookPath, String title, int page) {
        Vector bookmarks = load();
        for (int i = 0; i < bookmarks.size(); i++) {
            Bookmark bookmark = (Bookmark) bookmarks.elementAt(i);
            if (bookPath.equals(bookmark.getBookPath()) && page == bookmark.getPage()) {
                bookmarks.removeElementAt(i);
                save(bookmarks);
                return false;
            }
        }
        bookmarks.addElement(new Bookmark(bookPath, title, page));
        save(bookmarks);
        return true;
    }

    public static int findPage(String bookPath) {
        Vector bookmarks = load();
        for (int i = 0; i < bookmarks.size(); i++) {
            Bookmark bookmark = (Bookmark) bookmarks.elementAt(i);
            if (bookPath.equals(bookmark.getBookPath())) {
                return bookmark.getPage();
            }
        }
        return -1;
    }

    private static String[] split(String value) {
        if (value == null || value.length() == 0) {
            return new String[0];
        }
        Vector lines = new Vector();
        int start = 0;
        for (int i = 0; i <= value.length(); i++) {
            if (i == value.length() || value.charAt(i) == '\n') {
                String segment = value.substring(start, i);
                if (segment.length() > 0) {
                    lines.addElement(segment);
                }
                start = i + 1;
            }
        }
        String[] result = new String[lines.size()];
        for (int i = 0; i < lines.size(); i++) {
            result[i] = (String) lines.elementAt(i);
        }
        return result;
    }

    private static int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return fallback;
        }
    }
}
