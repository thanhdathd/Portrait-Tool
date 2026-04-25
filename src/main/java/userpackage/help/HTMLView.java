package userpackage.help;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

public class HTMLView extends JPanel {
    private static final long serialVersionUID = 1L;
    protected JEditorPane viewer = new JEditorPane();
    protected JTextField commandLine;

    public HTMLView() {
        this.viewer.setEditable(false);
        this.commandLine = new JTextField();

        try {
            URL url = new URL("file:data/help/help.html");
            this.viewer.setPage(url);
            this.commandLine.setText(url.toExternalForm());
        } catch (MalformedURLException var3) {
            System.out.println("URL khong hop le");
        } catch (IOException var4) {
            System.out.println("khong tim thay file htm");
        }

        this.viewer.addHyperlinkListener(new HyperlinkListener() {
            public void hyperlinkUpdate(HyperlinkEvent evt) {
                HTMLView.this.setURL(evt.getURL());
            }
        });
        this.setLayout(new BorderLayout());
        JScrollPane scr = new JScrollPane(this.viewer);
        this.add(scr, "Center");
        this.add(this.commandLine, "North");
        this.commandLine.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    URL newURL = new URL(e.getActionCommand());
                    HTMLView.this.setURL(newURL);
                } catch (MalformedURLException e1) {
                    e1.printStackTrace();
                }

            }
        });
    }

    protected void setURL(URL url) {
        try {
            this.viewer.setPage(url);
            this.commandLine.setText(url.toExternalForm());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
