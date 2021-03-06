package com.rotartsi.jgame.mixer;

import java.util.HashMap;

/**
 * Note. Basically a frequency to play for x amount of time and wait y amount of time afterwards.
 */
public class Note {
    /**
     * HashMap converts string notes to their frequencies.
     */
    private static HashMap<String, Double> notes = new HashMap<>() {{
        put("REST", 0d);
        put("A#0", 29.14);
        put("A#1", 58.27);
        put("A#2", 116.54);
        put("A#3", 233.08);
        put("A#4", 466.16);
        put("A#5", 932.33);
        put("A#6", 1864.66);
        put("A#7", 3729.3);
        put("A#8", 7458.62);
        put("A0", 27.5);
        put("A1", 55.0);
        put("A2", 110.0);
        put("A3", 220.0);
        put("A4", 440.0);
        put("A5", 880.0);
        put("A6", 1760.0);
        put("A7", 3520.0);
        put("A8", 7040.0);
        put("Ab0", 25.96);
        put("Ab1", 51.91);
        put("Ab2", 103.83);
        put("Ab3", 207.65);
        put("Ab4", 415.3);
        put("Ab5", 830.61);
        put("Ab6", 1661.22);
        put("Ab7", 3322.44);
        put("Ab8", 6644.8);
        put("B0", 30.87);
        put("B1", 61.74);
        put("B2", 123.4);
        put("B3", 246.94);
        put("B4", 493.88);
        put("B5", 987.77);
        put("B6", 1975.5);
        put("B7", 3951.0);
        put("B8", 7902.13);
        put("Bb0", 29.14);
        put("Bb1", 58.27);
        put("Bb2", 116.54);
        put("Bb3", 233.08);
        put("Bb4", 466.16);
        put("Bb5", 932.33);
        put("Bb6", 1864.66);
        put("Bb7", 3729.3);
        put("Bb8", 7458.62);
        put("C#0", 17.32);
        put("C#1", 34.65);
        put("C#2", 69.3);
        put("C#3", 138.59);
        put("C#4", 277.18);
        put("C#5", 554.37);
        put("C#6", 1108.73);
        put("C#7", 2217.46);
        put("C#8", 4434.9);
        put("C0", 16.35);
        put("C1", 32.7);
        put("C2", 65.41);
        put("C3", 130.8);
        put("C4", 261.63);
        put("C5", 523.25);
        put("C6", 1046.5);
        put("C7", 2093.0);
        put("C8", 4186.0);
        put("D#0", 19.45);
        put("D#1", 38.89);
        put("D#2", 77.78);
        put("D#3", 155.56);
        put("D#4", 311.13);
        put("D#5", 622.25);
        put("D#6", 1244.51);
        put("D#7", 2489.02);
        put("D#8", 4978.0);
        put("D0", 18.35);
        put("D1", 36.71);
        put("D2", 73.42);
        put("D3", 146.83);
        put("D4", 293.66);
        put("D5", 587.33);
        put("D6", 1174.6);
        put("D7", 2349.3);
        put("D8", 4698.6);
        put("Db0", 17.32);
        put("Db1", 34.65);
        put("Db2", 69.3);
        put("Db3", 138.59);
        put("Db4", 277.18);
        put("Db5", 554.37);
        put("Db6", 1108.73);
        put("Db7", 2217.46);
        put("Db8", 4434.9);
        put("E0", 20.6);
        put("E1", 41.2);
        put("E2", 82.41);
        put("E3", 164.81);
        put("E4", 329.63);
        put("E5", 659.25);
        put("E6", 1318.5);
        put("E7", 2637.0);
        put("E8", 5274.0);
        put("Eb0", 19.45);
        put("Eb1", 38.89);
        put("Eb2", 77.78);
        put("Eb3", 155.56);
        put("Eb4", 311.13);
        put("Eb5", 622.25);
        put("Eb6", 1244.51);
        put("Eb7", 2489.02);
        put("Eb8", 4978.0);
        put("F#0", 23.12);
        put("F#1", 46.25);
        put("F#2", 92.5);
        put("F#3", 185.0);
        put("F#4", 369.99);
        put("F#5", 739.99);
        put("F#6", 1479.98);
        put("F#7", 2959.96);
        put("F#8", 5919.9);
        put("F0", 21.83);
        put("F1", 43.65);
        put("F2", 87.31);
        put("F3", 174.61);
        put("F4", 349.23);
        put("F5", 698.46);
        put("F6", 1396.9);
        put("F7", 2793.8);
        put("F8", 5587.6);
        put("G#0", 25.96);
        put("G#1", 51.91);
        put("G#2", 103.83);
        put("G#3", 207.65);
        put("G#4", 415.3);
        put("G#5", 830.61);
        put("G#6", 1661.22);
        put("G#7", 3322.44);
        put("G#8", 6644.8);
        put("G0", 24.5);
        put("G1", 49.0);
        put("G2", 98.0);
        put("G3", 196.0);
        put("G4", 392.0);
        put("G5", 783.99);
        put("G6", 1567.9);
        put("G7", 3135.9);
        put("G8", 6271.9);
        put("Gb0", 23.12);
        put("Gb1", 46.25);
        put("Gb2", 92.5);
        put("Gb3", 185.0);
        put("Gb4", 369.99);
        put("Gb5", 739.99);
        put("Gb6", 1479.98);
        put("Gb7", 2959.96);
        put("Gb8", 5919.9);
    }};

    /**
     * Frequency (Hz) to play
     */
    double frequency;

    /**
     * How long to keep playing the specified frequency (holding the note)
     */
    double holdTime;

    /**
     * How long to rest after playing the note
     */
    double restTime;

    /**
     * Volume (multiplier)
     */
    float noteVolume;

    /**
     * Notes!
     *
     * @param name   String note
     * @param hold   Time to hold note in seconds
     * @param rest   Time to rest in seconds
     * @param volume Volume. Usually 100
     */
    public Note(String name, double hold, double rest, float volume) {
        if (notes.containsKey(name)) {
            frequency = notes.get(name);
        } else {
            frequency = 0;
        }
        noteVolume = volume;
        holdTime = hold;
        restTime = rest;
    }

    /**
     * Note with frequency instead of string name
     *
     * @param freq Frequency
     * @param hold Time to hold in seconds
     * @param rest Time to rest in seconds
     * @param volume Default to 100
     */
    public Note(double freq, double hold, double rest, float volume) {
        frequency = freq;
        noteVolume = volume;
        holdTime = hold;
        restTime = rest;
    }
}
