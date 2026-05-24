package utils;

import java.io.InputStream;
import java.util.Properties;

public class BuildInfo {

    private static final Properties appProps = new Properties();
    private static final Properties gitProps = new Properties();

    static {

        try (InputStream is =
                     BuildInfo.class.getResourceAsStream("/app.properties")) {

            if (is != null) {
                appProps.load(is);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try (InputStream is =
                     BuildInfo.class.getResourceAsStream("/git.properties")) {

            if (is != null) {
                gitProps.load(is);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getVersion() {
        return appProps.getProperty("app.version", "unknown");
    }

    public static String getCommit() {
        return gitProps.getProperty("git.commit.id.abbrev", "unknown");
    }

    public static String getBuildCount() {
        return gitProps.getProperty("git.total.commit.count", "unknown");
    }

    public static String getBuildTime() {
        return gitProps.getProperty("git.build.time", "unknown");
    }

    public static String getBranch() {
        return gitProps.getProperty("git.branch", "unknown");
    }
}
