package com.example.rocky.motioncapture;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.util.Log;
import android.view.SurfaceHolder;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.widget.Toast;

/**
 * Created by V on 5/8/2016.
 */
public class CameraPreview implements SurfaceHolder.Callback, Camera.PreviewCallback
{
    private Camera mCamera = null;
    private int PreviewSizeWidth;
    private int PreviewSizeHeight;
    private String NowPictureFileName;
    public static Boolean TakePicture = false;
    private YuvImage img = null;


    public CameraPreview(int PreviewlayoutWidth, int PreviewlayoutHeight)
    {
        PreviewSizeWidth = PreviewlayoutWidth;
        PreviewSizeHeight = PreviewlayoutHeight;
    }

    @Override
    public void onPreviewFrame(byte[] arg0, Camera arg1)
    {
        // At preview mode, the frame data will push to here.
        // But we do not want these data.
        Parameters parameters = arg1.getParameters();
        int imageFormat = parameters.getPreviewFormat();
        if (imageFormat == ImageFormat.NV21)
        {
//            Rect rect = new Rect(0, 0, PreviewSizeWidth, PreviewSizeHeight);
            img = new YuvImage(arg0, ImageFormat.NV21, PreviewSizeWidth, PreviewSizeHeight, null);
//            OutputStream outStream = null;
//            File file = new File(NowPictureFileName);
//            try
//            {
//                outStream = new FileOutputStream(file);
//                img.compressToJpeg(rect, 100, outStream);
//                outStream.flush();
//                outStream.close();
//            }
//            catch (FileNotFoundException e)
//            {
//                e.printStackTrace();
//            }
//            catch (IOException e)
//            {
//                e.printStackTrace();
//            }
           if (TakePicture) {
               MyCamera.frameQueue.add(img.getYuvData());
               if (MyCamera.frameQueue.size() % 20 == 0)
                   Log.d("FRAME_CAPTURE", MyCamera.frameQueue.size() + " frames");
//                try {
//                    Thread.sleep(10, 0);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
           }
        }
    }



    @Override
    public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3)
    {
        Parameters parameters;

        parameters = mCamera.getParameters();
        // Set the camera preview size
        parameters.setPreviewSize(PreviewSizeWidth, PreviewSizeHeight);
        // Set the take picture size, you can set the large size of the camera supported.
        parameters.setPictureSize(PreviewSizeWidth, PreviewSizeHeight);

        // Turn on the camera flash.
        String NowFlashMode = parameters.getFlashMode();
        if ( NowFlashMode != null )
            parameters.setFlashMode(Parameters.FLASH_MODE_ON);
        // Set the auto-focus.
        String NowFocusMode = parameters.getFocusMode ();
        if ( NowFocusMode != null )
            parameters.setFocusMode("auto");

        mCamera.setParameters(parameters);

        mCamera.startPreview();
    }

    @Override
    public void surfaceCreated(SurfaceHolder arg0)
    {
        mCamera = Camera.open();
        try
        {
            // If did not set the SurfaceHolder, the preview area will be black.
            mCamera.setPreviewDisplay(arg0);
            mCamera.setPreviewCallback(this);
        }
        catch (IOException e)
        {
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder arg0)
    {
        mCamera.setPreviewCallback(null);
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
    }

    // Take picture interface
    public void CameraTakePicture(String FileName)
    {
//        TakePicture = true;
        NowPictureFileName = FileName;
        mCamera.autoFocus(myAutoFocusCallback);
    }

    // Set auto-focus interface
    public void CameraStartAutoFocus()
    {
//        TakePicture = false;
        mCamera.autoFocus(myAutoFocusCallback);
    }


    //=================================
    //
    // AutoFocusCallback
    //
    //=================================
    AutoFocusCallback myAutoFocusCallback = new AutoFocusCallback()
    {
        public void onAutoFocus(boolean arg0, Camera NowCamera)
        {
//            if ( TakePicture )
//            {
//                NowCamera.stopPreview();//fixed for Samsung S2
//                NowCamera.takePicture(shutterCallback, rawPictureCallback, jpegPictureCallback);
//                TakePicture = false;
//            }
        }
    };
    ShutterCallback shutterCallback = new ShutterCallback()
    {
        public void onShutter()
        {
            // Just do nothing.
        }
    };

    PictureCallback rawPictureCallback = new PictureCallback()
    {
        public void onPictureTaken(byte[] arg0, Camera arg1)
        {
//            MyCamera.frameQueue.add(arg0);
        }
    };

    PictureCallback jpegPictureCallback = new PictureCallback()
    {
        public void onPictureTaken(byte[] data, Camera arg1)
        {
            // Save the picture.
            try {
                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0,data.length);
                FileOutputStream out = new FileOutputStream(NowPictureFileName);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    };



}