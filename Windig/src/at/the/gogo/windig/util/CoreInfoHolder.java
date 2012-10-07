package at.the.gogo.windig.util;

import android.speech.tts.TextToSpeech;

public class CoreInfoHolder {

    private TextToSpeech          mTts;
    private boolean               speakit;

    private boolean               speechRecoAvailable;

    private static CoreInfoHolder instance;

    public static CoreInfoHolder getInstance() {
        if (CoreInfoHolder.instance == null) {
            CoreInfoHolder.instance = new CoreInfoHolder();
        }
        return CoreInfoHolder.instance;
    }

    public TextToSpeech getTts() {
        return mTts;
    }

    public void setTts(final TextToSpeech mTts) {
        this.mTts = mTts;
    }

    public boolean isSpeakit() {
        return speakit;
    }

    public void setSpeakit(final boolean speakit) {
        this.speakit = speakit;
    }

    public boolean isSpeechRecoAvailable() {
        return speechRecoAvailable;
    }

    public void setSpeechRecoAvailable(final boolean speechRecoAvailable) {
        this.speechRecoAvailable = speechRecoAvailable;
    }

}
