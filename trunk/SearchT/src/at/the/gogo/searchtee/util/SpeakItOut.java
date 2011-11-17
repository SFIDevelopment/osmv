package at.the.gogo.searchtee.util;

import android.speech.tts.TextToSpeech;

public class SpeakItOut {

	private static TextToSpeech mTts;

	public static TextToSpeech getTts() {
		return mTts;
	}

	public static void setTts(TextToSpeech mTts) {
		SpeakItOut.mTts = mTts;
	}

	
	
	
	
	public static void speak(final String text) {
		if (getTts() != null) {
			getTts().speak(text, TextToSpeech.QUEUE_ADD, // Drop all
															// pending
															// entries in the
															// playback
															// queue.
					null);
		}

	}

}
