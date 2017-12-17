package com.github.bagiasn.cognitive_demo;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.speech.SpeechRecognizer;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.bagiasn.cognitive_demo.tasks.CloudVisionTask;
import com.github.bagiasn.cognitive_demo.tasks.GoogleSpeechTask;
import com.github.bagiasn.cognitive_demo.utils.PermissionUtils;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,
    CloudVisionCallback, GoogleSpeechCallback {

    public static String PACKAGE_NAME;

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int GALLERY_PERMISSIONS_REQUEST = 0;
    private static final int RECORD_PERMISSIONS_REQUEST = 1;
    private static final int GALLERY_IMAGE_REQUEST = 2;
    // UI elements.
    private ImageView imgHolder;
    private TextView txtVisionResult;
    private TextView txtSpeechResult;
    private ProgressBar pgSpeechBar;
    private ProgressBar pgVisionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        PACKAGE_NAME = getPackageName();

        Button btnSelectImage = findViewById(R.id.main_button_select_image);
        btnSelectImage.setOnClickListener(this);

        Button btnStartRecording = findViewById(R.id.main_button_start_recording);
        btnStartRecording.setOnClickListener(this);

        imgHolder = findViewById(R.id.main_image_holder);
        txtVisionResult = findViewById(R.id.main_textview_vision_result);
        txtSpeechResult = findViewById(R.id.main_textview_speech_result);
        pgSpeechBar = findViewById(R.id.main_progress_speech);
        pgVisionBar = findViewById(R.id.main_progress_vision);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.main_button_select_image:
                startGalleryChooser();
                break;
            case R.id.main_button_start_recording:
                startSpeechRecognition();
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            // Indicate progress.
            pgVisionBar.setVisibility(View.VISIBLE);
            // Clear any previous results.
            txtVisionResult.setText("");
            txtVisionResult.setVisibility(View.INVISIBLE);
            txtSpeechResult.setText("");
            // Start the Vision request.
            uploadImage(data.getData());
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case GALLERY_PERMISSIONS_REQUEST:
                if (PermissionUtils.permissionGranted(requestCode, GALLERY_PERMISSIONS_REQUEST, grantResults)) {
                    startGalleryChooser();
                }
                break;
            case RECORD_PERMISSIONS_REQUEST:
                if (PermissionUtils.permissionGranted(requestCode, RECORD_PERMISSIONS_REQUEST, grantResults)) {
                    startSpeechRecognition();
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onVisionApiResult(String result) {
        if (txtVisionResult != null) {
            txtVisionResult.setText(result);
        }
        pgVisionBar.setVisibility(View.GONE);
    }

    @Override
    public void onSpeechStarted() {
        pgSpeechBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onSpeechResult(String result) {
        if (txtSpeechResult != null) {
            txtSpeechResult.setText(result);
            txtSpeechResult.setVisibility(View.VISIBLE);
            // Show Vision results, too, for comparison.
            txtVisionResult.setVisibility(View.VISIBLE);
        }
        // Hide progress bar.
        pgSpeechBar.setVisibility(View.GONE);
    }

    public void startGalleryChooser() {
        if (PermissionUtils.requestPermission(this, GALLERY_PERMISSIONS_REQUEST, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select an image"), GALLERY_IMAGE_REQUEST);
        }
    }

    public void startSpeechRecognition() {
        if (PermissionUtils.requestPermission(this, RECORD_PERMISSIONS_REQUEST, Manifest.permission.RECORD_AUDIO)) {
            // Google Recognizer needs to run on the main thread.
            SpeechRecognizer recognizer = SpeechRecognizer.createSpeechRecognizer(this);
            final Handler mainHandler = new Handler(Looper.getMainLooper());
            mainHandler.post(new GoogleSpeechTask(recognizer, this));
        }
    }

    public void uploadImage(Uri uri) {
        if (uri != null) {
            try {
                // scale the image to save on bandwidth
                Bitmap bitmap = scaleBitmapDown(
                                MediaStore.Images.Media.getBitmap(getContentResolver(), uri),
                                1200);

                // Start uploading to Vision API.
                CloudVisionTask task = new CloudVisionTask(bitmap, getPackageManager(), this);
                task.execute();
                // Show the image.
                imgHolder.setImageBitmap(bitmap);
            } catch (IOException e) {
                Log.d(TAG, "Image picking failed because " + e.getMessage());
                Toast.makeText(this, R.string.image_picker_error, Toast.LENGTH_LONG).show();
            }
        } else {
            Log.d(TAG, "Image picker gave us a null image.");
            Toast.makeText(this, R.string.image_picker_error, Toast.LENGTH_LONG).show();
        }
    }

    public Bitmap scaleBitmapDown(Bitmap bitmap, @SuppressWarnings("SameParameterValue") int maxDimension) {

        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        int resizedWidth = maxDimension;
        int resizedHeight = maxDimension;

        if (originalHeight > originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = (int) (resizedHeight * (float) originalWidth / (float) originalHeight);
        } else if (originalWidth > originalHeight) {
            resizedWidth = maxDimension;
            resizedHeight = (int) (resizedWidth * (float) originalHeight / (float) originalWidth);
        } else if (originalHeight == originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = maxDimension;
        }
        return Bitmap.createScaledBitmap(bitmap, resizedWidth, resizedHeight, false);
    }
}
