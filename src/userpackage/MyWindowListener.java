package userpackage;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;

public class MyWindowListener extends WindowAdapter implements WindowStateListener {
    public void windowStateChanged(WindowEvent e) {
        Noitifier.printConsole(e);
    }
}