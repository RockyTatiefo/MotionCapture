package com.example.rocky.motioncapture;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

/**
 * Created by Rocky on 4/24/2016.
 */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback{

    private SurfaceHolder mHolder;
    private Camera mCamera;

    public CameraPreview(Context context, Camera camera) {
        super(context);

        mCamera = camera;
        //get the holder and set this class as the callback, so we can get camera data here
        mCamera.setDisplayOrientation(90);
        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_NORMAL);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (IOException e) {
            Log.d("Error", "Error setting camera preview: " + e.getMessage());
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if(mHolder.getSurface() == null)//check if the surface is ready to receive camera data
            return;

        try{
            mCamera.stopPreview();
        } catch (Exception e){
            //this will happen when you are trying the camera if it's not running
        }

        //now, recreate the camera preview
        try{
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
        } catch (IOException e) {
            Log.d("ERROR", "Camera error on surfaceChanged " + e.getMessage());
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mCamera.stopPreview();
        mCamera.release();
    }
}
