package at.the.gogo.parkoid.util.speech;

import android.speech.tts.TextToSpeech;
import at.the.gogo.parkoid.util.CoreInfoHolder;

public class SpeakItOut {

//    private static Semaphore sem = new Semaphore(1);

    public static void speak(final String text) {
        if (CoreInfoHolder.getInstance().getTts() != null) {
//            try {
//                sem.acquire();
//
                CoreInfoHolder.getInstance().getTts()
                        .speak(text, TextToSpeech.QUEUE_ADD, // Drop all
                                                               // pending
                                                               // entries in the
                                                               // playback
                                                               // queue.
                                null);

//            } catch (final InterruptedException e) {
//                Util.dd("Speech - Semaphore aquireing failed");
//            }
//
//            sem.release();

        }

    }

}
