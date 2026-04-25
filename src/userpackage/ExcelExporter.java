package userpackage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import javax.swing.JTable;
import javax.swing.table.TableModel;

class ExcelExporter {
    private String noitify = "nothing";
    private String write_to = "Data saved to ";

    public ExcelExporter() {
    }

    public void exportTable(JTable table, File file) throws IOException {
        TableModel model = table.getModel();
        FileWriter out = new FileWriter(file);

        for(int i = 0; i < model.getColumnCount(); ++i) {
            out.write(model.getColumnName(i) + "\t");
        }

        out.write("\n");

        for(int i = 0; i < model.getRowCount(); ++i) {
            for(int j = 0; j < model.getColumnCount(); ++j) {
                if (j == 0) {
                    out.write(model.getValueAt(i, j) + "\t ");
                } else {
                    out.write(model.getValueAt(i, j) + "\t");
                }
            }

            out.write("\n");
        }

        out.close();
        Noitifier.printConsole(this.write_to + file);
        this.noitify = this.write_to + file;
    }

    public String getNoitify() {
        return this.noitify;
    }

    public void setLanguage(boolean viLang) {
        if (viLang) {
            this.write_to = "Dữ liệu đã lưu vào ";
        } else {
            this.write_to = "Data saved to ";
        }

    }
}
