package com.ereader.epub;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.kxml2.io.KXmlParser;
import org.xmlpull.v1.XmlPullParser;

public final class ContainerParser {
    private ContainerParser() {
    }

    public static String findRootfile(byte[] xml) throws IOException {
        try {
            KXmlParser parser = new KXmlParser();
            parser.setInput(new ByteArrayInputStream(xml), "UTF-8");
            int event;
            while ((event = parser.next()) != XmlPullParser.END_DOCUMENT) {
                if (event == XmlPullParser.START_TAG
                        && "rootfile".equals(parser.getName())) {
                    String path = parser.getAttributeValue(null, "full-path");
                    if (path != null && path.length() > 0) {
                        return path;
                    }
                }
            }
            throw new IOException("container.xml has no rootfile");
        } catch (Exception ex) {
            if (ex instanceof IOException) {
                throw (IOException) ex;
            }
            throw new IOException("Invalid EPUB container.xml");
        }
    }
}
