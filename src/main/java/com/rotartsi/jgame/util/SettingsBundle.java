package com.rotartsi.jgame.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;

import static com.rotartsi.jgame.Constants.JGameStr;
import static com.rotartsi.jgame.util.StringManager.fmt;

/**
 * Class for reading and writing settings in the .properties format.
 */
public class SettingsBundle {
    /**
     * The internal properties object
     */
    private Properties prop;

    private Logger logger = LogManager.getLogger(SettingsBundle.class);

    /**
     * URL to read/write to.
     */
    private URL url;

    /**
     * Load settings from file
     *
     * @param file File to load (from resources root using the class loader)
     * @throws IOException Stream opening may fail
     */
    public SettingsBundle(String file) throws IOException {
        url = this.getClass().getClassLoader().getResource(file);
        prop = new Properties();

        if (url != null) {
            InputStream in = url.openStream();
            prop.load(in);
        } else {
            logger.warn(fmt(JGameStr.getString("util.SettingsBundle.nullURL"), file));
        }
    }

    /**
     * Create an empty SettingsBundle
     */
    public SettingsBundle() {
        prop = new Properties();
    }

    /**
     * Load settings from a file. If it doesn't exist, create an empty file.
     *
     * @param file File to load
     * @throws IOException Stream opening may fail
     */
    public SettingsBundle(File file) throws IOException {
        file.getParentFile().mkdirs();
        file.createNewFile();
        url = file.toURI().toURL();
        prop = new Properties();
        InputStream in = url.openStream();
        prop.load(in);
    }

    /**
     * Get a property from a string.
     *
     * @param key Key to load
     * @return The resource. '!MISSING RESOURCE: ${key}!' is returned if resource cannot be loaded.
     */
    public String get(String key) {
        if (prop != null) {
            String ret = prop.getProperty(key);
            if (ret != null) {
                return ret;
            }
        }
        logger.warn(fmt("Missing resource from settings bundle: %s", key));
        return fmt("!MISSING RESOURCE: %s!", key);
    }

    public String get(String key, String def) {
        if (prop != null) {
            String ret = prop.getProperty(key);
            if (ret != null) {
                return ret;
            }
        }
        logger.warn(fmt("Missing resource: %s. Returning default of %s", key, def));
        return def;
    }

    /**
     * Set a key in the internal properties.
     *
     * @param key   Key
     * @param value New value.
     */
    public void set(String key, String value) {
        if (prop != null) {
            prop.setProperty(key, value);
        }
    }

    /**
     * Override the file with the contents of the current properties object.
     *
     * @throws URISyntaxException Conversion from url to URI may fail.
     * @throws IOException        Writing to file may fail.
     */
    public void save() throws URISyntaxException, IOException {
        if (prop != null) {
            prop.store(new FileOutputStream(new File(url.toURI())), null);
        }
    }
}
