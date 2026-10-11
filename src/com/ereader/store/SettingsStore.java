package com.ereader.store;

import com.ereader.model.Settings;
import com.ereader.util.Utf8;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;
import javax.microedition.rms.RecordStoreNotFoundException;

public final class SettingsStore {
    private static final String STORE_NAME = "EReaderSettings";

    private SettingsStore() {
    }

    public static Settings load() {
        Settings settings = new Settings();
        try {
            RecordStore rs = RecordStore.openRecordStore(STORE_NAME, false);
            if (rs.getNumRecords() == 0) {
                rs.closeRecordStore();
                return settings;
            }

            byte[] raw = rs.getRecord(1);
            rs.closeRecordStore();
            String text = Utf8.decode(raw);
            String[] lines = split(text);
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i];
                int p = line.indexOf('=');
                if (p <= 0) {
                    continue;
                }
                String key = line.substring(0, p);
                String value = line.substring(p + 1);
                if ("theme".equals(key)) {
                    settings.setTheme(parseInt(value, Settings.THEME_SEPIA));
                } else if ("fontLevel".equals(key)) {
                    settings.setFontLevel(parseInt(value, Settings.FONT_MEDIUM));
                } else if ("rootPath".equals(key)) {
                    settings.setRootPath(value);
                }
            }
            return settings;
        } catch (RecordStoreNotFoundException e) {
            return settings;
        } catch (Exception e) {
            return settings;
        }
    }

    public static void save(Settings settings) {
        StringBuffer content = new StringBuffer();
        content.append("theme=").append(settings.getTheme()).append('\n');
        content.append("fontLevel=").append(settings.getFontLevel()).append('\n');
        content.append("rootPath=").append(settings.getRootPath()).append('\n');

        byte[] data = Utf8.encode(content.toString());
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

    private static String[] split(String value) {
        if (value == null || value.length() == 0) {
            return new String[0];
        }
        java.util.Vector lines = new java.util.Vector();
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
