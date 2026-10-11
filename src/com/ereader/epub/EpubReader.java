package com.ereader.epub;

import com.ereader.util.Utf8;
import java.io.IOException;
import javax.microedition.io.Connector;
import javax.microedition.io.InputConnection;
import java.util.Hashtable;

public final class EpubReader {
    private EpubReader() {
    }

    public static String readFirstChapter(String epubPath) throws IOException {
        InputConnection connection = (InputConnection) Connector.open(epubPath, Connector.READ);
        try {
            Hashtable metadata = ZipReader.readEntries(connection,
                    new String[] {"META-INF/container.xml"});
            byte[] container = (byte[]) metadata.get("META-INF/container.xml");
        if (container == null) {
            throw new IOException("EPUB container.xml not found");
        }
        String opfPath = ContainerParser.findRootfile(container);
        Hashtable packageData = ZipReader.readEntries(connection,
                new String[] {opfPath});
        byte[] opf = (byte[]) packageData.get(opfPath);
        if (opf == null) {
            throw new IOException("EPUB package file not found");
        }
        String chapterPath = OpfParser.firstSpinePath(opf, opfPath);
        Hashtable chapterData = ZipReader.readEntries(connection,
                new String[] {chapterPath});
        byte[] chapter = (byte[]) chapterData.get(chapterPath);
        if (chapter == null) {
            throw new IOException("EPUB chapter not found");
        }
        return XhtmlStripper.extract(Utf8.decode(chapter));
        } finally {
            connection.close();
        }
    }

    public static String readBook(String epubPath) throws IOException {
        InputConnection connection = (InputConnection) Connector.open(epubPath, Connector.READ);
        try {
        Hashtable metadata = ZipReader.readEntries(connection,
                new String[] {"META-INF/container.xml"});
        byte[] container = (byte[]) metadata.get("META-INF/container.xml");
        if (container == null) {
            throw new IOException("EPUB container.xml not found");
        }
        String opfPath = ContainerParser.findRootfile(container);
        Hashtable packageData = ZipReader.readEntries(connection,
                new String[] {opfPath});
        byte[] opf = (byte[]) packageData.get(opfPath);
        if (opf == null) {
            throw new IOException("EPUB package file not found");
        }
        String[] chapters = OpfParser.spinePaths(opf, opfPath);
        StringBuffer book = new StringBuffer();
        Hashtable chapterData = ZipReader.readEntries(connection, chapters);
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
        } finally {
            connection.close();
        }
    }
}
