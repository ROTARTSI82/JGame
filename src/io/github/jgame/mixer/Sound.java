package io.github.jgame.mixer;

import javax.sound.sampled.*;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import static io.github.jgame.Constants.JGameStr;
import static io.github.jgame.util.StringManager.fmt;

/**
 * Sound object. Can be loaded from files or URLs
 * See {@link Clip}
 */
public class Sound {
    public String name;
    private Clip sound;
    private URL url;
    private Logger logger;

    private HashMap<FloatControl.Type, Float> state;

    /**
     * Load sound from file
     *
     * @param filename File to load
     */
    public Sound(String filename) {
        name = filename;
        logger = Logger.getLogger(this.getClass().getName());
        url = this.getClass().getClassLoader().getResource(filename);

        init();
    }

    /**
     * Load sound from url
     *
     * @param soundURL url to load
     */
    public Sound(URL soundURL) {
        url = soundURL;
        name = soundURL.toString();
        logger = Logger.getLogger(this.getClass().getName());

        init();
    }

    private void init() {
        if (url == null) {
            logger.warning("Null file/url supplied to sound. Dummy sound created.");
            sound = null;
            state = new HashMap<>();
            return;
        }
        tryLoadFromURL();
        state = new HashMap<>() {{
            put(FloatControl.Type.AUX_RETURN, getVal(FloatControl.Type.AUX_RETURN));
            put(FloatControl.Type.AUX_SEND, getVal(FloatControl.Type.AUX_SEND));
            put(FloatControl.Type.BALANCE, getVal(FloatControl.Type.BALANCE));
            put(FloatControl.Type.MASTER_GAIN, getVal(FloatControl.Type.MASTER_GAIN));
            put(FloatControl.Type.PAN, getVal(FloatControl.Type.PAN));
            put(FloatControl.Type.REVERB_RETURN, getVal(FloatControl.Type.REVERB_RETURN));
            put(FloatControl.Type.REVERB_SEND, getVal(FloatControl.Type.REVERB_SEND));
            put(FloatControl.Type.SAMPLE_RATE, getVal(FloatControl.Type.SAMPLE_RATE));
            put(FloatControl.Type.VOLUME, getVal(FloatControl.Type.VOLUME));
        }};
    }

    private void tryLoadFromURL() {
        try {
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(url);
            AudioFormat format = audioIn.getFormat();
            DataLine.Info info = new DataLine.Info(Clip.class, format);
            sound = (Clip) AudioSystem.getLine(info);
            sound.open(audioIn);
        } catch (IOException | LineUnavailableException | UnsupportedAudioFileException e) {
            logger.log(Level.WARNING, fmt(JGameStr.getString("loadFail"), name), e);
        }
    }

    /**
     * Reset sound and prep it for playing over itself. See {@link #play}
     */
    public void reset() {
        if (url == null) {
            return;
        }
        tryLoadFromURL();

        for (FloatControl.Type type : state.keySet()) {
            setVal(state.get(type), type);
        }
    }

    public float getVal(FloatControl.Type type) {
        if (sound == null) {
            return 0f;
        }
        FloatControl control;
        try {
            control = (FloatControl) sound.getControl(type);
        } catch (IllegalArgumentException e) {
            logger.log(Level.WARNING, "Unsupported control type: ", e);
            return 0f;
        }
        float min = control.getMinimum();
        float max = control.getMaximum();
        return (control.getValue() - min) / (max - min);
    }

    /**
     * Is playing
     *
     * @return true if clip is active
     */
    public boolean isPlaying() {
        if (sound == null) {
            return false;
        }
        return sound.isActive();
    }

    public void setVal(float x, FloatControl.Type type) {
        state.put(type, x);
        if (sound == null) {
            return;
        }
        FloatControl control;
        try {
            control = (FloatControl) sound.getControl(type);
        } catch (IllegalArgumentException e) {
            logger.log(Level.WARNING, "Unsupported control type: ", e);
            return;
        }
        float min = control.getMinimum();
        float max = control.getMaximum();
        control.setValue(x * (max - min) + min);
    }

    /**
     * Play sound.
     *
     * With resetting enabled, the sound is allowed to play over itself, but it uses more resources and threads.
     * <ul>
     *     <li>
     *         Instead of interrupting itself if play() is called before the sound is finished
     *     </li>
     * </ul>
     * @param reset Resetting enabled (calls {@link #reset})
     */
    public void play(boolean reset) {
        if (sound != null) {
            if (reset) {
                reset();
            }
            stop();
            skipTo(0);
            sound.start();
        }
    }

    /**
     * Stop the sound. see {@code Clip.stop}
     */
    public void stop() {
        if (sound != null) {
            sound.stop();
        }
    }

    /**
     * Skip to sound to the microseconds position. See {@code Clip.setMicrosecondPosition}
     *
     * @param micros Microsecond position
     */
    public void skipTo(long micros) {
        if (sound != null) {
            sound.setMicrosecondPosition(micros);
        }
    }

    /**
     * Loop sound for {@code num} number of times. -1 will loop indefinitely (any negative number will, but use -1)
     * See {@code Clip.loop}
     * @param num number of loops
     */
    public void loopFor(int num) {
        if (sound != null) {
            sound.loop(num);
        }
    }
}
