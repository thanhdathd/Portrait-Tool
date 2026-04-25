package core.history;

import userpackage.ImgFrame;

public class TransformCommand implements Command {

    private final ImgFrame imgFrame;
    private final int transformType;

    public TransformCommand(ImgFrame imgFrame, int transformType) {
        this.imgFrame = imgFrame;
        this.transformType = transformType;
    }

    @Override
    public void execute() {
        imgFrame.redoTranform(transformType);
    }

    @Override
    public void undo() {
        imgFrame.undoTranForm(transformType);
    }
}
