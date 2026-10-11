package com.ereader.epub;

public final class TocEntry {
    private final String title;
    private final int characterOffset;

    public TocEntry(String title, int characterOffset) {
        this.title = title;
        this.characterOffset = characterOffset;
    }

    public String getTitle() {
        return title;
    }

    public int getCharacterOffset() {
        return characterOffset;
    }
}
