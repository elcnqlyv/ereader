package com.ereader.epub;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;
import org.kxml2.io.KXmlParser;
import org.xmlpull.v1.XmlPullParser;

public final class OpfParser {
    private OpfParser() {
    }

    public static String firstSpinePath(byte[] opf, String opfPath) throws IOException {
        String[] paths = spinePaths(opf, opfPath);
        if (paths.length == 0) {
            throw new IOException("OPF has no readable spine");
        }
        return paths[0];
    }

    public static String[] spinePaths(byte[] opf, String opfPath) throws IOException {
        Hashtable manifest = new Hashtable();
        Vector spineIds = new Vector();
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
                } else if ("itemref".equalsIgnoreCase(name)) {
                    String idref = parser.getAttributeValue(null, "idref");
                    if (idref != null) {
                        spineIds.addElement(idref);
                    }
                }
            }
        } catch (Exception ex) {
            throw new IOException("Invalid OPF package");
        }
        if (spineIds.size() == 0) {
            throw new IOException("OPF has no readable spine");
        }
        Vector paths = new Vector();
        for (int i = 0; i < spineIds.size(); i++) {
            String href = (String) manifest.get(spineIds.elementAt(i));
            if (href != null) {
                paths.addElement(resolve(opfPath, href));
            }
        }
        String[] result = new String[paths.size()];
        for (int i = 0; i < paths.size(); i++) {
            result[i] = (String) paths.elementAt(i);
        }
        return result;
    }

    private static String resolve(String opfPath, String href) {
        int slash = opfPath.lastIndexOf('/');
        if (slash < 0) {
            return href;
        }
        return opfPath.substring(0, slash + 1) + href;
    }
}
