package core.history;

import filter.FilterProperties;
// import userpackage.ImgFrame;

public class FilterCommand implements Command {

    // private final ImgFrame imgFrame;
    private final FilterProperties fp;

    public FilterCommand(/*ImgFrame imgFrame,*/ FilterProperties fp) {
        // this.imgFrame = imgFrame;
        this.fp = fp;
    }

    @Override
    public void execute() {
        // imgFrame.redoFilter(fp);
    }

    @Override
    public void undo() {
        // imgFrame.undoFilter historically required the stack, but we can pass fp or modify it later
        // imgFrame.undoFilterCommand(fp);
    }
}
