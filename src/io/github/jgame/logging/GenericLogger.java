package io.github.jgame.logging;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.logging.*;

public class GenericLogger {
    private static final Logger logger = Logger.getLogger("GenericLogger");
    private static boolean setupDone = false;

    public static void setup(Level CONSOLE_LEVEL, Level LATEST_LEVEL, Level LOG_LEVEL) {
        if (setupDone) {
            return;
        }
        File logDir = new File("logs");
        if (!logDir.exists()) {
            logDir.mkdirs();
        }
        Logger root = Logger.getLogger("");
        for (Handler h : root.getHandlers()) {
            root.removeHandler(h);
        }
        root.setLevel(Level.ALL);
        GenericFormatter formatterTxt = new GenericFormatter();

        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setFormatter(formatterTxt);
        consoleHandler.setLevel(CONSOLE_LEVEL);
        root.addHandler(consoleHandler);
        try {
            FileHandler fileHandler = new FileHandler("logs/latest.log");
            fileHandler.setFormatter(formatterTxt);
            fileHandler.setLevel(LATEST_LEVEL);
            root.addHandler(fileHandler);
        } catch (IOException e) {
            logger.severe("Failed to add handler: \n" + getStackTrace(e));
        }
        try {
            if (LOG_LEVEL != Level.OFF) {
                FileHandler logFileHandler = new FileHandler("logs/" + new Date().toString() + ".log");
                logFileHandler.setFormatter(formatterTxt);
                logFileHandler.setLevel(LOG_LEVEL);
                root.addHandler(logFileHandler);
            }
        } catch (IOException e) {
            logger.severe("Failed to add handler: \n" + getStackTrace(e));
        }
        setupDone = true;
    }

    public static String getStackTrace(Throwable e) {
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw, true);
        e.printStackTrace(pw);
        return sw.getBuffer().toString();
    }
}
