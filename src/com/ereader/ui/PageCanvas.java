package com.ereader.ui;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.TextBox;
import javax.microedition.lcdui.TextField;

public class PageCanvas extends TextBox implements CommandListener {
    public interface PageListener {
        void goToPage(String value);
        void cancelPage();
    }

    private PageListener listener;

    public PageCanvas() {
        super("Go to page", "", 8, TextField.NUMERIC);
        addCommand(new Command("Go", Command.OK, 1));
        addCommand(new Command("Cancel", Command.BACK, 2));
        setCommandListener(this);
    }

    public void setPageListener(PageListener listener) {
        this.listener = listener;
    }

    public void commandAction(Command command, Displayable displayable) {
        if (command.getCommandType() == Command.OK) {
            if (listener != null) {
                listener.goToPage(getString());
            }
        } else if (listener != null) {
            listener.cancelPage();
        }
    }
}
