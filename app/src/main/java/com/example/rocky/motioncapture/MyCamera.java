package com.example.rocky.motioncapture;

/**
 * Created by V on 5/8/2016.
 */

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.io.File;
import java.util.LinkedList;
import java.util.Queue;

public class MyCamera extends Activity
{
    private CameraPreview camPreview;
    private FrameLayout mainLayout;
    private int PreviewSizeWidth = 720;
    private int PreviewSizeHeight= 480;
    private CharSequence[] processingText = {"Process", "Stop Processing"};
    private CharSequence[] startStopText = {"Record","Stop"};
    public static Queue frameQueue = new LinkedList<byte[]>();

    public static Handler mHandler = new Handler(Looper.getMainLooper());

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        //Set this APK Full screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //Set this APK no title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_video_capture);

        SurfaceView camView = new SurfaceView(this);
        SurfaceHolder camHolder = camView.getHolder();
        camPreview = new CameraPreview(PreviewSizeWidth, PreviewSizeHeight);

        camHolder.addCallback(camPreview);
        camHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        mainLayout = (FrameLayout) findViewById(R.id.camera_preview);
        mainLayout.addView(camView, new ViewGroup.LayoutParams(PreviewSizeWidth, PreviewSizeHeight));

        // first parameter is sleep time in milliseconds between access to Queue
        // second parameter is Queue to process images from
        // third parameter is the button to use to start and stop processing
        startProcessing(50, frameQueue, (Button) findViewById(R.id.button_processing));

        captureSetup();

        findViewById(R.id.imgClose).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.exit(0);
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        if (event.getAction() == MotionEvent.ACTION_DOWN)
        {
            int X = (int)event.getX();
            if ( X >= PreviewSizeWidth )
                mHandler.postDelayed(TakePicture, 300);
            else
            camPreview.CameraStartAutoFocus();
        }
        return true;
    }

    private void captureSetup() {
        final Button startStopButton = (Button) findViewById(R.id.button_capture);
        if (startStopButton != null) {
            startStopButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(startStopButton.getText().equals(startStopText[0])) {
                        startStopButton.setText(startStopText[1]);
                        long startTime = System.currentTimeMillis();
                        Log.d("FRAME_CAPTURE", "Frame captured started.");
                        Toast.makeText(MyCamera.this, "Frame capture started.", Toast.LENGTH_SHORT).show();
                        CameraPreview.TakePicture = true;
                    } else {
                        startStopButton.setText(startStopText[0]);
                        CameraPreview.TakePicture = false;
                    }
                }
            });
        }
    }

    private Runnable TakePicture = new Runnable()
    {
        String extStorageDirectory = Environment.getExternalStorageDirectory().toString();
        public void run()
        {
            String MyDirectory_path = extStorageDirectory;

            File file = new File(MyDirectory_path);
            if (!file.exists())
                file.mkdirs();
            String PictureFileName = MyDirectory_path + "/MyPicture.jpg";

            camPreview.CameraTakePicture(PictureFileName);
        }
    };

    private void startProcessing(long pause, Queue frameQueue, final Button processingButton){
        processingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(processingButton.getText().equals(processingText[0]))
                    processingButton.setText(processingText[1]);
                else
                    processingButton.setText(processingText[0]);
            }
        });
        FrameProcessing processFrames = new FrameProcessing(pause, frameQueue, processingButton);
        Thread t = new Thread(processFrames);
        t.start();
    }
}