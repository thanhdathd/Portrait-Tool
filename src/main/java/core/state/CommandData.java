package core.state;

import core.image.ImageTransformUtils;
import filter.FilterProperties;
import ui.dialogs.ResizeDialog;
import userpackage.SPoint;

import java.awt.*;

public class CommandData {
    public enum CommandType {
        ADD_POINT,
        DELETE_POINT,
        EDIT_POINT,
        BATCH_DELETE_POINTS,
        BATCH_EDIT_POINTS_COLOR,
        ADD_GRID,
        DELETE_GRID,
        EDIT_GRID,
        CROP,
        ROTATE,
        FLIP,
        FILTER,
        RESIZE,
        ADD_LINE,
        DELETE_LINE,
        EDIT_LINE,
        BATCH_DELETE_LINES,
        BATCH_EDIT_LINES
    }

    public CommandType type;

        // Tham số cho các lệnh liên quan đến Point (ADD_POINT, DELETE_POINT) và Grid
        public SPoint point;
        public SPoint newPoint; // Dùng cho lệnh EDIT_GRID / EDIT_POINT

        // Tham số cho lệnh Line
        public userpackage.SLine line;
        public userpackage.SLine newLine;

        // Tham số cho batch point commands
        public java.util.List<SPoint> points;  // danh sách point cho BATCH_DELETE / BATCH_EDIT_COLOR
        public java.awt.Color batchColor;       // màu mới cho BATCH_EDIT_POINTS_COLOR
        public java.util.List<java.awt.Color> oldColors; // màu cũ từng point (cho undo)

        // Tham số cho batch line commands
        public java.util.List<userpackage.SLine> lines;
        public java.util.List<userpackage.SLine> newLines;

        // Tham số cho lệnh CROP
        public Integer cropX;
        public Integer cropY;
        public Integer cropW;
        public Integer cropH;
        public Float zomAtCrop;
        public Integer oldVisualX;
        public Integer oldVisualY;

        // tham số cho lệnh filter
        public FilterProperties filterProps;

        // tham số cho resize
        public ResizeDialog.ResizeProps resizeProps;

        // Tham số cho lệnh transform
        public ImageTransformUtils.TransformType transformType;

        public CommandData() {}

        public static CommandData createPointCmd(SPoint p) {
            CommandData cd = new CommandData();
            cd.type = CommandType.ADD_POINT;
            cd.point = p;
            return cd;
        }

        public static CommandData createGridCmd(SPoint p) {
            CommandData cd = new CommandData();
            cd.type = CommandType.ADD_GRID;
            cd.point = p;
            return cd;
        }

        public static CommandData createCropCmd(Rectangle rect) {
            CommandData cd = new CommandData();
            cd.type = CommandType.CROP;
            cd.cropX = rect.x;
            cd.cropY = rect.y;
            cd.cropW = rect.width;
            cd.cropH = rect.height;
            return cd;
        }
}

