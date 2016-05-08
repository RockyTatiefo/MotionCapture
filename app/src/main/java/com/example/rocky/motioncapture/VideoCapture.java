package com.example.rocky.motioncapture;

import android.hardware.Camera;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;

public class VideoCapture extends AppCompatActivity {
    private Camera mCamera = null;
    private CameraPreview mCameraView = null;
    private Button startStopButton;
    private CharSequence[] startStopText = {"Record","Stop"};
    protected Queue frameQueue = new LinkedList<byte[]>();
    private long timeSpent = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_capture);

        try{
            mCamera = Camera.open();//you can use open(int) to use different cameras
        } catch (Exception e){
            Log.d("ERROR", "Failed to get camera: " + e.getMessage());
        }

        if(mCamera != null) {
            mCameraView = new CameraPreview(this, mCamera);//create a SurfaceView to show camera data
            FrameLayout camera_view = (FrameLayout)findViewById(R.id.camera_preview);
            camera_view.addView(mCameraView);//add the SurfaceView to the layout
        }

        captureSetup();

        exitSetup();

    }

    protected void captureSetup() {
        final Button startStopButton = (Button) findViewById(R.id.button_capture);
        if (startStopButton != null) {
            startStopButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mCamera != null){
                        if(startStopButton.getText().equals(startStopText[0])) {
                            startStopButton.setText(startStopText[1]);
                            long startTime = System.currentTimeMillis();
                            while(true){
                                try {
                                    mCamera.takePicture(shutterCallback(), rawDataCallback(), postViewCallback(), null);
                                    Thread.sleep(10,0);
//                                  mCamera.startPreview();
                                } catch (Exception e){
                                    timeSpent = System.currentTimeMillis() - startTime;
                                    break;
                                }
                            }
                        } else {
                            startStopButton.setText(startStopText[0]);
                            mCamera.release();
                            try {
                                mCamera.open();
                            } catch(Exception e) {
                                Log.d("ERROR", "Failed to reconnect to camera: " + e.getMessage());
                            }
                            Log.d("FRAME_CAPTURE", "Frame captured stopped.");
                            Toast.makeText(VideoCapture.this, "Frame captured stopped.", Toast.LENGTH_SHORT).show();
                            Log.d("FRAME_CAPTURE", frameQueue.size() + " frames captured in " + timeSpent/1000);
                            Toast.makeText(VideoCapture.this, frameQueue.size() + " frames captured in " + timeSpent/1000, Toast.LENGTH_LONG).show();
                        }
                    }
                }
            });
        }
    }

    private Camera.ShutterCallback shutterCallback() {
        return new Camera.ShutterCallback() {
            @Override
            public void onShutter() {
                Log.d("FRAME_CAPTURE", "Frame captured started.");
                Toast.makeText(VideoCapture.this, "Frame capture started.", Toast.LENGTH_SHORT).show();
            }
        };
    }

    private Camera.PictureCallback rawDataCallback() {
        return new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                frameQueue.add(data);
            }
        };
    }

    private Camera.PictureCallback postViewCallback() {
        return new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                //might not be needed
            }
        };
    }

    protected void exitSetup() {
        ImageButton imgClose = (ImageButton)findViewById(R.id.imgClose);
        if (imgClose != null)
            imgClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    System.exit(0);
                }
            });
    }

    private void startProcessing(long pause, Queue frameQueue){
        FrameProcessing processFrames = new FrameProcessing(10, frameQueue);
        Thread t = new Thread(processFrames);
        t.start();
    }
}
