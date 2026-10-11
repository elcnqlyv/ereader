package com.ereader.epub;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Hashtable;
import org.kxml2.io.KXmlParser;
import org.xmlpull.v1.XmlPullParser;

public final class OpfParser {
    private OpfParser() {
    }

    public static String firstSpinePath(byte[] opf, String opfPath) throws IOException {
        Hashtable manifest = new Hashtable();
        String spineId = null;
        try {
            KXmlParser parser = new KXmlParser();
            parser.setInput(new ByteArrayInputStream(opf), "UTF-8");
            int event;
            while ((event = parser.next()) != XmlPullParser.END_DOCUMENT) {
                if (event != XmlPullParser.START_TAG) {
                    continue;
                }
                String name = parser.getName();
                if ("item".equalsIgnoreCase(name)) {
                    String id = parser.getAttributeValue(null, "id");
                    String href = parser.getAttributeValue(null, "href");
                    if (id != null && href != null) {
                        manifest.put(id, href);
                    }
                } else if ("itemref".equalsIgnoreCase(name) && spineId == null) {
                    spineId = parser.getAttributeValue(null, "idref");
                }
            }
        } catch (Exception ex) {
            throw new IOException("Invalid OPF package");
        }
        if (spineId == null || !manifest.containsKey(spineId)) {
            throw new IOException("OPF has no readable spine");
        }
        return resolve(opfPath, (String) manifest.get(spineId));
    }

    private static String resolve(String opfPath, String href) {
        int slash = opfPath.lastIndexOf('/');
        if (slash < 0) {
            return href;
        }
        return opfPath.substring(0, slash + 1) + href;
    }
}
