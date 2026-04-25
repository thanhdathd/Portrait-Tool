package core.history;

import userpackage.ImgFrame;

public class FlipCommand implements Command {

    private final ImgFrame imgFrame;
    private final int flipType; // 0 for H, 1 for V

    public FlipCommand(ImgFrame imgFrame, int flipType) {
        this.imgFrame = imgFrame;
        this.flipType = flipType;
    }

    @Override
    public void execute() {
        imgFrame.FlipImg(flipType);
    }

    @Override
    public void undo() {
        imgFrame.FlipImg(flipType); // Flipping again reverses it
    }
}
