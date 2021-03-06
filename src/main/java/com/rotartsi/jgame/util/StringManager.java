package com.rotartsi.jgame.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

/**
 * Safe access to resource bundles.
 */
public class StringManager {
    /**
     * The internal resource bundle
     */
    private ResourceBundle bundle;

    /**
     * The internal logger object used for logging.
     */
    private Logger logger = LogManager.getLogger(StringManager.class);

    /**
     * The strings to load in place of a missing resource.
     */
    private String[] missingResource = {"!![Missing Resource] %s_%s: %s!!", "!!%s_%s: %s!!"};

    /**
     * Load strings from a resource bundle.
     *
     * @param baseName Bundle to load
     * @param locale   Language to load.
     * @throws ExceptionInInitializerError The bundle may fail to load.
     */
    public StringManager(String baseName, Locale locale) throws ExceptionInInitializerError {
        bundle = ResourceBundle.getBundle(baseName, locale);
        try {
            missingResource = new String[]{bundle.getString("missingResource.log"),
                    bundle.getString("missingResource.ret")};
        } catch (MissingResourceException e) {
            // Ignore this
        }
    }

    public StringManager() {
        bundle = null;
    }

    /**
     * Safe string formatting.
     *
     * @param format String format
     * @param args The format args.
     * @return Formatted string
     */
    public static String fmt(String format, Object... args) {
        try {
            return String.format(format, args);
        } catch (MissingFormatArgumentException e) {
            e.printStackTrace();
            String fmtArgs = Arrays.toString(args);
            return String.format("fmt(\"%s\", %s)", format, fmtArgs.substring(1, fmtArgs.length() - 1));
        }
    }

    /**
     * retrieve string from the resource bundle.
     * Return placeholder if the resource is missing.
     *
     * @param key Key to retrieve.
     * @return String
     */
    public String getString(String key) {
        if (bundle != null) {
            try {
                return bundle.getString(key);
            } catch (MissingResourceException e) {
                // Ignore. Let it fall through
            }
            Object[] fmtArgs = {bundle.getBaseBundleName(), bundle.getLocale().toString(), key};
            logger.warn(fmt(missingResource[0], fmtArgs));
            return fmt(missingResource[1], fmtArgs);
        }
        return "";
    }

    public String getString(String key, String def) {
        if (bundle != null) {
            try {
                return bundle.getString(key);
            } catch (MissingResourceException e) {
                // Ignore. Let it fall through
            }
        }
        logger.warn(fmt("Missing resource: %s. Returning default of %s", key, def));
        return def;
    }
}
