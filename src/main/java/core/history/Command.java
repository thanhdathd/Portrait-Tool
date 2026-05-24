package core.history;

import core.state.CommandData;

public interface Command {
    void execute();
    void undo();
    CommandData capture();
    /**
     * @return Estimated memory size in bytes.
     */
    long getMemorySize();
}
