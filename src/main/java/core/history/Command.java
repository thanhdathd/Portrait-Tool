package core.history;

import core.state.CommandData;

public interface Command {
    void execute();
    void undo();
    CommandData capture();
}
