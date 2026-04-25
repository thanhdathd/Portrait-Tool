package core.history;

import ui.canvas.ImageCanvas;

import java.awt.image.BufferedImage;

public class FilterCommand implements Command {

    private final ImageCanvas canvas;
    private final BufferedImage oldImage;
    private final BufferedImage newImage;

    public FilterCommand(ImageCanvas canvas, BufferedImage oldImage, BufferedImage newImage) {
        this.canvas = canvas;
        this.oldImage = oldImage;
        this.newImage = newImage;
    }

    @Override
    public void execute() {
        canvas.setBackgroundImage(newImage);
    }

    @Override
    public void undo() {
        canvas.setBackgroundImage(oldImage);
    }
}
