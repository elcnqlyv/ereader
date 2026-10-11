package com.ereader.ui;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.TextBox;
import javax.microedition.lcdui.TextField;

public class SearchCanvas extends TextBox implements CommandListener {
    public interface SearchListener {
        void search(String query);
        void cancelSearch();
    }

    private SearchListener listener;

    public SearchCanvas() {
        super("Search", "", 64, TextField.ANY);
        addCommand(new Command("Find", Command.OK, 1));
        addCommand(new Command("Cancel", Command.BACK, 2));
        setCommandListener(this);
    }

    public void setSearchListener(SearchListener listener) {
        this.listener = listener;
    }

    public void commandAction(Command command, Displayable displayable) {
        if (command.getCommandType() == Command.OK) {
            if (listener != null) {
                listener.search(getString());
            }
        } else if (listener != null) {
            listener.cancelSearch();
        }
    }
}
