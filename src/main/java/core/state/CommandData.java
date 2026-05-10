package core.state;

import userpackage.SPoint;

import java.awt.*;

public class CommandData {
        public String type; // "ADD_POINT", "DELETE_POINT", "CROP", "ROTATE", "FLIP"

        // Tham số cho các lệnh liên quan đến Point (ADD_POINT, DELETE_POINT)
        public SPoint point;

        // Tham số cho lệnh CROP
        public Integer cropX;
        public Integer cropY;
        public Integer cropW;
        public Integer cropH;

        // Tham số cho lệnh ROTATE
        public Integer rotateAngle;

        public CommandData() {}

        // Static factory methods để tạo data thuận tiện hơn
        public static CommandData createPointCmd(SPoint p) {
            CommandData cd = new CommandData();
            cd.type = "ADD_POINT";
            cd.point = p;
            return cd;
        }

        public static CommandData createGridCmd(SPoint p) {
            CommandData cd = new CommandData();
            cd.type = "ADD_GRID";
            cd.point = p;
            return cd;
        }

        public static CommandData createCropCmd(Rectangle rect) {
            CommandData cd = new CommandData();
            cd.type = "CROP";
            cd.cropX = rect.x;
            cd.cropY = rect.y;
            cd.cropW = rect.width;
            cd.cropH = rect.height;
            return cd;
        }
}

