package core.actions;

import javax.swing.*;
import java.awt.event.ActionEvent;

@FunctionalInterface
public interface KeyAction {
    public void perform(ActionEvent e);
}
