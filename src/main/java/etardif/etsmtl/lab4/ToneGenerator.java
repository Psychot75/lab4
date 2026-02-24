package etardif.etsmtl.lab4;

import javax.sound.sampled.*;

/**
 * Generates short sine-wave tones mapped to array values.
 * Value 1 → low pitch, value 100 → high pitch.
 */
public class ToneGenerator {

    private static final float SAMPLE_RATE = 44100f;
    private static final double MIN_FREQ = 200;
    private static final double MAX_FREQ = 1400;
    private static final int DURATION_MS = 50;

    private SourceDataLine line;
    private boolean enabled = true;
    private volatile double volume = 1.0;

    public ToneGenerator() {
        try {
            AudioFormat format = new AudioFormat(SAMPLE_RATE, 8, 1, true, false);
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
            line = (SourceDataLine) AudioSystem.getLine(info);
            line.open(format, 4096);
            line.start();
        } catch (LineUnavailableException e) {
            line = null;
        }
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /** Set volume from 0.0 (silent) to 1.0 (full). */
    public void setVolume(double volume) {
        this.volume = Math.max(0.0, Math.min(1.0, volume));
    }

    /**
     * Play a tone for a given array value (1-100) and maxValue.
     * Called from background sort threads.
     */
    public void play(int value, int maxValue) {
        if (!enabled || line == null) return;

        double ratio = (value - 1.0) / Math.max(maxValue - 1, 1);
        double freq = MIN_FREQ + ratio * (MAX_FREQ - MIN_FREQ);

        int samples = (int) (SAMPLE_RATE * DURATION_MS / 1000);
        byte[] buf = new byte[samples];
        for (int i = 0; i < samples; i++) {
            double angle = 2.0 * Math.PI * freq * i / SAMPLE_RATE;
            // Fade in/out to avoid clicks
            double envelope = 1.0;
            int fadeLen = samples / 5;
            if (i < fadeLen) envelope = (double) i / fadeLen;
            else if (i > samples - fadeLen) envelope = (double) (samples - i) / fadeLen;
            buf[i] = (byte) (Math.sin(angle) * 80 * envelope * volume);
        }

        line.write(buf, 0, buf.length);
    }

    public void close() {
        if (line != null) {
            line.drain();
            line.close();
        }
    }
}
