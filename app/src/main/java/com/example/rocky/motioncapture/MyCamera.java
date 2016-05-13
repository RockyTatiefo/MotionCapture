package com.example.rocky.motioncapture;

/**
 * Created by V on 5/8/2016.
 */

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Bitmap;
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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;

public class MyCamera extends Activity
{
    private CameraPreview camPreview;
    private FrameLayout mainLayout;
    private int PreviewSizeWidth = 640;
    private int PreviewSizeHeight= 480;
    private CharSequence[] processingText = {"Process", "Stop Processing"};
    private CharSequence[] startStopText = {"Record","Stop"};
    public static Queue frameQueue = new LinkedList<>();
    public static Queue timeStampQueue = new LinkedList<>();
    public static long startTime = System.currentTimeMillis();
    public static ArrayList<int[]> motionMap = new ArrayList<>();

    private BluetoothAdapter bluetooth = BluetoothAdapter.getDefaultAdapter();
//    private ArrayList<int[]> data;
    // Data will be in cm.
    public String address;
    ConnectedThread mConnect;

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

        startProcessing(2, frameQueue, timeStampQueue, (Button) findViewById(R.id.button_capture));

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
                        startTime = System.currentTimeMillis();
                        Log.d("FRAME_CAPTURE", "Frame captured started.");
                        Toast.makeText(MyCamera.this, "Frame capture started.", Toast.LENGTH_SHORT).show();
                        CameraPreview.TakePicture = true;

                        // Add join (?)
                        // Save data as CSV

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

    private void startProcessing(long pause, Queue frameQueue, Queue timeStampQueue, final Button processingButton){
//        processingButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if(processingButton.getText().equals(processingText[0]))
//                    processingButton.setText(processingText[1]);
//                else
//                    processingButton.setText(processingText[0]);
//            }
//        });
        FrameProcessing processFrames = new FrameProcessing(pause, frameQueue, timeStampQueue, processingButton);
        Thread t = new Thread(processFrames);
        t.start();
    }


    // Bluetooth
    public void setBluetooth(View view){
        Intent intent = new Intent(this, SetUpBluetooth.class);
        startActivity(intent);
    }


    public void onResume(){
        super.onResume();

        // Get the address from the intent
        Intent intent = getIntent();
        address = intent.getStringExtra(SetUpBluetooth.EXTRA_DEVICE_ADDRESS);

        // Connect the devices
        if(address != null) {
            BluetoothDevice device2 = bluetooth.getRemoteDevice(address);
            //Thread connect = new Thread(new ConnectThread(device2));
            //connect.start();

            // Test
            BluetoothSocket mmSocket;
            BluetoothDevice mmDevice;
            BluetoothSocket tmp = null;
            mmDevice = device2;

            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                // MY_UUID is the app's UUID string, also used by the server code
                tmp = device2.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
            } catch (IOException e) {
            }
            mmSocket = tmp;
            bluetooth.cancelDiscovery();

            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                mmSocket.connect();
                Toast.makeText(getBaseContext(), "Connection successful", Toast.LENGTH_SHORT).show();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and get out
                Toast.makeText(getBaseContext(), "Connection failed", Toast.LENGTH_SHORT).show();
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                    Toast.makeText(getBaseContext(), "Socket not closed", Toast.LENGTH_SHORT).show();
                }

            }
            mConnect = new ConnectedThread(mmSocket);
            mConnect.start();
        }
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }


        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);
                    // Send the obtained bytes to the UI activity
                    String readMessage = new String(buffer, 0, bytes);

                    // Make any received message stop recoding

                } catch (IOException e) {
                    break;
                }
            }
        }

        // Call this from the main activity to send data to the remote device
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) { }
        }

        // Call this from the main activity to shutdown the connection
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }

    //Save CSV data
    private void saveData(){
        
    }
}