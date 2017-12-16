package com.github.bagiasn.cognitive_demo;

/**
 * Callback interface for routing actions from the {@link CloudVisionTask}
 */

public interface CloudVisionCallback {

    void onVisionApiResult(String result);
}
