package mote4.util.audio;

import mote4.scenegraph.Window;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

/**
 * This class should be temporary!  Rough support for streaming ogg files,
 * will be switched to OpenAL in the future.
 * Created by Peter on 12/6/16.
 */
public class JavaAudio extends Thread {

    private static volatile HashMap<String, JavaAudio> map = new HashMap<>();

    public static void playOgg(String filename) {
        JavaAudio t = new JavaAudio(filename,"ogg");
        t.start();
        map.put(filename, t);
    }
    public static void playWav(String filename) {
        JavaAudio t = new JavaAudio(filename,"wav");
        t.start();
        map.put(filename, t);
    }

    public static void stopAudio(String filename) {
        JavaAudio a = map.get(filename);
        if (a != null) {
            a.stopAudio();
            a.interrupt();
            map.remove(filename);
        }
    }

    private String filename, format;
    public JavaAudio(String filename, String format) {
        this.filename = filename;
        this.format = format;
    }

    @Override
    public void run() {
        switch (format) {
            case "ogg":
                loadOgg(filename);
                break;
            case "wav":
                loadWav(filename);
                break;
        }
    }

    public void stopAudio() {
        switch (format) {
            case "ogg":
                //line.drain();
                line.stop();
                line.close();
                try {
                    din.close();
                    in.close();
                } catch (IOException e) {}
                break;
            case "wav":
                clip.stop();
                break;
        }
    }

    public Clip clip = null;
    private void loadWav(String name) {
        try {
            //Clip clip;
            //if (clip != null && clip.isOpen())
            //    clip.close();

            InputStream is = ClassLoader.class.getResourceAsStream("/res/audio/"+name+".wav");
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(is);
            clip = AudioSystem.getClip();

            clip.open(audioInputStream);
            FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            gainControl.setValue(0f);

            System.out.println(clip.getFrameLength() + " | " + clip.getFramePosition());
            clip.start();
        } catch (UnsupportedAudioFileException | LineUnavailableException | IOException e) {
            e.printStackTrace();
            Window.destroy();
        }
    }

    private AudioInputStream in, din;
    private void loadOgg(String name)
    {
        try
        {
            InputStream is = ClassLoader.class.getResourceAsStream("/res/audio/"+name+".ogg");
            in = AudioSystem.getAudioInputStream(is);
            din = null;
            if (in != null)
            {
                AudioFormat baseFormat = in.getFormat();
                AudioFormat  decodedFormat = new AudioFormat(
                        AudioFormat.Encoding.PCM_SIGNED,
                        baseFormat.getSampleRate(),
                        16,
                        baseFormat.getChannels(),
                        baseFormat.getChannels() * 2,
                        baseFormat.getSampleRate(),
                        false);
                // Get AudioInputStream that will be decoded by underlying VorbisSPI
                din = AudioSystem.getAudioInputStream(decodedFormat, in);
                // Play now !
                rawplay(decodedFormat, din);
                in.close();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private SourceDataLine line;
    private void rawplay(AudioFormat targetFormat,
                         AudioInputStream din) throws IOException, LineUnavailableException
    {
        byte[] data = new byte[4096];
        line = getLine(targetFormat);
        if (line != null)
        {
            // Start
            line.start();
            int nBytesRead = 0, nBytesWritten = 0;
            while (nBytesRead != -1)
            {
                nBytesRead = din.read(data, 0, data.length);
                if (nBytesRead != -1 && line.isOpen()) nBytesWritten = line.write(data, 0, nBytesRead);
            }
            // Stop
            line.drain();
            line.stop();
            line.close();
            din.close();
        }
    }

    private SourceDataLine getLine(AudioFormat audioFormat) throws LineUnavailableException
    {
        SourceDataLine res = null;
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
        res = (SourceDataLine) AudioSystem.getLine(info);
        res.open(audioFormat);
        return res;
    }
}
