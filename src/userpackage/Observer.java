package userpackage;

import java.awt.Component;
import java.awt.Image;
import java.awt.image.ImageObserver;

public class Observer implements ImageObserver {
    Component component;
    boolean echo = false;

    public Observer(Component comp) {
        this.component = comp;
    }

    public void showEcho(boolean e) {
        this.echo = e;
    }

    public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
        if ((infoflags & 1) != 0) {
            Noitifier.printConsole("Image width:" + width);
        }

        if ((infoflags & 2) != 0) {
            Noitifier.printConsole("Image height:" + height);
        }

        if ((infoflags & 8) != 0 && this.echo) {
            Noitifier.printConsole("pixcel coming: (" + x + "," + y + "," + width + "," + height + ")");
        }

        if ((infoflags & 32) != 0) {
            Noitifier.printConsole("Loading Image completed.");
            this.component.repaint();
            return false;
        } else {
            return true;
        }
    }
}
