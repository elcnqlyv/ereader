package com.ereader.epub;

import com.ereader.util.Utf8;
import java.io.IOException;

public final class EpubReader {
    private EpubReader() {
    }

    public static String readFirstChapter(String epubPath) throws IOException {
        byte[] container = ZipReader.readEntry(epubPath, "META-INF/container.xml");
        if (container == null) {
            throw new IOException("EPUB container.xml not found");
        }
        String opfPath = ContainerParser.findRootfile(container);
        byte[] opf = ZipReader.readEntry(epubPath, opfPath);
        if (opf == null) {
            throw new IOException("EPUB package file not found");
        }
        String chapterPath = OpfParser.firstSpinePath(opf, opfPath);
        byte[] chapter = ZipReader.readEntry(epubPath, chapterPath);
        if (chapter == null) {
            throw new IOException("EPUB chapter not found");
        }
        return XhtmlStripper.extract(Utf8.decode(chapter));
    }
}
