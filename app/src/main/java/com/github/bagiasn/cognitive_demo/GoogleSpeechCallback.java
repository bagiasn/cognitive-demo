package com.github.bagiasn.cognitive_demo;

import com.github.bagiasn.cognitive_demo.tasks.GoogleSpeechTask;

/**
 * Callback interface for routing actions from the {@link GoogleSpeechTask}.
 */

public interface GoogleSpeechCallback {

    void onSpeechStarted();

    void onSpeechResult(String result);
}
