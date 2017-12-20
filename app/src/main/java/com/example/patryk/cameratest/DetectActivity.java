package com.example.patryk.cameratest;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


public class DetectActivity extends AppCompatActivity {

    private Executor executor = Executors.newSingleThreadExecutor();
    static final int REQUEST_IMAGE_CAPTURE = 1;

    private static final String INPUT_NAME = "input";
    private static final String OUTPUT_NAME = "output";

    private static final String MODEL_FILE = "file:///android_asset/frozen_inference_graph.pb";
    private static final String LABEL_FILE = "file:///android_asset/graph_label_strings.txt";

    private static final int INPUT_SIZE = 960;

    private Classifier classifier;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detect);
    }


    public void dispatchTakePictureIntent(View view) {
        ImageView imageView = (ImageView) findViewById(R.id.imageView);
        Bitmap bitmap;
        try {
            InputStream ims = getAssets().open("image10.jpg");
            BufferedInputStream bufferedInputStream = new BufferedInputStream(ims);
            bitmap = BitmapFactory.decodeStream(bufferedInputStream);
            imageView.setImageBitmap(bitmap);
        }
        catch(IOException ex) {
            return;
        }
        android.os.SystemClock.sleep(1000);
        detect(bitmap);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        ImageView imageView = (ImageView) findViewById(R.id.imageView);
        EditText editText = (EditText) findViewById(R.id.editText);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
//            float res = detect(imageBitmap);
//            editText.setText(valueOf(res));
            imageView.setImageBitmap(imageBitmap);
            detect(imageBitmap);
        }
    }

    public void detect(Bitmap bitmap) {
        EditText editText = (EditText) findViewById(R.id.editText);

        String res = initTensorFlowAndLoadModel(readAllBytes(bitmap));
        editText.setText(res);
    }

    private static byte[] readAllBytes(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        return byteArray;
    }

    private String initTensorFlowAndLoadModel(byte[] pixels) {

        String value = "";
        try {
            classifier = ImageDetector.create(
                    getAssets(),
                    MODEL_FILE,
                    LABEL_FILE,
                    INPUT_SIZE,
                    INPUT_NAME,
                    OUTPUT_NAME);
        } catch (final Exception e) {
            throw new RuntimeException("Error initializing TensorFlow!", e);
        }


        List<Classifier.Recognition> results;
        results = classifier.recognizeImage(pixels);

        if (results.size() > 0) {
            value = " Result: " +results.get(0).getTitle();
        }

        classifier.close();
        return value;
    }
}
