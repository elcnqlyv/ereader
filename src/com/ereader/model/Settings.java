package com.ereader.model;

public class Settings {
    public static final int THEME_SEPIA = 0;
    public static final int THEME_GRUVBOX = 1;

    public static final int FONT_SMALL = 0;
    public static final int FONT_MEDIUM = 1;
    public static final int FONT_LARGE = 2;

    private int theme;
    private int fontLevel;
    private String rootPath;

    public Settings() {
        theme = THEME_SEPIA;
        fontLevel = FONT_MEDIUM;
        rootPath = "file:///E:/";
    }

    public int getTheme() {
        return theme;
    }

    public void setTheme(int theme) {
        this.theme = theme;
    }

    public int getFontLevel() {
        return fontLevel;
    }

    public void setFontLevel(int fontLevel) {
        this.fontLevel = fontLevel;
    }

    public String getRootPath() {
        return rootPath;
    }

    public void setRootPath(String rootPath) {
        this.rootPath = rootPath;
    }
}
