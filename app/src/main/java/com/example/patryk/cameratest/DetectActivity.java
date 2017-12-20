package com.example.patryk.cameratest;

import android.content.pm.ActivityInfo;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.*;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.os.Message;
import android.widget.RelativeLayout;
/*
import org.tensorflow.DataType;
import org.tensorflow.Graph;
import org.tensorflow.Output;
import org.tensorflow.Session;
import org.tensorflow.Tensor;
import org.tensorflow.TensorFlow;
import org.tensorflow.types.UInt8;
*/
import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.Timer;
import java.util.TimerTask;


import static java.lang.String.valueOf;

public class DetectActivity extends AppCompatActivity {
    @SuppressWarnings("deprecation")
    boolean cameraWorking = false;
    EditText feedback;
    Timer cameraTime;
    CameraPreview widokPodgladu;
    RelativeLayout rl;
    ImageView imageView;
    private Camera cam;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detect);
        cam = getCameraInstance();
        cameraTime = new Timer("test",true);
        TimerTask takePhoto = new TimerTask() {
            @Override
            public void run() {
                mHandler.obtainMessage(1).sendToTarget();
            }
        };
        cameraTime.scheduleAtFixedRate(takePhoto, 0, 5000);
        feedback = (EditText) findViewById(R.id.editText);
        widokPodgladu = new CameraPreview(this,cam);
        rl = (RelativeLayout) findViewById(R.id.rl);
        rl.addView(widokPodgladu);
        imageView = (ImageView) findViewById(R.id.imageView);
    }



    public Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            if(cameraWorking) {
                cam.startPreview();
                int before = getRequestedOrientation();
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                cam.takePicture(null,null,mPicture);
                setRequestedOrientation(before);
                feedback.setText("Fajnie");
            }
            else {
                feedback.setText("Super");
            }
        }
    };
    //kamera

    private Camera getCameraInstance() {
        Camera camera = null;
        try {
            camera = Camera.open();
        } catch (Exception e) {
            // cannot get camera or does not exist
        }
        return camera;
    }

    PictureCallback mPicture = new PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
                Bitmap bitmap =  BitmapFactory.decodeByteArray(data, 0 , data.length);
                Matrix matrix = new Matrix();
                matrix.postRotate(90);
                imageView.setImageBitmap(Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true));

        }
    };




    public void dispatchTakePictureIntent(View view) {
        cameraWorking = !cameraWorking;
    }



    public void detect(Bitmap bitmap) {
        boolean logStats = false;
        String[] outputNames = null;
        String modelName = "frozen_inference_graph.pb";
        EditText editText = (EditText) findViewById(R.id.editText);

        TensorFlowInferenceInterface d = new TensorFlowInferenceInterface(getAssets(), modelName);
        byte[] byteArray = readAllBytes(bitmap);
        d.feedString(modelName, byteArray);
        d.run(outputNames, logStats);

    }

    private static byte[] readAllBytes(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        return byteArray;
    }

}
