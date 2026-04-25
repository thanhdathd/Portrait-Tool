package userpackage;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Panel;
import javax.swing.JButton;
import javax.swing.JLabel;

public class ConfirmBox {
    Dialog confirm;
    public ConfirmState con;
    public JButton save;
    public JButton dontSave;
    public JButton cancel;

    public ConfirmBox(Frame f, String mgs, boolean isModal, boolean viLang) {
        this.con = ConfirmBox.ConfirmState.OK;
        this.confirm = new Dialog(f, "Confirm Save", isModal);
        if (viLang) {
            this.confirm.setTitle("Lưu Tệp Trước Khi Đóng");
        }

        this.confirm.setLayout(new BorderLayout());
        int w = mgs.length();
        if (w < 36) {
            this.confirm.setBounds(550, 250, 350, 150);
        } else {
            this.confirm.setBounds(550, 250, 350 + (w - 36) * 5, 150);
        }

        this.confirm.setModal(true);
        Panel mainPanel = new Panel();
        mainPanel.setLayout(new FlowLayout(1, 15, 15));
        Panel texpanel = new Panel();
        JLabel conmgs = new JLabel(mgs);
        texpanel.add(conmgs);
        mainPanel.add(texpanel);
        Panel btp = new Panel();
        btp.setLayout(new GridLayout(1, 3, 15, 5));
        this.save = new JButton("Save");
        this.dontSave = new JButton("Dont Save");
        this.cancel = new JButton("Cancel");
        this.save.setFocusCycleRoot(true);
        this.save.setForeground(new Color(0, 153, 204, 255));
        this.save.setMnemonic('s');
        this.dontSave.setMnemonic('n');
        this.cancel.setMnemonic('c');
        if (viLang) {
            this.save.setText("Lưu");
            this.dontSave.setText("Không Lưu");
            this.cancel.setText("Hủy");
            this.save.setMnemonic('l');
            this.dontSave.setMnemonic('k');
            this.cancel.setMnemonic('h');
        }

        btp.add(this.save);
        btp.add(this.dontSave);
        btp.add(this.cancel);
        ConfirmListener mylistener = new ConfirmListener(this);
        this.save.addActionListener(mylistener);
        this.dontSave.addActionListener(mylistener);
        this.cancel.addActionListener(mylistener);
        mainPanel.add(btp);
        this.confirm.add(mainPanel);
    }

    public int getState() {
        if (this.con == ConfirmBox.ConfirmState.OK) {
            return 1;
        } else {
            return this.con == ConfirmBox.ConfirmState.DONT_SAVE ? 0 : 3;
        }
    }

    public void setVisible(boolean b) {
        this.confirm.setVisible(b);
    }

    public static enum ConfirmState {
        OK,
        DONT_SAVE,
        CANCEL;
    }
}
