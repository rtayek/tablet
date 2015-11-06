package com.tayek.audio;
import java.io.BufferedInputStream;
import java.util.logging.Logger;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
public class Audio implements Runnable {
    public enum Sound {
        challenge,stone,atari,capture,pass,illegal;
    }
    private Audio(String filename) {
        this.filename=filename;
    }
    @Override public void run() {
        started=true;
        completed=false;
        try {
            Clip clip=AudioSystem.getClip();
            AudioInputStream inputStream=AudioSystem.getAudioInputStream(new BufferedInputStream(Audio.class.getResourceAsStream(filename)));
            if(inputStream!=null) {
                clip.open(inputStream);
                FloatControl gainControl=(FloatControl)clip.getControl(FloatControl.Type.MASTER_GAIN);
                gainControl.setValue(+6.0f); // ?
                clip.start();
                // probably need a timeout here!!
                while(clip.getMicrosecondLength()!=clip.getMicrosecondPosition())
                    Thread.yield(); // wait
            } else {
                System.out.println("null input stream!");
                logger.warning("audio"+" "+" null inpit stream!");
            }
            completed=true;
        } catch(Exception e) {
            System.err.println(e);
            completed=false;
        }
    }
    private static Audio play(String filename) {
        System.out.println(filename);
        Audio audio=new Audio(filename);
        Thread thread=new Thread(audio,"audio");
        audio.thread=thread;
        System.out.println("started");
        thread.start();
        return audio;
    }
    public static Audio play(Sound sound) {
        switch(sound) {
            case challenge:
                return play("gochlng.wav");
            case stone:
                return play("stone.wav");
            case atari:
                return play("goatari.wav");
            case capture:
                return play("gocaptb.wav");
            case pass:
                return play("gopass.wav");
            case illegal:
                return play("goillmv.wav");
            default:
                logger.warning(""+" "+"default where!");
                return null;
        }
    }
    public static void main(String[] args) throws InterruptedException {
        if(true) play(Sound.atari);
        else {
            System.out.println("sounds");
            for(Sound sound:Sound.values()) {
                System.out.println(sound);
                Audio audio=play(sound);
                if(audio!=null) audio.thread.join();
                else System.out.println("no sound for: "+sound);
            }
        }
    }
    final String filename;
    transient boolean started,completed;
    Thread thread;
    public static final Logger logger=Logger.getLogger(Audio.class.getName());
}
