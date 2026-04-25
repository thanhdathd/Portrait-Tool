package userpackage;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import userpackage.ConfirmBox.ConfirmState;

class ConfirmListener implements ActionListener {
    private ConfirmBox d;

    public ConfirmListener(ConfirmBox d) {
        this.d = d;
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == this.d.save) {
            this.d.con = ConfirmState.OK;
        }

        if (e.getSource() == this.d.cancel) {
            this.d.con = ConfirmState.CANCEL;
        }

        if (e.getSource() == this.d.dontSave) {
            this.d.con = ConfirmState.DONT_SAVE;
        }

        this.d.setVisible(false);
    }
}
