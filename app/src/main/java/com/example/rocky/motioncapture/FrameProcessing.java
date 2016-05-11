package com.example.rocky.motioncapture;

import android.util.Log;

import java.util.Queue;

/**
 * Created by V on 5/8/2016.
 */
public class FrameProcessing implements Runnable{

    //pause time in millis
    private long pause;
    private Queue frameQueue;

    public FrameProcessing(long pause, Queue frameQueue) {
        this.pause = pause;
        this.frameQueue = frameQueue;
    }

    public void run() {
        while (!this.frameQueue.isEmpty())
            processFrame((byte[]) frameQueue.poll());
    }

    public void processFrame(byte[] frame){
        //do processing
        try {
            Log.d("FRAME_CAPTURE", frameQueue.size() + " frames");
            Thread.sleep(pause, 0);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
