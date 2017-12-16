package com.github.bagiasn.cognitive_demo;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;

import java.util.ArrayList;

/**
 * Task for using the {@link SpeechRecognizer} to get the user's decision.
 */

public class GoogleSpeechTask implements Runnable, RecognitionListener {
    // Logging tag
    private static final String TAG = GoogleSpeechTask.class.getSimpleName();

    private GoogleSpeechCallback callback;
    // SpeechRecognizer needs to run on the main thread.
    // So, we pre-create it and pass it here as a parameter.
    private SpeechRecognizer speechRecognizer;
    private Intent recognitionIntent;

    public GoogleSpeechTask(SpeechRecognizer speechRecognizer, GoogleSpeechCallback callback) {
        this.speechRecognizer = speechRecognizer;
        this.callback = callback;
    }

    @Override
    public void run() {
        // Construct the desired intent.
        setupIntent();
        // Set this class as listener to handle recognition events.
        speechRecognizer.setRecognitionListener(this);
        // Start the actual recognition.
        speechRecognizer.startListening(recognitionIntent);
        // Indicate that recording has started.
        callback.onSpeechStarted();
    }

    @Override
    public void onReadyForSpeech(Bundle bundle) {

    }

    @Override
    public void onBeginningOfSpeech() {

    }

    @Override
    public void onRmsChanged(float v) {

    }

    @Override
    public void onBufferReceived(byte[] bytes) {

    }

    @Override
    public void onEndOfSpeech() {

    }

    @Override
    public void onError(int i) {

    }

    @Override
    public void onResults(Bundle results) {
        ArrayList data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        if (data == null || data.size() == 0) {
            callback.onSpeechResult("An error occurred. Please try again.");
            return;
        }
        String text = (String) data.get(0);
        Log.d(TAG, text);
        callback.onSpeechResult(text);
    }

    @Override
    public void onPartialResults(Bundle bundle) {

    }

    @Override
    public void onEvent(int i, Bundle bundle) {

    }

    private void setupIntent() {
        recognitionIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognitionIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "el-GR");
        recognitionIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "el-GR");
        recognitionIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        recognitionIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, MainActivity.PACKAGE_NAME);
    }

}
