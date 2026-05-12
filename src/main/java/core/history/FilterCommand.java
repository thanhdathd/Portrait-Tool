package core.history;

import core.state.CommandData;
import filter.FilterProperties;
import ui.canvas.ImageCanvas;

import java.awt.image.BufferedImage;

public class FilterCommand implements Command {

    private final ImageCanvas canvas;
    private final BufferedImage oldImage;
    private final BufferedImage newImage;
    private final FilterProperties props;

    public FilterCommand(ImageCanvas canvas, BufferedImage oldImage,
                         BufferedImage newImage, FilterProperties props) {
        this.canvas = canvas;
        this.oldImage = oldImage;
        this.newImage = newImage;
        this.props = props;
    }

    @Override
    public void execute() {
        canvas.setBackgroundImage(newImage);
    }

    @Override
    public void undo() {
        canvas.setBackgroundImage(oldImage);
    }

    @Override
    public CommandData capture() {
        CommandData cmd = new CommandData();
        cmd.type = CommandData.CommandType.FILTER;
        cmd.filterProps = props;
        return cmd;
    }

    @Override
    public long getMemorySize() {
        long size = 0;
        if (oldImage != null) size += (long) oldImage.getWidth() * oldImage.getHeight() * 4;
        if (newImage != null) size += (long) newImage.getWidth() * newImage.getHeight() * 4;
        return size + 1024;
    }
}
