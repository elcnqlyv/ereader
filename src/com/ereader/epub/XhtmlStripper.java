package com.ereader.epub;

import java.io.IOException;
import java.io.ByteArrayInputStream;
import org.kxml2.io.KXmlParser;
import org.xmlpull.v1.XmlPullParser;

public final class XhtmlStripper {
    private XhtmlStripper() {
    }

    public static String extract(String xhtml) throws IOException {
        StringBuffer text = new StringBuffer();
        try {
            KXmlParser parser = new KXmlParser();
            parser.setInput(new ByteArrayInputStream(xhtml.getBytes("UTF-8")), "UTF-8");
            int event;
            while ((event = parser.next()) != XmlPullParser.END_DOCUMENT) {
                if (event == XmlPullParser.TEXT || event == XmlPullParser.CDSECT) {
                    appendText(text, parser.getText());
                } else if (event == XmlPullParser.START_TAG) {
                    String name = parser.getName();
                    if ("p".equalsIgnoreCase(name) || "div".equalsIgnoreCase(name)
                            || "br".equalsIgnoreCase(name)
                            || "h1".equalsIgnoreCase(name) || "h2".equalsIgnoreCase(name)
                            || "h3".equalsIgnoreCase(name)) {
                        appendBreak(text);
                    }
                }
            }
            return text.toString();
        } catch (Exception ex) {
            throw new IOException("Invalid XHTML content");
        }
    }

    private static void appendText(StringBuffer output, String value) {
        if (value == null) {
            return;
        }
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            if (ch == '\u2022' || ch == '\u25e6' || ch == '\u25aa'
                    || ch == '\u25cf' || ch == '\u2043') {
                output.append("*");
            } else {
                output.append(ch);
            }
        }
    }

    private static void appendBreak(StringBuffer output) {
        int length = output.length();
        if (length > 0 && output.charAt(length - 1) != '\n') {
            output.append('\n');
        }
    }
}
