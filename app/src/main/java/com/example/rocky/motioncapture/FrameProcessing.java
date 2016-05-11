package com.example.rocky.motioncapture;

import android.util.Log;
import android.widget.Button;

import java.io.InterruptedIOException;
import java.util.Queue;

/**
 * Created by V on 5/8/2016.
 */
public class FrameProcessing implements Runnable{

    //pause time in millis
    private long pause;
    private Queue frameQueue;
    private Button processingButton;

    public FrameProcessing(long pause, Queue frameQueue, Button processingButton) {
        this.pause = pause;
        this.frameQueue = frameQueue;
        this.processingButton = processingButton;
    }

    public void run() {
        while (true) {
            if (processingButton.getText() == "Stop Processing" && !frameQueue.isEmpty())
                processFrame((byte[]) frameQueue.poll());
            try {
                Thread.sleep(10, 0);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
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
