package mote4.util;

import java.net.URISyntaxException;

/**
 * Utilities for loading configuration and save data.
 */
public class Config {

    public static String getDefaultPath(String key) {
        String OS = System.getProperty("os.name").toLowerCase();

        if (OS.contains("win"))
        {
            String dir = System.getenv("APPDATA") + "/"+key+"/";
            //File file = new File(dir);
            //file.mkdir();
            return dir;
        }
        else if (OS.contains("mac"))
        {
            String dir = System.getProperty("user.home") + "/Library/Application Support/"+key+"/";
            //File file = new File(dir);
            //file.mkdir();
            return dir;
        }
        else if (OS.contains("nix") || OS.contains("nux") || OS.contains("aix"))
        {
            String dir = System.getProperty("user.home") + "/."+key+"/";
            //File file = new File(dir);
            //file.mkdir();
            return dir;
        }
        else return "./";
    }

    public static String getInstallPath() {
        try {
            //File file = new File(Config.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            return Config.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return "./";
    }
}
