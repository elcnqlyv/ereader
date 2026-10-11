package com.ereader.epub;

import com.ereader.util.Utf8;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;
import javax.microedition.io.Connector;
import javax.microedition.io.InputConnection;

public final class EpubReader {
    private EpubReader() {
    }

    public static String readFirstChapter(String epubPath) throws IOException {
        String opfPath = readOpfPath(epubPath);
        byte[] opf = readRequired(epubPath, opfPath, "EPUB package file not found");
        String chapterPath = OpfParser.firstSpinePath(opf, opfPath);
        byte[] chapter = readRequired(epubPath, chapterPath, "EPUB chapter not found");
        return XhtmlStripper.extract(Utf8.decode(chapter));
    }

    public static String readBook(String epubPath) throws IOException {
        String opfPath = readOpfPath(epubPath);
        byte[] opf = readRequired(epubPath, opfPath, "EPUB package file not found");
        String[] chapters = OpfParser.spinePaths(opf, opfPath);
        Hashtable chapterData = readEntries(epubPath, chapters);
        StringBuffer book = new StringBuffer();
        for (int i = 0; i < chapters.length; i++) {
            byte[] chapter = (byte[]) chapterData.get(chapters[i]);
            if (chapter == null) {
                continue;
            }
            if (book.length() > 0) {
                book.append("\n\n");
            }
            book.append(XhtmlStripper.extract(Utf8.decode(chapter)));
        }
        if (book.length() == 0) {
            throw new IOException("EPUB has no readable chapters");
        }
        return book.toString();
    }

    public static TocEntry[] readTableOfContents(String epubPath) throws IOException {
        String opfPath = readOpfPath(epubPath);
        byte[] opf = readRequired(epubPath, opfPath, "EPUB package file not found");
        String[] chapters = OpfParser.spinePaths(opf, opfPath);
        Hashtable chapterData = readEntries(epubPath, chapters);
        Vector entries = new Vector();
        int offset = 0;
        for (int i = 0; i < chapters.length; i++) {
            byte[] chapter = (byte[]) chapterData.get(chapters[i]);
            if (chapter == null) {
                continue;
            }
            String text = XhtmlStripper.extract(Utf8.decode(chapter));
            entries.addElement(new TocEntry(firstHeading(text, chapterTitle(chapters[i])),
                    offset));
            if (offset > 0) {
                offset += 2;
            }
            offset += text.length();
        }
        TocEntry[] result = new TocEntry[entries.size()];
        entries.copyInto(result);
        return result;
    }

    private static String readOpfPath(String epubPath) throws IOException {
        Hashtable metadata = readEntries(epubPath, new String[] {"META-INF/container.xml"});
        byte[] container = (byte[]) metadata.get("META-INF/container.xml");
        if (container == null) {
            throw new IOException("EPUB container.xml not found");
        }
        return ContainerParser.findRootfile(container);
    }

    private static byte[] readRequired(String epubPath, String name, String message)
            throws IOException {
        Hashtable values = readEntries(epubPath, new String[] {name});
        byte[] value = (byte[]) values.get(name);
        if (value == null) {
            throw new IOException(message);
        }
        return value;
    }

    private static Hashtable readEntries(String epubPath, String[] names)
            throws IOException {
        InputConnection connection = (InputConnection) Connector.open(epubPath, Connector.READ);
        try {
            return ZipReader.readEntries(connection, names);
        } finally {
            connection.close();
        }
    }

    private static String firstHeading(String text, String fallback) {
        String[] lines = splitLines(text);
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].trim().length() > 0 && lines[i].length() < 100) {
                return lines[i].trim();
            }
        }
        return fallback;
    }

    private static String chapterTitle(String path) {
        int slash = path.lastIndexOf('/');
        String value = slash >= 0 ? path.substring(slash + 1) : path;
        int dot = value.lastIndexOf('.');
        return dot > 0 ? value.substring(0, dot) : value;
    }

    private static String[] splitLines(String text) {
        Vector lines = new Vector();
        int start = 0;
        for (int i = 0; i <= text.length(); i++) {
            if (i == text.length() || text.charAt(i) == '\n') {
                lines.addElement(text.substring(start, i));
                start = i + 1;
            }
        }
        String[] result = new String[lines.size()];
        lines.copyInto(result);
        return result;
    }
}
