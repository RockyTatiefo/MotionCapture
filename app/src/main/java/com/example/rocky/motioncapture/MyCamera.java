package com.example.rocky.motioncapture;

/**
 * Created by V on 5/8/2016.
 */

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import java.io.File;

public class MyCamera extends Activity
{
    private CameraPreview camPreview;
    private FrameLayout mainLayout;
    private int PreviewSizeWidth = 640;
    private int PreviewSizeHeight= 480;

    private Handler mHandler = new Handler(Looper.getMainLooper());

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
        Log.d("FRAME_CAPTURE", CameraPreview.frameQueue.size() + " frames");
        return true;
    };

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
}