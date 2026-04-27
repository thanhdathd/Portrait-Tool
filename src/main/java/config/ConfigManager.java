package config;

import core.state.AppState;

import java.awt.*;
import java.io.*;
import java.util.Properties;

public class ConfigManager {
    private static final String CONFIG_DIR_NAME = ".myapp";
    private static final String CONFIG_FILE_NAME = "config.properties";
    private final File configFile;

    public ConfigManager() {
        String userHome = System.getProperty("user.home");
        File configDir = new File(userHome, CONFIG_DIR_NAME);
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
        this.configFile = new File(configDir, CONFIG_FILE_NAME);
    }

    public void load(AppState appState) {
        if (!configFile.exists()) {
            // Dùng giá trị mặc định
            setDefaults(appState);
            return;
        }

        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(configFile)) {
            props.load(fis);

            // Ví dụ: load các giá trị
            String brushColor = props.getProperty("brushColor", "#00FFFF");
            appState.setBrushColor(Color.decode(brushColor));

            String gridSize = props.getProperty("gridSize", "40");
            appState.setGridSize(Integer.parseInt(gridSize));

//            String cmUnit = props.getProperty("cmUnit", "false");
//            appState.getCanvasState().setImageOffsetY(Integer.parseInt(cmUnit));

            // Load vị trí và kích thước cửa sổ
            String windowX = props.getProperty("windowX", "100");
            String windowY = props.getProperty("windowY", "100");
            String windowWidth = props.getProperty("windowWidth", "1000");
            String windowHeight = props.getProperty("windowHeight", "750");

            String lastOpenedFile =  props.getProperty("lastOpenedFile", "");
            String lastOpenedDir =  props.getProperty("lastOpenedDir", "~/");
            appState.setFilePath(lastOpenedFile);
            appState.setLastOpenedDir(lastOpenedDir);
            appState.setWindowX(Integer.parseInt(windowX));
            appState.setWindowY(Integer.parseInt(windowY));
            appState.setWindowWidth(Integer.parseInt(windowWidth));
            appState.setWindowHeight(Integer.parseInt(windowHeight));

        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
            setDefaults(appState);
        }
    }

    public void save(AppState appState) {
        Properties props = new Properties();

        // Lưu các giá trị từ appState
        Color c = appState.getBrushColor();
        props.setProperty("brushColor", String.format("#%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue()));
        props.setProperty("gridSize", String.valueOf(appState.getGridSize()));
        props.setProperty("cmUnit", String.valueOf(appState.isCmUnit()));
        props.setProperty("viLang", String.valueOf(appState.isViLang()));
        props.setProperty("round", String.valueOf(appState.isRound()));

        // Có thể thêm: vị trí cửa sổ, kích thước, lần mở file gần nhất, tool đang dùng...
        props.setProperty("lastOpenedFile", appState.getFilePath());
        props.setProperty("lastOpenedDir", appState.getLastOpenedDir());
        props.setProperty("windowX", String.valueOf(appState.getWindowX()));
        props.setProperty("windowY", String.valueOf(appState.getWindowY()));
        props.setProperty("windowWidth", String.valueOf(appState.getWindowWidth()));
        props.setProperty("windowHeight", String.valueOf(appState.getWindowHeight()));

        try (FileOutputStream fos = new FileOutputStream(configFile)) {
            props.store(fos, "MyApp Configuration");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setDefaults(AppState appState) {
        appState.setCurrentZoom(1.0f);
        appState.setBrushColor(Color.RED);
        appState.setGridSize(40);
        // Các giá trị mặc định khác
    }
}
