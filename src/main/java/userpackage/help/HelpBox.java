package userpackage.help;

import java.awt.Dialog;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class HelpBox extends Dialog {
    private static final long serialVersionUID = 1L;
    public Dialog box;
    private HTMLView htmlview;

    public HelpBox(Frame f, String title, boolean isModal) {
        super(f);
        this.box = new Dialog(f, title, isModal);
        this.htmlview = new HTMLView();
        this.creatGUI();
    }

    private void creatGUI() {
        this.box.setBounds(300, 35, 850, 600);
        this.box.add(this.htmlview);
        this.box.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent evt) {
                HelpBox.this.box.dispose();
            }
        });
    }
}
