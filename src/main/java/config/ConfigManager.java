package config;

import core.state.AppState;

import java.awt.*;
import java.io.*;
import java.util.Properties;

public class ConfigManager {
    private static final String CONFIG_DIR_NAME = ".myapp";
    private static final String CONFIG_FILE_NAME = "config.properties";
    private final File configFile;
    private final CustomCropProfileManager customCropProfileManager = new CustomCropProfileManager();

    public CustomCropProfileManager getCustomCropProfileManager() {
        return customCropProfileManager;
    }

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
            appState.setGridSize(Float.parseFloat(gridSize));
            String gridInCm = props.getProperty("gridInCm", "false");
            appState.setGridInCm(Boolean.parseBoolean(gridInCm));
            String cmUnit = props.getProperty("cmUnit", "false");
            appState.setCmUnit(Boolean.parseBoolean(cmUnit));
            String scale =  props.getProperty("scale", "1.0");
            appState.setScale(Float.parseFloat(scale));
            String vLang =  props.getProperty("viLang", "false");
            appState.setViLang(Boolean.parseBoolean(vLang));
            String round =  props.getProperty("round", "false");
            appState.setRound(Boolean.parseBoolean(round));
            String showHelp =  props.getProperty("showHelp", "false");
            appState.setShowCropHelp(Boolean.parseBoolean(showHelp));
            String stackSize = props.getProperty("stackSize", "15");
            appState.setStackSize(Integer.parseInt(stackSize));
            String checkerSize = props.getProperty("checkerSize", "40");
            appState.setCheckerSize(Integer.parseInt(checkerSize));


            // Load vị trí và kích thước cửa sổ
            String windowX = props.getProperty("windowX", "100");
            String windowY = props.getProperty("windowY", "100");
            String windowWidth = props.getProperty("windowWidth", "1000");
            String windowHeight = props.getProperty("windowHeight", "750");
            String zoomWindowBounds = props.getProperty("zoomWindowBounds", "");

            String lastOpenedFile =  props.getProperty("lastOpenedFile", "");
            String lastOpenedDir =  props.getProperty("lastOpenedDir", "~/");
            appState.setFilePath(lastOpenedFile);
            appState.setLastOpenedDir(lastOpenedDir);
            appState.setWindowX(Integer.parseInt(windowX));
            appState.setWindowY(Integer.parseInt(windowY));
            appState.setWindowWidth(Integer.parseInt(windowWidth));
            appState.setWindowHeight(Integer.parseInt(windowHeight));
            appState.setStringZoomWindowBounds(zoomWindowBounds);

            customCropProfileManager.load(props);

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
        props.setProperty("gridInCm", String.valueOf(appState.isGridInCm()));
        props.setProperty("scale", String.valueOf(appState.getScale()));
        props.setProperty("cmUnit", String.valueOf(appState.isCmUnit()));
        props.setProperty("viLang", String.valueOf(appState.isViLang()));
        props.setProperty("round", String.valueOf(appState.isRound()));
        props.setProperty("showHelp", String.valueOf(appState.isShowCropHelp()));
        props.setProperty("stackSize", String.valueOf(appState.getStackSize()));
        props.setProperty("checkerSize", String.valueOf(appState.getCheckerSize()));

        // Có thể thêm: vị trí cửa sổ, kích thước, lần mở file gần nhất, tool đang dùng...
        props.setProperty("lastOpenedFile", appState.getFilePath());
        props.setProperty("lastOpenedDir", appState.getLastOpenedDir());
        props.setProperty("windowX", String.valueOf(appState.getWindowX()));
        props.setProperty("windowY", String.valueOf(appState.getWindowY()));
        props.setProperty("windowWidth", String.valueOf(appState.getWindowWidth()));
        props.setProperty("windowHeight", String.valueOf(appState.getWindowHeight()));
        props.setProperty("zoomWindowBounds", appState.getStringZoomWindowBounds());

        customCropProfileManager.save(props);

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

    /**
     * Persist ONLY the custom crop profiles without requiring a full AppState.
     * Reads the existing config file, patches the crop entries, and writes back.
     */
    public void saveCropProfiles() {
        Properties props = new Properties();
        // Load current content to avoid wiping other settings
        if (configFile.exists()) {
            try (FileInputStream fis = new FileInputStream(configFile)) {
                props.load(fis);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        customCropProfileManager.save(props);
        try (FileOutputStream fos = new FileOutputStream(configFile)) {
            props.store(fos, "MyApp Configuration");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
