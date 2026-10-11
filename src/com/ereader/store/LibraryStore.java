package com.ereader.store;

import com.ereader.model.Book;
import com.ereader.util.Utf8;
import java.util.Vector;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;
import javax.microedition.rms.RecordStoreNotFoundException;

public final class LibraryStore {
    private static final String STORE_NAME = "EReaderLibrary";

    private LibraryStore() {
    }

    public static Vector load() {
        Vector books = new Vector();
        try {
            RecordStore rs = RecordStore.openRecordStore(STORE_NAME, false);
            if (rs.getNumRecords() == 0) {
                rs.closeRecordStore();
                return books;
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
                int positionIdx = line.lastIndexOf('|');
                if (idx <= 0) {
                    continue;
                }
                String title = line.substring(0, idx);
                String path = positionIdx > idx ? line.substring(idx + 1, positionIdx) : line.substring(idx + 1);
                Book book = new Book(title, path);
                if (positionIdx > idx) {
                    book.setLastPosition(parseInt(line.substring(positionIdx + 1), 0));
                }
                books.addElement(book);
            }
            return books;
        } catch (RecordStoreNotFoundException e) {
            return books;
        } catch (Exception e) {
            return books;
        }
    }

    public static void save(Vector books) {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < books.size(); i++) {
            Book book = (Book) books.elementAt(i);
            if (book == null || book.getPath() == null) {
                continue;
            }
            buffer.append(book.getTitle()).append('|').append(book.getPath()).append('|')
                  .append(book.getLastPosition()).append('\n');
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

    public static void addOrUpdate(Book book) {
        Vector books = load();
        for (int i = 0; i < books.size(); i++) {
            Book current = (Book) books.elementAt(i);
            if (current.getPath().equals(book.getPath())) {
                books.setElementAt(book, i);
                save(books);
                return;
            }
        }
        books.addElement(book);
        save(books);
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
