import userpackage.EditPanel;
import userpackage.ImageScrollPane;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class LayoutUpdater implements Runnable {
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private Future<?> task;

    private volatile boolean running = false;
    int count = 0;
    int dlayTime = 500;

    private ImageScrollPane imgScrollPane;
    private StickyPoint stickypoint;
    private EditPanel editPanel;

    public void setLayoutContent(ImageScrollPane i) {
        this.imgScrollPane = i;
    }

    public void setMasterContent(StickyPoint stp) {
        this.stickypoint = stp;
    }

    public void setEditPanel(EditPanel editPanel) {
        this.editPanel = editPanel;
    }

    public void setIterval(int imgSize) {
        if (imgSize < 250000) {
            this.dlayTime = 500;
        } else {
            this.dlayTime = (int)(20.0F * (float)imgSize / 250000.0F);
        }

        System.out.println("setdInterVal :" + imgSize);
    }

    public void start() {
        // nếu đang chạy thì stop trước
        stop();

        running = true;
        count = 0;

        task = executor.submit(this);
    }

    public void stop() {
        running = false;

        if (task != null) {
            task.cancel(true); // interrupt thread nếu đang sleep
        }
    }

    public void run() {
        try {
            while(running && this.count < 2) {
                SwingUtilities.invokeAndWait(() -> {
                    stickypoint.floattingScrPanel();
                    imgScrollPane.doLayout();
                });
                ++count;
                System.out.println("Layout update :" + count + "; dlay:" + dlayTime);


                Thread.sleep((long)dlayTime);

            }

            if (this.imgScrollPane.getWidth() > this.editPanel.getWidth() && this.imgScrollPane.getHeight() > this.editPanel.getHeight()) {
                this.stickypoint.fixed();
            }
        } catch (InterruptedException e) {
            // bị stop() gọi cancel(true)
            Thread.currentThread().interrupt();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } finally {
            running = false;
        }
    }

    // gọi khi app đóng hẳn
    public void shutdown() {
        executor.shutdownNow();
    }
}
