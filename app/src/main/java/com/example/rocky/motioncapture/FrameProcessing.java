package com.example.rocky.motioncapture;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.util.Log;
import android.widget.Button;

import java.io.InterruptedIOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Queue;

/**
 * Created by V on 5/8/2016.
 */
public class FrameProcessing implements Runnable{

    //pause time in millis
    private long pause;
    private Queue frameQueue;
    private Queue timeStampQueue;
    private Button processingButton;
    private HashSet<int[]> pixelSet = new HashSet<int[]>();
    private int[] centroid;

    public FrameProcessing(long pause, Queue frameQueue, Queue timeStampQueue, Button processingButton) {
        this.pause = pause;
        this.frameQueue = frameQueue;
        this.timeStampQueue = timeStampQueue;
        this.processingButton = processingButton;
    }

    public void run() {
        while (true) {
            if (processingButton.getText().equals("Stop") && !frameQueue.isEmpty())
                processFrame((Bitmap) frameQueue.poll());
            try {
                Thread.sleep(2, 0);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void processFrame(Bitmap frame){
        //do processing
        int time = (int) timeStampQueue.poll();
        int height = frame.getHeight();
        int width = frame.getWidth();
        int color = 0;
        for (int i = 0; i < height; i++){
            for ( int j = 0; j < width; j++){
                color = Color.red(frame.getPixel(j, i));
                if( color > 200)
                    pixelSet.add(new int[] {i, j});
            }
        }
        centroid = centroid(pixelSet);
        Log.d("FRAME_PROCESSING", "Centroid: " + Arrays.toString(centroid));
        MyCamera.motionMap.add(new int[] {time, centroid[0], centroid[1]});
        if (MyCamera.frameQueue.size() % 20 == 0)
            Log.d("FRAME_PROCESSING", frameQueue.size() + " frames");
        try {
            Thread.sleep(pause, 0);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static int[] centroid(HashSet points) {
        float[] centroid = { 0, 0 };

        Iterator pointIterator = points.iterator();
        int[] point;
        while(pointIterator.hasNext()){
            point = (int[]) pointIterator.next();
            centroid[0] += point[0];
            centroid[1] += point[1];
        }

        int totalPoints = points.size();
        centroid[0] = centroid[0] / totalPoints;
        centroid[1] = centroid[1] / totalPoints;

        return new int[] {Math.round(centroid[0]), Math.round(centroid[1])};
    }
}
