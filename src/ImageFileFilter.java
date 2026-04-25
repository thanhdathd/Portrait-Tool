import java.io.File;
import java.io.FilenameFilter;
import userpackage.Noitifier;

public class ImageFileFilter implements FilenameFilter {
    String[] list;

    public ImageFileFilter(String[] list) {
        this.list = list;
    }

    public boolean accept(File dir, String name) {
        boolean re = false;

        for(int i = 0; i < this.list.length; ++i) {
            if (name.endsWith("." + this.list[i])) {
                re = true;
                Noitifier.printConsole("filter called: " + re + " - " + name);
                return re;
            }
        }

        Noitifier.printConsole("filter called: " + re + " - " + name);
        return false;
    }
}