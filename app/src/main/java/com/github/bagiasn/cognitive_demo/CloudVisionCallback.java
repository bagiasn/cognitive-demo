package com.github.bagiasn.cognitive_demo;

import com.github.bagiasn.cognitive_demo.tasks.CloudVisionTask;

/**
 * Callback interface for routing actions from the {@link CloudVisionTask}
 */

public interface CloudVisionCallback {

    void onVisionApiResult(String result);
}
